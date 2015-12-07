package com.example.karlbuha.serviceme;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import DataContract.DataModels.ChatMessage;
import DataContract.GetChatRoomDetailsRequestContainer;
import DataContract.GetChatRoomDetailsReturnContainer;
import DataContract.SendChatMessageRequestContainer;
import Helpers.BaseActivity;
import Helpers.Constants;
import Helpers.PopupHelpers.MyPopupWindow;
import Helpers.PopupHelpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import Helpers.dbHelper.ChatsDb;
import services.gcm.MyGcmListenerService;
import webApi.ApiCallService;
import webApi.MyResultReceiver;

public class UserChatRoom extends BaseActivity implements MyResultReceiver.Receiver {
    public static GetChatRoomDetailsReturnContainer getChatRoomDetailsReturnContainerCache;
    private static BroadcastReceiver receiver;
    private static String senderId;
    private static String senderName;
    public static boolean broadcastReceiverIsSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat_room);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processNewMessage(context, intent);
            }
        };

        UserChatRoom.senderId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        UserChatRoom.senderName = new AppIdentityDb(this).GetResource(AppIdentityDb.userName);

        GetChatRoomDetailsRequestContainer getChatRoomDetailsRequestContainer = new GetChatRoomDetailsRequestContainer();
        Intent intent = getIntent();
        getChatRoomDetailsRequestContainer.caseId = intent.getStringExtra(Constants.caseIdString);
        getChatRoomDetailsRequestContainer.userId = UserChatRoom.senderId;
        getChatRoomDetailsRequestContainer.agentId = intent.getStringExtra(Constants.agentIdString);

        String jsonString = new Gson().toJson(getChatRoomDetailsRequestContainer);
        ApiCallService.CallService(this, true, "GetChatRoomDetails", jsonString, "3");

        String newMessage = new AppIdentityDb(this).GetResource(AppIdentityDb.newChatMessage + getChatRoomDetailsRequestContainer.caseId);
        if (newMessage.equals("") || Integer.parseInt(newMessage) == 1) {
            new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.newChatMessage + getChatRoomDetailsRequestContainer.caseId, "0");
        }
    }

    private void processNewMessage(Context context, Intent intent) {
        String message = intent.getStringExtra(Constants.messageString);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.senderId = intent.getStringExtra(Constants.senderIdString);
        chatMessage.senderName = intent.getStringExtra(Constants.senderNameString);
        chatMessage.timestamp = new Date(System.currentTimeMillis());
        chatMessage.type = 0;
        chatMessage.messageData = message;

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(chatMessage);
        this.InsertMessages(messages, true);
    }

    public void SendMessageButton(View view) {
        EditText newMessageEditText = (EditText) findViewById(R.id.newMessageEditText);
        String message = newMessageEditText.getText().toString();
        newMessageEditText.setText("");
        SendChatMessageRequestContainer sendChatMessageRequestContainer = new SendChatMessageRequestContainer();
        sendChatMessageRequestContainer.message = message;
        sendChatMessageRequestContainer.typeOfMessage = 0;
        sendChatMessageRequestContainer.caseId = getChatRoomDetailsReturnContainerCache.caseId;
        sendChatMessageRequestContainer.participantsInfo = getChatRoomDetailsReturnContainerCache.participantsInfo;
        sendChatMessageRequestContainer.senderId = UserChatRoom.senderId;
        sendChatMessageRequestContainer.senderName = UserChatRoom.senderName;

        String jsonString = new Gson().toJson(sendChatMessageRequestContainer);
        ApiCallService.CallService(this, false, "SendChatMessage", jsonString, "4");

        new ChatsDb(this).insertChatMessage(UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId, UserChatRoom.senderId, 0, message);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.senderId = UserChatRoom.senderId;
        chatMessage.senderName = UserChatRoom.senderName;
        chatMessage.timestamp = new Date(System.currentTimeMillis());
        chatMessage.type = 0;
        chatMessage.messageData = message;

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(chatMessage);
        this.InsertMessages(messages, true);
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        String result;
        switch (resultCode) {
            case 1:
                MyProgressWindow.ShowProgressWindow(this);
                break;
            case 2:
                new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.generic_error_text));
                break;
            case 3:
                result = resultData.getString("results");
                UserChatRoom.getChatRoomDetailsReturnContainerCache = new Gson().fromJson(result, GetChatRoomDetailsReturnContainer.class);

                ActionBar actionBar = getActionBar();
                if(actionBar != null) {
                    actionBar.setTitle(getChatRoomDetailsReturnContainerCache.chatRoomTitle);
                }

                if(getChatRoomDetailsReturnContainerCache.userType == 1) {
                    // Todo: Try to set the theme here rather than the color
                    actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));
                }

//                UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId = "testCaseId";
//                new ChatsDb(this).insertChatMessage(UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId, "1234", 0, "This is a test message");
//                new ChatsDb(this).insertChatMessage(UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId, "12345", 0, "This is a test message that is very very long and will continue forever");
                List<ChatMessage> messages = new ChatsDb(this).getCharMessages(UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId, 0, 100, UserChatRoom.getChatRoomDetailsReturnContainerCache.userIdNamePairs);
                this.InsertMessages(messages, false);

                ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView) findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                break;
            case 4:
                break;
        }
    }

    private void InsertMessages(List<ChatMessage> messages, boolean newMessage){
        for (ChatMessage message : messages) {
            LinearLayout mainLinearLayout = (LinearLayout) findViewById(R.id.chatMessagesLinearLayout);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            LinearLayout secondLevelLinearLayout = new LinearLayout(this);
            secondLevelLinearLayout.setOrientation(LinearLayout.VERTICAL);
            secondLevelLinearLayout.setBackgroundColor(getResources().getColor(R.color.link_blue));
            int px10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            secondLevelLinearLayout.setPadding(px10, px10, px10, px10);
            LinearLayout.LayoutParams secondLevelLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;
            secondLevelLinearLayoutParams.width = (width * 2) / 3;
            secondLevelLinearLayoutParams.setMargins(px10, px10, px10, px10);
            mainLinearLayout.addView(secondLevelLinearLayout, secondLevelLinearLayoutParams);

            LinearLayout nameTimestampLinearLayout = new LinearLayout(this);
            nameTimestampLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            nameTimestampLinearLayout.setWeightSum(3.0f);
            LinearLayout.LayoutParams nameTimestampLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            secondLevelLinearLayout.addView(nameTimestampLinearLayout, nameTimestampLinearLayoutParams);

            TextView nameTextView = new TextView(this);
            nameTextView.setText(message.senderName);
            nameTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
            LinearLayout.LayoutParams nameTextViewLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f);
            nameTimestampLinearLayout.addView(nameTextView, nameTextViewLinearLayoutParams);

            TextView timestampTextView = new TextView(this);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd HH:mm");
            timestampTextView.setText(dateFormat.format(message.timestamp));
            timestampTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
            LinearLayout.LayoutParams timestampTextViewLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            nameTimestampLinearLayout.addView(timestampTextView, timestampTextViewLinearLayoutParams);

            if(message.type == 0){
                TextView messageTextView = new TextView(this);
                messageTextView.setSingleLine(false);
                messageTextView.setText(message.messageData);
                secondLevelLinearLayout.addView(messageTextView, linearLayoutParams);
            }

            if(message.senderId.equals(UserChatRoom.senderId) ){
                secondLevelLinearLayoutParams.gravity = Gravity.RIGHT;
                secondLevelLinearLayout.setBackgroundColor(getResources().getColor(R.color.dark_blue));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MyGcmListenerService.MESSAGE_RESULT)
        );
        UserChatRoom.broadcastReceiverIsSet = true;
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
        UserChatRoom.broadcastReceiverIsSet = false;
    }
}
