package services.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.example.karlbuha.serviceme.R;
import com.example.karlbuha.serviceme.UserCaseOverview;
import com.example.karlbuha.serviceme.UserChatRoom;
import com.google.android.gms.gcm.GcmListenerService;

import Helpers.Constants;
import Helpers.dbHelper.AppIdentityDb;
import Helpers.dbHelper.ChatsDb;

public class MyGcmListenerService extends GcmListenerService {
    static final public String MESSAGE_RESULT = "com.example.karlbuha.serviceme.services.gcm";
    private static LocalBroadcastManager broadcaster;

    @Override
    public void onCreate(){
        super.onCreate();

        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String senderId = data.getString("senderId");
        String senderName = data.getString("senderName");
        String caseId = data.getString("caseId");
        caseId = "testCaseId";
        String messageId = data.getString("messageId");
        int typeOfMessage = data.getInt("typeOfMessage");
        new ChatsDb(this).insertChatMessage(caseId, senderId, typeOfMessage, message);

        if(UserChatRoom.broadcastReceiverIsSet && UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId.equals(caseId)) {
            Intent intent = new Intent(MyGcmListenerService.MESSAGE_RESULT);
            if(message != null) {
                intent.putExtra(Constants.messageString, message);
                intent.putExtra(Constants.senderNameString, senderName);
                intent.putExtra(Constants.senderIdString, senderId);
                intent.putExtra(Constants.typeOfMessageString, typeOfMessage);
                intent.putExtra(Constants.caseIdString, caseId);
            }

            broadcaster.sendBroadcast(intent);
        }
        else {
            new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.newChatMessage + caseId, "1");
            sendNotification(message, senderName, caseId);
        }
    }

    private void sendNotification(String message, String senderName, String caseId) {
        Intent intent = new Intent(this, UserChatRoom.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.caseIdString, caseId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(senderName)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1234, notificationBuilder.build());
    }
}
