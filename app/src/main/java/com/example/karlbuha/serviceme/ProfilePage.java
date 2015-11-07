package com.example.karlbuha.serviceme;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;

import java.util.ArrayList;

import DataContract.CreateUpdateProfileRequestContainer;
import DataContract.CreateUpdateProfileReturnContainer;
import DataContract.DataModels.UserProfile;
import Helpers.BaseActivity;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class ProfilePage extends BaseActivity implements MyResultReceiver.Receiver {
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
        LinearLayout agentSelectionLinearLayout = (LinearLayout) findViewById(R.id.agentSelectionLinearLayout);
        agentSelectionLinearLayout.setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);

        RadioButton agentRadioButton = (RadioButton) findViewById(R.id.agentRadioButton);
        agentRadioButton.setChecked(true);

        RadioButton agentPageRadioButton = (RadioButton) findViewById(R.id.agentPageRadioButton);
        agentPageRadioButton.setChecked(true);
    }

    public void DoneButtonOnClick(View view) {
        UserProfile userProfile = new UserProfile();
        userProfile.UserId  = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        try {
            userProfile.IsVerified = Boolean.parseBoolean(new AppIdentityDb(this).GetResource(AppIdentityDb.verified));
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
        else {
            new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.userName, name);
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
            new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.emailAddress, emailAddress);
        }

        new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.contactPref, new Gson().toJson(userProfile.ContactPreference));

        RadioGroup beAnAgentRadioGroup = (RadioGroup) findViewById(R.id.beAnAgentRadioGroup);
        int checkedId = beAnAgentRadioGroup.getCheckedRadioButtonId();
        int childId = beAnAgentRadioGroup.indexOfChild(findViewById(checkedId));
        switch (childId){
            case 0:
                userProfile.LandingPage = this.GetLandingPage();
                break;
            case 1:
                userProfile.IsAgent = true;
                userProfile.LandingPage = this.GetLandingPage();
                break;
            case 2:
                userProfile.IsAgent = true;
                userProfile.IsManager = true;
                userProfile.LandingPage = this.GetLandingPage();
                break;
            default:
                break;
        }

        new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.landingPage, Integer.toString(userProfile.LandingPage));

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

    private int GetLandingPage() {
        RadioGroup landingPagePrefRadioGroup = (RadioGroup) findViewById(R.id.landingPagePrefRadioGroup);
        int checkedId = landingPagePrefRadioGroup.getCheckedRadioButtonId();
        return landingPagePrefRadioGroup.indexOfChild(findViewById(checkedId));
    }

    public void NoneRadioButtonClick(View view) {
        RadioButton clientPageRadioButton = (RadioButton) findViewById(R.id.clientPageRadioButton);
        clientPageRadioButton.setChecked(true);
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
                CreateUpdateProfileReturnContainer createUpdateProfileReturnContainer = new Gson().fromJson(result, CreateUpdateProfileReturnContainer.class);

                if(createUpdateProfileReturnContainer.returnCode.equals("101")){
                    if(createUpdateProfileReturnContainer.isAgent) {
                        Intent intent = new Intent(this, AgentTagSetup.class);
                        startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(this, UserCaseOverview.class);
                        startActivity(intent);
                    }
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
