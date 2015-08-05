package com.example.karlbuha.serviceme;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;

import com.google.gson.Gson;

import java.lang.String;

import DataContract.CreateNewUserRequestContainer;
import DataContract.CreateNewUserReturnContainer;


public class NewUser extends Activity implements MyResultReceiver.Receiver {

    public MyResultReceiver myResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Button sendVerificationCodeButton = (Button) findViewById(R.id.sendVerificationCodeButton);
        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationCodeButtonOnClick();
            }
        });
    }

    private void SendVerificationCodeButtonOnClick() {
        EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
        String phoneNumber = phoneNumberEditText.getText().toString();
        TextView phoneNumberTextView = (TextView) findViewById(R.id.phoneNumberTextView);
        // Todo: Need additional verifications for phone number
        if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            phoneNumberTextView.setTextColor(getResources().getColor(R.color.red));
            return;
        } else {
            phoneNumberTextView.setTextColor(getResources().getColor(R.color.white));
        }

        CreateNewUserRequestContainer createNewUserRequestContainer = new CreateNewUserRequestContainer();
        createNewUserRequestContainer.deviceType = "Phone";
        createNewUserRequestContainer.phoneNumber = phoneNumber;
        String jsonString = new Gson().toJson(createNewUserRequestContainer);

        myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("apiCall", "CreateUser");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                //show progress
                break;
            case 2:
                String result = resultData.getString("results");
                CreateNewUserReturnContainer createNewUserReturnContainer = new Gson().fromJson(result, CreateNewUserReturnContainer.class);
                String test = createNewUserReturnContainer.userId;
                break;
            case 3:
                // handle the error;
                break;
        }
    }

    public void onPause() {
        myResultReceiver.setReceiver(null); // clear receiver so no leaks.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
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
