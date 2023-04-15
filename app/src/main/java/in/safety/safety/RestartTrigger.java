package in.safety.safety;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartTrigger extends BroadcastReceiver {
    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        Intent intent1 = new Intent(context, BackgroundLocationService.class);
        context.startService(intent1);
    }
}
