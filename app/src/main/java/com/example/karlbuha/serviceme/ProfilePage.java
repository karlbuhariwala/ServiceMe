package com.example.karlbuha.serviceme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;

import java.util.ArrayList;

import DataContract.CreateUpdateProfileRequestContainer;
import DataContract.CreateUpdateProfileReturnContainer;
import DataContract.DataModels.UserProfile;
import Helpers.AppIdentity;
import Helpers.MyPopupWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class ProfilePage extends Activity implements MyResultReceiver.Receiver {
    public MyResultReceiver myResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
    }

    public void AddEmailButtonOnClick(View view){
        Button addEmailButton = (Button)findViewById(R.id.addEmailAddressButton);
        LinearLayout emailLinearLayout = (LinearLayout) findViewById(R.id.emailLinearLayout);
        addEmailButton.setVisibility(View.GONE);
        emailLinearLayout.setVisibility(View.VISIBLE);
    }

    public void AddAddressButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void AddPaymentDetailsButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void BeAAgentButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void DoneButtonOnClick(View view) {
        UserProfile userProfile = new UserProfile();
        userProfile.UserId  = (String)AppIdentity.GetResource(this, AppIdentity.userId);
        try {
            userProfile.IsVerified = (Boolean) AppIdentity.GetResource(this, AppIdentity.verified);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        EditText nameEditText = (EditText) findViewById(R.id.profileNameEditText);
        String name = nameEditText.getText().toString();
        if (name.isEmpty()) {
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.name_is_mandatory_text));
            return;
        }

        userProfile.Name = name;
        userProfile.ContactPreference = new ArrayList<>();

        CheckBox phoneCheckBox = (CheckBox) findViewById(R.id.phoneCheckBox);
        if (phoneCheckBox.isChecked()) {
            userProfile.ContactPreference.add("Phone");
        }

        CheckBox chatCheckBox = (CheckBox) findViewById(R.id.chatCheckBox);
        if (chatCheckBox.isChecked()) {
            userProfile.ContactPreference.add("Chat");
        }

        CheckBox emailCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
        if(emailCheckBox.isChecked()) {
            EditText emailEditText = (EditText) findViewById(R.id.emailEditText);
            String emailAddress = emailEditText.getText().toString();
            if (emailAddress.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.invalid_email_address));
                return;
            }

            userProfile.ContactPreference.add("Email");
            userProfile.EmailAddress = emailAddress;
            AppIdentity.UpdateResource(this, AppIdentity.emailAddress, emailAddress);
        }

        AppIdentity.UpdateResource(this, AppIdentity.contactPref, userProfile.ContactPreference);

        CreateUpdateProfileRequestContainer createUpdateProfileRequestContainer = new CreateUpdateProfileRequestContainer();
        createUpdateProfileRequestContainer.userProfile = userProfile;
        String jsonString = new Gson().toJson(createUpdateProfileRequestContainer);

        myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("successCode", "3");
        intent.putExtra("apiCall", "CreateUpdateProfile");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        String result;
        switch (resultCode) {
            case 1:
                //show progress
                break;
            case 2:
                // handle the error
                break;
            case 3:
                result = resultData.getString("results");
                CreateUpdateProfileReturnContainer createNewUserReturnContainer = new Gson().fromJson(result, CreateUpdateProfileReturnContainer.class);

                if(createNewUserReturnContainer.returnCode.equals("101")){
                    Intent intent = new Intent(this, UserCaseOverview.class);
                    startActivity(intent);
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_page, menu);
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
