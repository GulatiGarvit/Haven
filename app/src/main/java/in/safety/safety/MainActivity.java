package in.safety.safety;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import in.LocationUpdate;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraActivity;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {
    private static final int JOB_ID = 1;
    private static final long LOCATION_REFRESH_TIME = 0;
    private static final float LOCATION_REFRESH_DISTANCE = 0f;
    private SharedPreferences emergencyManager, loginManager;
    private boolean isInEmergency;
    private String emergencyCode = null;
    private DatabaseReference emergencyReference;
    private JobScheduler mJobScheduler;
    private FirebaseAuth mAuth;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForFcmToken();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null)
        {
            startActivity(new Intent(getApplicationContext(), OnboardingActivity.class));
            finish();
        }
        loginManager = getSharedPreferences("login_manager", MODE_PRIVATE);
        loginManager.edit().putString("uid", mAuth.getCurrentUser().getUid()).apply();
        emergencyManager = getSharedPreferences("emergency_details", MODE_PRIVATE);
        isInEmergency = emergencyManager.getBoolean("isInEmergency", false);
        if(!isJobServiceOn())
        {
            mJobScheduler = (JobScheduler)
                    getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,
                    new ComponentName(getPackageName(),
                            ScheduledLocationService.class.getName()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPeriodic(1800000, 600000);
            }
            else
            {
                builder.setPeriodic(1800000);
            }
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPersisted(true);
            mJobScheduler.schedule(builder.build());
        }
        /*if (mJobScheduler.schedule(builder.build()) <= 0) {
            Log.e("LocationErr", "Some error while scheduling the job");*/
    }

    private void checkForFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        ParseQuery<ParseObject> query = new ParseQuery<>("NotifKeys");
                        query.whereEqualTo("uid", mAuth.getCurrentUser().getUid()).getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                if(e != null) {
                                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND || e.getCode() == ParseException.INVALID_CLASS_NAME) {
                                        object = new ParseObject("NotifKeys");
                                        object.put("uid", mAuth.getCurrentUser().getUid());
                                    }
                                }
                                object.put("fcmKey", token);
                                object.saveInBackground();
                            }
                        });
                    }
                });
    }

    public void manageEmergencyContacts(View view) {
        //startService(new Intent(this, MySimpleOverlayService.class));
        startActivity(new Intent(this, ManageEmergencyContactsActivity.class));
    }

    private boolean isJobServiceOn() {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if (jobInfo.getId() == 1) {
                hasBeenScheduled = true ;
                break ;
            }
        }
        return hasBeenScheduled ;
    }

    public void panicButtonClicked(View view) {
        isInEmergency = true;
        emergencyCode = getAlphaNumericString(20);
        emergencyManager.edit().putBoolean("isInEmergency", true).putString("emergencyCode", emergencyCode).apply();
        emergencyReference = FirebaseDatabase.getInstance().getReference().child("Emergencies").child(emergencyCode);
        emergencyReference.child("isInEmergency").setValue(true);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LocationUpdate locationUpdate = new LocationUpdate();
                locationUpdate.setLatitude(location.getLatitude());
                locationUpdate.setLongitude(location.getLongitude());
                locationUpdate.setSpeed(location.getSpeed());
                locationUpdate.setAccuracy(location.getAccuracy());
                locationUpdate.setTime(location.getTime());
                FirebaseDatabase.getInstance().getReference().child("Emergencies").child(emergencyCode).child("location").setValue(locationUpdate);
                mLocationManager.removeUpdates(mLocationListener);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, CameraService1.class));
            startForegroundService(new Intent(this, BackgroundLocationService.class));
        } else {
            startService(new Intent(this, BackgroundLocationService.class));
            startService(new Intent(this, CameraService1.class));
        }
        startService(new Intent(this, SMSService.class));
        SmsManager smsManager = SmsManager.getDefault();
        for(int i=1;i<=5;i++)
        {
            String phoneNumber = emergencyManager.getString("emergencyContact"+i, null);
            if(phoneNumber == null)
                continue;
            smsManager.sendTextMessage(phoneNumber, null, getResources().getString(R.string.emergency_message).concat("https://safetyapp-123.000webhostapp.com/emergency.html?emergencyCode="+emergencyCode), null, null);
        }
//        smsManager.sendTextMessage("1234567890", null, "https://retailor11.000webhostapp.com/emergency.html?emergencyCode="+emergencyCode, null, null);
        Toast.makeText(this, "Panic button clicked!", Toast.LENGTH_SHORT).show();
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

    public void report(View view) {
    }

    public void logout(View view) {
        mAuth.signOut();
        startActivity(new Intent(getApplicationContext(), OnboardingActivity.class));
        finish();
    }

    /*@Override
    public void onImageCapture(@NonNull File imageFile) {
        stopCamera();
    }

    @Override
    public void onCameraError(int errorCode) {
        //Toast.makeText(this, "Error: "+errorCode, Toast.LENGTH_SHORT).show();
    }*/
}