package com.example.karlbuha.serviceme;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;

import DataContract.DataModels.ChatMessage;
import DataContract.GetChatRoomDetailsRequestContainer;
import DataContract.GetChatRoomDetailsReturnContainer;
import Helpers.BaseActivity;
import Helpers.Constants;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import Helpers.dbHelper.ChatsDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;

public class UserChatRoom extends BaseActivity implements MyResultReceiver.Receiver {
    private static GetChatRoomDetailsReturnContainer getChatRoomDetailsReturnContainerCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat_room);

        GetChatRoomDetailsRequestContainer getChatRoomDetailsRequestContainer = new GetChatRoomDetailsRequestContainer();
        Intent intent = getIntent();
        getChatRoomDetailsRequestContainer.caseId = intent.getStringExtra(Constants.caseIdString);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(intent.getStringExtra(Constants.chatTitle));

        if(intent.getStringExtra(Constants.typeOfUser).equals("user")) {
            // Todo: Try to set the theme here rather than the color
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.purple)));
        }

        String jsonString = new Gson().toJson(getChatRoomDetailsRequestContainer);
        ApiCallService.CallService(this, true, "GetChatRoomDetails", jsonString, "3");
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

                new ChatsDb(this).insertChatMessage(UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId, "1234", "Text", "This is a test message");
                new ChatsDb(this).insertChatMessage(UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId, "12345", "Text", "This is a test message that is very very long and will continue forever");
                List<ChatMessage> messages = new ChatsDb(this).getCharMessages(UserChatRoom.getChatRoomDetailsReturnContainerCache.caseId, 0, 10, UserChatRoom.getChatRoomDetailsReturnContainerCache.userIdNamePairs);
                this.InsertMessages(messages, false);
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

            if(message.type.equals("Text")){
                TextView messageTextView = new TextView(this);
                messageTextView.setSingleLine(false);
                messageTextView.setText(message.messageData);
                secondLevelLinearLayout.addView(messageTextView, linearLayoutParams);
            }

            if(message.senderId.equals("1234") ){
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
}
