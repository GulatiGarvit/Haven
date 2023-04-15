package in.safety.safety;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.logging.Logger;

public class SMSService extends Service {
    private SharedPreferences emergencyManager;
    public SMSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void sendSMS(String phoneNumber, String message)
    {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Log.i("SMS", "sent");
    }

    @Override
    public void onCreate() {
        Log.i("SMS", "started");
        emergencyManager = getSharedPreferences("emergency_details", MODE_PRIVATE);
        String emergencyCode = emergencyManager.getString("emergencyCode", null);
        for(int i=1;i<=5;i++)
        {
            String phoneNumber = emergencyManager.getString("emergencyContact"+i, null);
            if(phoneNumber == null)
                continue;
            sendSMS(phoneNumber, getResources().getString(R.string.emergency_message).concat("https://safetyapp-123.000webhostapp.com/emergency.html?emergencyCode="+emergencyCode));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
