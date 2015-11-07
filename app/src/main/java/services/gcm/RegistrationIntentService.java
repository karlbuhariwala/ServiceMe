package services.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import java.io.IOException;

import DataContract.AddNotificationInfoToUserRequestContainer;
import DataContract.GetAgentsForAutoCompleteReturnContainer;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;

public class RegistrationIntentService extends IntentService implements MyResultReceiver.Receiver {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken("1014904004608",
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        AddNotificationInfoToUserRequestContainer addNotificationInfoToUserRequestContainer = new AddNotificationInfoToUserRequestContainer();
        addNotificationInfoToUserRequestContainer.userId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        addNotificationInfoToUserRequestContainer.notificationToken = token;

        String jsonString = new Gson().toJson(addNotificationInfoToUserRequestContainer);

        MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("showProgress", "false");
        intent.putExtra("successCode", "3");
        intent.putExtra("apiCall", "UpdateUserNotificationInfo");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String result;
        switch (resultCode) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                result = resultData.getString("results");
                GetAgentsForAutoCompleteReturnContainer getAgentsForAutoCompleteReturnContainer = new Gson().fromJson(result, GetAgentsForAutoCompleteReturnContainer.class);

                break;
        }
    }
}
