package com.example.karlbuha.serviceme;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;

import java.lang.String;

import DataContract.CreateNewUserRequestContainer;
import DataContract.CreateNewUserReturnContainer;
import DataContract.DeviceValidationRequestContainer;
import DataContract.DeviceValidationReturnContainer;
import Helpers.BaseActivity;
import Helpers.dbHelper.AppIdentityDb;
import services.gcm.RegistrationIntentService;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;

public class NewUser extends BaseActivity implements MyResultReceiver.Receiver {

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

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                //apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                  //      .show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private void SendVerificationCodeButtonOnClick() {
        EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
        String phoneNumber = phoneNumberEditText.getText().toString();
        TextView phoneNumberTextView = (TextView) findViewById(R.id.phoneNumberTextView);
        // Todo: Need additional verifications for phone number
        if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.invalid_phone_number_text));
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
        intent.putExtra("successCode", "3");
        intent.putExtra("apiCall", "CreateUser");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    public void VerifyCodeButtonOnClick(View view) {
        EditText verificationCodeEditText = (EditText) findViewById(R.id.verificationCodeEditText);
        String verificationCode = verificationCodeEditText.getText().toString();
        if(verificationCode.length() < 4){
            return;
        }

        DeviceValidationRequestContainer deviceValidationRequestContainer = new DeviceValidationRequestContainer();
        deviceValidationRequestContainer.userId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        deviceValidationRequestContainer.validationCode = verificationCode;
        String jsonString = new Gson().toJson(deviceValidationRequestContainer);

        myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("successCode", "4");
        intent.putExtra("apiCall", "ValidateDevice");
        intent.putExtra("data", jsonString);
        startService(intent);
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
                CreateNewUserReturnContainer createNewUserReturnContainer = new Gson().fromJson(result, CreateNewUserReturnContainer.class);
                new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.userId, createNewUserReturnContainer.userId);

                //Show view
                LinearLayout verifyCodeLinearLayout = (LinearLayout)findViewById(R.id.verifyCodeLinearLayout);
                verifyCodeLinearLayout.setVisibility(View.VISIBLE);

                //Disable upper text
                EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
                phoneNumberEditText.setEnabled(false);
                Button sendVerificationCodeButton = (Button) findViewById(R.id.sendVerificationCodeButton);
                sendVerificationCodeButton.setEnabled(false);

                if (checkPlayServices() || true) {
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(this, RegistrationIntentService.class);
                    startService(intent);
                }
                break;
            case 4:
                result = resultData.getString("results");
                DeviceValidationReturnContainer deviceValidationReturnContainer = new Gson().fromJson(result, DeviceValidationReturnContainer.class);
                if(deviceValidationReturnContainer.returnCode.equals("101")) {
                    new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.verified, new Boolean(true).toString());
                    Intent intent = new Intent(this, ProfilePage.class);
                    startActivity(intent);
                }
                else{
                    // Todo: Show the different error messages on validation failure
                    new MyPopupWindow().InitiatePopupWindow(this, "Error!");
                    EditText validationCodeEditText = (EditText) findViewById(R.id.verificationCodeEditText);
                    validationCodeEditText.setText("");
                }

                break;
        }
    }

    public void onPause() {
        myResultReceiver.setReceiver(null); // clear receiver so no leaks.
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
//                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
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
