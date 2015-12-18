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

import java.text.MessageFormat;
import java.util.ArrayList;

import DataContract.CreateUpdateProfileRequestContainer;
import DataContract.CreateUpdateProfileReturnContainer;
import DataContract.DataModels.AddressContainer;
import DataContract.DataModels.UserProfile;
import DataContract.GetProfileRequestContainer;
import DataContract.GetProfileReturnContainer;
import Helpers.BaseActivity;
import Helpers.Interfaces.AddressPopupCallback;
import Helpers.PopupHelpers.AddressPopupWindow;
import Helpers.PopupHelpers.MyPopupWindow;
import Helpers.PopupHelpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class ProfilePage extends BaseActivity implements MyResultReceiver.Receiver, AddressPopupCallback {
    public MyResultReceiver myResultReceiver;
    private AddressContainer addressContainerCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        GetProfileRequestContainer getProfileRequestContainer = new GetProfileRequestContainer();
        getProfileRequestContainer.userId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        String jsonString = new Gson().toJson(getProfileRequestContainer);

        ApiCallService.CallService(this, true, "GetProfile", jsonString, "4");
    }

    public void AddEmailButtonOnClick(View view){
        Button addEmailButton = (Button)findViewById(R.id.addEmailAddressButton);
        LinearLayout emailLinearLayout = (LinearLayout) findViewById(R.id.emailLinearLayout);
        addEmailButton.setVisibility(View.GONE);
        emailLinearLayout.setVisibility(View.VISIBLE);
    }

    public void AddAddressButtonOnClick(View view){
        new AddressPopupWindow().InitiatePopupWindow(this, this.addressContainerCache);
    }

    public void ShowAddress(AddressContainer addressContainer) {
        this.addressContainerCache = addressContainer;
        Button addAddressButton = (Button) findViewById(R.id.addAddressButton);
        addAddressButton.setVisibility(View.GONE);

        Button editAddressButton = (Button) findViewById(R.id.editAddressButton);
        String address = "";
        if(addressContainer.AddressLine1 != null && !addressContainer.AddressLine1.equals("")) {
            address += addressContainer.AddressLine1 + "\n";
        }

        if(addressContainer.AddressLine2 != null && !addressContainer.AddressLine2.equals("")) {
            address += addressContainer.AddressLine2 + "\n";
        }

        editAddressButton.setText(MessageFormat.format("{0}{1}, {2}\n{3}", address, addressContainer.City, addressContainer.PostalCode, addressContainer.Country));
        LinearLayout addressLinearLayout = (LinearLayout) findViewById(R.id.addressLinearLayout);
        addressLinearLayout.setVisibility(View.VISIBLE);
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

        if (this.addressContainerCache == null) {
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.incomplete_address));
            return;
        }

        userProfile.Address = addressContainerCache;

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
        new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.isAgent, Boolean.toString(userProfile.IsAgent));
        new AppIdentityDb(this).InsertUpdateResource(AppIdentityDb.isManager, Boolean.toString(userProfile.IsManager));

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
            case 4:
                result = resultData.getString("results");
                GetProfileReturnContainer getProfileReturnContrainer = new Gson().fromJson(result, GetProfileReturnContainer.class);

                if(getProfileReturnContrainer.returnCode.equals("101")) {
                    EditText profileNameEditText = (EditText) findViewById(R.id.profileNameEditText);
                    profileNameEditText.setText(getProfileReturnContrainer.userInfo.Name);

                    if(getProfileReturnContrainer.userInfo.ContactPreference.contains("Phone")) {
                        CheckBox phoneCheckBox = (CheckBox) findViewById(R.id.phoneCheckBox);
                        phoneCheckBox.setChecked(true);
                    }

                    if(getProfileReturnContrainer.userInfo.ContactPreference.contains("Chat")) {
                        CheckBox chatCheckBox = (CheckBox) findViewById(R.id.chatCheckBox);
                        chatCheckBox.setChecked(true);
                    }

                    if(getProfileReturnContrainer.userInfo.ContactPreference.contains("Email")) {
                        CheckBox emailCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
                        emailCheckBox.setChecked(true);
                    }

                    if(getProfileReturnContrainer.userInfo.Address != null){
                        this.ShowAddress(getProfileReturnContrainer.userInfo.Address);
                    }

                    if(!getProfileReturnContrainer.userInfo.EmailAddress.equals("")) {
                        LinearLayout emailLinearLayout = (LinearLayout) findViewById(R.id.emailLinearLayout);
                        emailLinearLayout.setVisibility(View.VISIBLE);

                        EditText emailEditText = (EditText) findViewById(R.id.emailEditText);
                        emailEditText.setText(getProfileReturnContrainer.userInfo.EmailAddress);

                        Button addEmailAddressButton = (Button) findViewById(R.id.addEmailAddressButton);
                        addEmailAddressButton.setVisibility(View.GONE);
                    }

                    if(getProfileReturnContrainer.userInfo.IsAgent || getProfileReturnContrainer.userInfo.IsManager) {
                        LinearLayout agentSelectionLinearLayout = (LinearLayout) findViewById(R.id.agentSelectionLinearLayout);
                        agentSelectionLinearLayout.setVisibility(View.VISIBLE);

                        Button beAnAgentButton = (Button) findViewById(R.id.beAnAgentButton);
                        beAnAgentButton.setVisibility(View.GONE);

                        if (getProfileReturnContrainer.userInfo.IsAgent) {
                            RadioButton agentRadioButton = (RadioButton) findViewById(R.id.agentRadioButton);
                            agentRadioButton.setChecked(true);
                        }
                        else {
                            RadioButton agentManagerRadioButton = (RadioButton) findViewById(R.id.agentManagerRadioButton);
                            agentManagerRadioButton.setChecked(true);
                        }

                        RadioGroup landingPagePrefRadioGroup = (RadioGroup) findViewById(R.id.landingPagePrefRadioGroup);
                        ((RadioButton) landingPagePrefRadioGroup.getChildAt(getProfileReturnContrainer.userInfo.LandingPage)).setChecked(true);
                    }
                }
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
