package com.example.karlbuha.serviceme;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import DataContract.DataModels.CaseDetails;
import DataContract.GetTagsRequestContainer;
import DataContract.GetTagsReturnContainer;
import Helpers.AppIdentity;
import Helpers.MyPopupWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class UserNewUpdateCase extends Activity implements MyResultReceiver.Receiver{
    public static final String TAG_CHECK_BOXES = "TagCheckBoxes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_new_update_case);

        // Todo: Check before cast
        List<String> contactPref = (List<String>)AppIdentity.GetResource(this, AppIdentity.contactPref);

        try {
            if (contactPref.contains(getResources().getString(R.string.phone_check_box))) {
                CheckBox phoneCheckBox = (CheckBox) findViewById(R.id.phoneCheckBox);
                phoneCheckBox.setChecked(true);
            }

            if (contactPref.contains(getResources().getString(R.string.profile_chat_check_box))) {
                CheckBox phoneCheckBox = (CheckBox) findViewById(R.id.chatCheckBox);
                phoneCheckBox.setChecked(true);
            }

            if (contactPref.contains(getResources().getString(R.string.profile_email_check_box))) {
                CheckBox phoneCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
                phoneCheckBox.setChecked(true);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // Todo: Format the currency better.
    public void AddBudgetButtonOnClick(View view){
        Button budgetButton = (Button) findViewById(R.id.addBudgetButton);
        budgetButton.setVisibility(View.GONE);
        LinearLayout budgetLinearLayout = (LinearLayout) findViewById(R.id.budgetLinearLayout);
        budgetLinearLayout.setVisibility(View.VISIBLE);
    }

    public void AddMyAddressButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void AddAnotherAddressButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void NextForTagsButtonOnClick(View view){
        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        ArrayList<View> viewsToDelete = new ArrayList<>();
        tagsLinearLayout.findViewsWithText(viewsToDelete, TAG_CHECK_BOXES,View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

        for(View i : viewsToDelete){
            tagsLinearLayout.removeView(i);
        }

        // Todo: Move all this to later in the flow. Only request details is needed here.
        GetTagsRequestContainer getTagsRequestContainer = new GetTagsRequestContainer();
        getTagsRequestContainer.caseDetails = new CaseDetails();
        /*
        EditText titleEditText = (EditText)findViewById(R.id.casePageTitleEditText);
        String title = titleEditText.getText().toString();
        if(title.isEmpty()){
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.missing_title_popup_text));
            return;
        }

        getTagsRequestContainer.caseDetails.Title = title;

        getTagsRequestContainer.caseDetails.ContactPreference = new ArrayList<>();
        CheckBox phoneCheckBox = (CheckBox) findViewById(R.id.phoneCheckBox);
        if (phoneCheckBox.isChecked()) {
            getTagsRequestContainer.caseDetails.ContactPreference.add("Phone");
        }

        CheckBox chatCheckBox = (CheckBox) findViewById(R.id.chatCheckBox);
        if (chatCheckBox.isChecked()) {
            getTagsRequestContainer.caseDetails.ContactPreference.add("Chat");
        }

        CheckBox emailCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
        if(emailCheckBox.isChecked()) {
            String emailAddress = (String)AppIdentity.GetResource(this, AppIdentity.emailAddress);
            if (emailAddress == null || emailAddress.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.missing_email_address_text));
                return;
            }

            getTagsRequestContainer.caseDetails.ContactPreference.add("Email");
        }*/

        EditText requestDetailsEditText = (EditText)findViewById(R.id.requestDetailsEditText);
        String requestDetails = requestDetailsEditText.getText().toString();
        if(requestDetails.isEmpty()){
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.missing_request_details_text));
            return;
        }

        getTagsRequestContainer.caseDetails.RequestDetails = requestDetails;

//        EditText budgetEditText = (EditText)findViewById(R.id.budgetEditText);
//        String budget = budgetEditText.getText().toString();
//        if(!budget.isEmpty()){
//            getTagsRequestContainer.caseDetails.Budget = Integer.parseInt(budget);
//        }

        String jsonString = new Gson().toJson(getTagsRequestContainer);
        MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("successCode", "3");
        intent.putExtra("apiCall", "GetTags");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    public void AddTagsButtonOnClick(View view){
        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        AutoCompleteTextView tagAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
        List<String> tagsToAdd = new ArrayList<>();
        tagsToAdd.add(0, tagAutoCompleteTextView.getText().toString());
        CreateTags(tagsToAdd, tagsLinearLayout);
    }

    public void CancelButtonOnClick(View view){
        Intent intent = new Intent(this, UserCaseOverview.class);
        startActivity(intent);
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
                GetTagsReturnContainer getTagsReturnContainer = new Gson().fromJson(result, GetTagsReturnContainer.class);

                if(getTagsReturnContainer.returnCode.equals("101")){
                    LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
                    tagsLinearLayout.setVisibility(View.VISIBLE);
                    this.CreateTags(getTagsReturnContainer.tags, tagsLinearLayout);

                    // Todo: When tags grow, change how this is done.
                    AutoCompleteTextView tagsAutoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.tagAutoCompleteTextView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getTagsReturnContainer.allTags);
                    tagsAutoCompleteTextView.setAdapter(adapter);

                    findViewById(R.id.scrollView).post(new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView)findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }

                break;
        }
    }

    private void CreateTags(List<String> tags, LinearLayout tagsLinearLayout) {
        for (String tag : tags) {
            RelativeLayout.LayoutParams tagCheckboxLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            CheckBox tagCheckBox = new CheckBox(this);
            tagCheckBox.setText(tag);
            tagCheckBox.setChecked(true);
            tagCheckBox.setContentDescription(TAG_CHECK_BOXES);
            tagsLinearLayout.addView(tagCheckBox, 2, tagCheckboxLayoutParams);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_new_update_case, menu);
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
