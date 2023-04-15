package in.safety.safety;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kromer.filecompressor.FileCompressor;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import id.zelory.compressor.Compressor;

public class CameraService extends HiddenCameraService {
    private CameraConfig mCameraConfig;
    private StorageReference mStorageReference;
    private SharedPreferences emergencyManager;
    private String emergencyCode;
    public CameraService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        emergencyManager = getSharedPreferences("emergency_details", MODE_PRIVATE);
        emergencyCode = emergencyManager.getString("emergencyCode", null);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Emergencies").child(emergencyCode).child("Photos");
        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_90)
                .setCameraFocus(CameraFocus.CONTINUOUS_PICTURE)
                .build();
        startCamera(mCameraConfig);
        CountDownTimer timer;

        timer = new CountDownTimer(60000
                , 3000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mCameraConfig = new CameraConfig()
                        .getBuilder(CameraService.this)
                        .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .setImageRotation(CameraRotation.ROTATION_90)
                        .setCameraFocus(CameraFocus.CONTINUOUS_PICTURE)
                        .build();
                //if(ContextCompat.checkSelfPermission(CameraService.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(CameraService.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED)
                  startCamera(mCameraConfig);
                        takePicture();
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
        //stopCamera();
        return START_STICKY;
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_baseline_arrow_forward_24)
                .setContentTitle("Image")
                .setContentText("captured")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(123, builder.build());*/
        File compressedImageFile = Compressor.getDefault(this) .compressToFile(imageFile);
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
    }

    @Override
    public void onCameraError(int errorCode) {
        Log.v("ER CODE",""+errorCode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}