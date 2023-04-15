package in.safety.safety;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class FcmNotifService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        ParseQuery<ParseObject> query = new ParseQuery<>("NotifKeys");
        try {
            ParseObject object = query.whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()).getFirst();
            object.put("fcmKey", refreshedToken);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
