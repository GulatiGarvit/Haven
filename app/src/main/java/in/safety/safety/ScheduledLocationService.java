package in.safety.safety;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class ScheduledLocationService extends JobService {
    private static final long LOCATION_REFRESH_TIME = 0;
    private static final float LOCATION_REFRESH_DISTANCE = 0F;
    private ParseObject userDataObject;
    private SharedPreferences loginManager;
    private static final String TAG = "JobSchedulerService";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager notificationManager;
    private String CHANNEL_ID="MYCHANNEL";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.v("SchedulerLoc", "started");
        notification();
        loginManager = getSharedPreferences("login_manager", MODE_PRIVATE);
        if (loginManager.getString("uid", null) == null)
            return false;
        Log.v("uid", loginManager.getString("uid", "null"));
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Log.v("SchedulerLoc", "locationChanged");
                userDataObject = new ParseObject("UserData");
                userDataObject.put("uid",loginManager.getString("uid", "uid"));
                userDataObject.put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                userDataObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null)
                            Log.v("SchedErr", e.toString());
                        mLocationManager.removeUpdates(mLocationListener);
                        jobFinished(params, false);
                    }
                });
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
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notification() {
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,"name",NotificationManager.IMPORTANCE_LOW);
        PendingIntent pendingIntent= PendingIntent.getActivity(getApplicationContext(),1,intent,0);
        Notification notification=new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentText("Updating location")
                .setContentTitle("Safety is updating your location in the background")
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.sym_def_app_icon,"Title",pendingIntent)
                .setChannelId(CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .build();

        notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1,notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStopJob(JobParameters params) {
        notificationManager.deleteNotificationChannel(CHANNEL_ID);
        return true;
    }
}
