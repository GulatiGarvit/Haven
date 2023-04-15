package in.safety.safety;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Parameter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import id.zelory.compressor.Compressor;

public class CameraService1 extends Service
{

    private static final String CHANNEL_FOREGROUND_SERVICE = "121";
    private static final int NOTIFICATION_ID = 2;
    private StorageReference mStorageReference;
    private SharedPreferences emergencyManager;
    private String emergencyCode;
    private int counter = 0;

    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Parameters parameters;
    /** Called when the activity is first created. */
    @Override
    public void onCreate()
    {
        super.onCreate();

    }
    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);

        Log.e("LALA", "HOHO");

        emergencyManager = getSharedPreferences("emergency_details", MODE_PRIVATE);
        emergencyCode = emergencyManager.getString("emergencyCode", null);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Emergencies").child(emergencyCode).child("Photos");
        final SurfaceTexture sv = new SurfaceTexture(10);

        CountDownTimer timer = new CountDownTimer(60000,5000) {

            @Override
            public void onTick(long millisUntilFinished) {
                if(++counter%2==0) {
                    try {
                        takePicture(1, sv);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        takePicture(0, sv);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotification();
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_FOREGROUND_SERVICE,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        createNotificationChannel();
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new Notification.Builder(this, CHANNEL_FOREGROUND_SERVICE)
                        .setContentTitle("Title")
                        .setContentText("Description")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .setTicker("Ticker")
                        .build();

// Notification ID cannot be 0.
        startForeground(NOTIFICATION_ID, notification);
    }

    //Get a surface
    //tells Android that this surface will have its data constantly replaced
    //sv.setsetType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    Camera.PictureCallback mCall = new Camera.PictureCallback()
    {

        public void onPictureTaken(byte[] data, Camera camera)
        {
            //decode the data obtained by the camera into a Bitmap

            FileOutputStream outStream = null;
            try{
                File dir = new File(getApplicationContext().getFilesDir()+"/Emergency/"+emergencyCode+"/");
                if(!dir.isDirectory() || !dir.exists())
                {
                    if(!dir.mkdirs())
                        Log.e("ERROR", "Error creating directory");
                }
                File file = new File(getApplicationContext().getFilesDir()+"/Emergency/"+emergencyCode+"/"+getAlphaNumericString(10)+".jpg");
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(data);
                bos.flush();
                bos.close();
                File compressedImageFile = Compressor.getDefault(CameraService1.this) .compressToFile(file);
                String filename = ""+Math.random();
                mStorageReference.child(filename).putFile(Uri.fromFile(compressedImageFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mStorageReference.child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                FirebaseDatabase.getInstance().getReference().child("Emergencies").child(emergencyCode).child("photos").push().setValue(uri.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("IMG", "failed: "+e.toString());
                    }
                });
                Log.v("IMG", "captured");
                /*outStream = new FileOutputStream(Environment.getExternalStorageDirectory()+"/Image.jpg");
                outStream.write(data);
                outStream.close();*/
            } catch (FileNotFoundException e){
                Log.d("CAMERA-FNF", e.getMessage());
            } catch (IOException e){
                Log.d("CAMERA-IO", e.getMessage());
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void takePicture(int cameraFacing, SurfaceTexture sv) throws IOException {
        try{
            if (cameraFacing == 1) {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } catch (Exception e) {
            return;
        }
        mCamera.setPreviewTexture(sv);
        parameters = mCamera.getParameters();
        parameters.setRotation(90);
        if(parameters.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_AUTO))
            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        if(cameraFacing == 1)
            parameters.setRotation(270);

        if(cameraFacing == 0)
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);

        //set camera parameters
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mCamera.takePicture(null, null, mCall);
            }
        });
    }

    public String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
