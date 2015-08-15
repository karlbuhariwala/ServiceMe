package com.example.karlbuha.serviceme;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.List;

import DataContract.DataModels.CaseDetails;
import DataContract.GetRecommendedAgentsRequestContainer;
import DataContract.GetRecommendedAgentsReturnContainer;
import DataContract.GetTagsForAutoCompleteRequestContainer;
import DataContract.GetTagsForAutoCompleteReturnContainer;
import DataContract.GetTagsRequestContainer;
import DataContract.GetTagsReturnContainer;
import Helpers.AppIdentity;
import Helpers.MyPopupWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class UserNewUpdateCase extends Activity implements MyResultReceiver.Receiver {
    private static final String TAG_CHECK_BOXES = "TagCheckBoxes";
    private static List<String> allTagsCache;
    private static GetRecommendedAgentsRequestContainer getRecommendedAgentsRequestContainer;
    private static String autoCompleteSuggestString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_new_update_case);

        UserNewUpdateCase.autoCompleteSuggestString = "";
        final AutoCompleteTextView tagsAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
        tagsAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tagsAutoCompleteTextView.isPerformingCompletion()
                        || s.length() < 3
                        || UserNewUpdateCase.autoCompleteSuggestString.equals(s.subSequence(0, 2).toString())) {
                    return;
                }

                UserNewUpdateCase.autoCompleteSuggestString = s.subSequence(0, 2).toString();
                CallAutoComplete(UserNewUpdateCase.autoCompleteSuggestString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do nothing
            }
        });
        List<String> contactPref;

        Intent intent = getIntent();
        if (intent.getStringExtra("caseInfo") != null) {
            String jsonString = intent.getStringExtra("caseInfo");
            UserNewUpdateCase.getRecommendedAgentsRequestContainer = new Gson().fromJson(jsonString, GetRecommendedAgentsRequestContainer.class);
            contactPref = UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.ContactPreference;

            EditText titleEditText = (EditText) findViewById(R.id.casePageTitleEditText);
            titleEditText.setText(UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Title);

            EditText requestDetailsEditText = (EditText) findViewById(R.id.requestDetailsEditText);
            requestDetailsEditText.setText(UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.RequestDetails);

            if(UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Budget != 0) {
                this.AddBudgetButtonOnClick(null);
                EditText budgetEditText = (EditText) findViewById(R.id.budgetEditText);
                budgetEditText.setText(Integer.toString(UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Budget));
            }

            this.CreateTags(UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Tags);
        } else {
            // Todo: Check before cast
            contactPref = (List<String>) AppIdentity.GetResource(this, AppIdentity.contactPref);
        }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Todo: Format the currency better.
    public void AddBudgetButtonOnClick(View view) {
        Button budgetButton = (Button) findViewById(R.id.addBudgetButton);
        budgetButton.setVisibility(View.GONE);
        LinearLayout budgetLinearLayout = (LinearLayout) findViewById(R.id.budgetLinearLayout);
        budgetLinearLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.budgetEditText).requestFocus();
    }

    public void AddMyAddressButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void AddAnotherAddressButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void NextForTagsButtonOnClick(View view) {
        ArrayList<View> tagsCheckBoxViews = this.GetCheckedTags();

        EditText requestDetailsEditText = (EditText) findViewById(R.id.requestDetailsEditText);
        String requestDetails = requestDetailsEditText.getText().toString();
        if (requestDetails.isEmpty()) {
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.missing_request_details_text));
            return;
        }

        if (tagsCheckBoxViews.isEmpty()) {
            GetTagsRequestContainer getTagsRequestContainer = new GetTagsRequestContainer();
            getTagsRequestContainer.caseDetails = new CaseDetails();
            getTagsRequestContainer.caseDetails.RequestDetails = requestDetails;

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
        } else {
            UserNewUpdateCase.getRecommendedAgentsRequestContainer = new GetRecommendedAgentsRequestContainer();
            UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails = new CaseDetails();
            UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.RequestDetails = requestDetails;

            EditText titleEditText = (EditText) findViewById(R.id.casePageTitleEditText);
            String title = titleEditText.getText().toString();
            if (title.isEmpty()) {
                new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.missing_title_popup_text));
                return;
            }

            UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Title = title;

            UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Tags = new ArrayList<>();
            for (View i : tagsCheckBoxViews) {
                if (((CheckBox) i).isChecked()) {
                    UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Tags.add(((CheckBox) i).getText().toString());
                }
            }

            if (UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Tags.isEmpty()) {
                new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.need_atleast_one_tag));
                return;
            }

            UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.ContactPreference = new ArrayList<>();
            CheckBox phoneCheckBox = (CheckBox) findViewById(R.id.phoneCheckBox);
            if (phoneCheckBox.isChecked()) {
                UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.ContactPreference.add("Phone");
            }

            CheckBox chatCheckBox = (CheckBox) findViewById(R.id.chatCheckBox);
            if (chatCheckBox.isChecked()) {
                UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.ContactPreference.add("Chat");
            }

            CheckBox emailCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
            if (emailCheckBox.isChecked()) {
                String emailAddress = (String) AppIdentity.GetResource(this, AppIdentity.emailAddress);
                if (emailAddress == null || emailAddress.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                    new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.missing_email_address_text));
                    return;
                }

                UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.ContactPreference.add("Email");
            }

            EditText budgetEditText = (EditText) findViewById(R.id.budgetEditText);
            String budget = budgetEditText.getText().toString();
            if (!budget.isEmpty()) {
                UserNewUpdateCase.getRecommendedAgentsRequestContainer.caseDetails.Budget = Integer.parseInt(budget);
            }

            String jsonString = new Gson().toJson(UserNewUpdateCase.getRecommendedAgentsRequestContainer);
            MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
            myResultReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
            intent.putExtra("receiver", myResultReceiver);
            intent.putExtra("command", "query");
            intent.putExtra("successCode", "4");
            intent.putExtra("apiCall", "GetRecommendedAgents");
            intent.putExtra("data", jsonString);
            startService(intent);
        }
    }

    public void RefreshTagsButtonOnClick(View view) {
        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        ArrayList<View> tagsCheckBoxViews = new ArrayList<>();
        tagsLinearLayout.findViewsWithText(tagsCheckBoxViews, TAG_CHECK_BOXES, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

        if (!tagsCheckBoxViews.isEmpty()) {
            for (View i : tagsCheckBoxViews) {
                tagsLinearLayout.removeView(i);
            }

            this.NextForTagsButtonOnClick(view);
        }
    }

    public void AddTagsButtonOnClick(View view) {
        AutoCompleteTextView tagAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
        String tagToAdd = tagAutoCompleteTextView.getText().toString();
        if (UserNewUpdateCase.allTagsCache != null && UserNewUpdateCase.allTagsCache.contains(tagToAdd)) {
            List<String> tagsToAdd = new ArrayList<>();
            tagsToAdd.add(0, tagToAdd);
            CreateTags(tagsToAdd);
            tagAutoCompleteTextView.setText("");
        } else {
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.unknown_tag_text));
        }
    }

    public void CancelButtonOnClick(View view) {
        Intent intent = new Intent(this, UserCaseOverview.class);
        startActivity(intent);
    }

    public void CannotFindTagButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
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

                if (getTagsReturnContainer.returnCode.equals("101")) {
                    this.CreateTags(getTagsReturnContainer.tags);
                    findViewById(R.id.scrollView).post(new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView) findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
                break;
            case 4:
                result = resultData.getString("results");
                GetRecommendedAgentsReturnContainer getRecommendedAgentsReturnContainer = new Gson().fromJson(result, GetRecommendedAgentsReturnContainer.class);

                if (getRecommendedAgentsReturnContainer.returnCode.equals("101")) {
                    String jsonString = new Gson().toJson(getRecommendedAgentsReturnContainer);
                    Intent intent = new Intent(this, SelectAgentForCase.class);
                    intent.putExtra("agentInfo", jsonString);
                    jsonString = new Gson().toJson(UserNewUpdateCase.getRecommendedAgentsRequestContainer);
                    intent.putExtra("caseInfo", jsonString);
                    startActivity(intent);
                }

                break;
            case 5:
                result = resultData.getString("results");
                GetTagsForAutoCompleteReturnContainer getTagsForAutoCompleteReturnContainer = new Gson().fromJson(result, GetTagsForAutoCompleteReturnContainer.class);

                if (getTagsForAutoCompleteReturnContainer.returnCode.equals("101")) {
                    final AutoCompleteTextView tagsAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getTagsForAutoCompleteReturnContainer.suggestedTags);
                    UserNewUpdateCase.allTagsCache = getTagsForAutoCompleteReturnContainer.suggestedTags;
                    tagsAutoCompleteTextView.setAdapter(adapter);
                }
        }
    }

    private void CreateTags(List<String> tags) {
        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        tagsLinearLayout.setVisibility(View.VISIBLE);
        for (String tag : tags) {
            RelativeLayout.LayoutParams tagCheckboxLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            CheckBox tagCheckBox = new CheckBox(this);
            tagCheckBox.setText(tag);
            tagCheckBox.setChecked(true);
            tagCheckBox.setContentDescription(TAG_CHECK_BOXES);
            tagsLinearLayout.addView(tagCheckBox, 2, tagCheckboxLayoutParams);
        }
    }

    private void CallAutoComplete(String text){
        GetTagsForAutoCompleteRequestContainer getTagsForAutoCompleteRequestContainer = new GetTagsForAutoCompleteRequestContainer();
        getTagsForAutoCompleteRequestContainer.text = text;
        String jsonString = new Gson().toJson(getTagsForAutoCompleteRequestContainer);

        MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("successCode", "5");
        intent.putExtra("apiCall", "GetTagsForAutoComplete");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_new_update_case, menu);
        return true;
    }

    private ArrayList<View> GetCheckedTags() {
        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        ArrayList<View> tagsCheckBoxViews = new ArrayList<>();
        tagsLinearLayout.findViewsWithText(tagsCheckBoxViews, TAG_CHECK_BOXES, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        return tagsCheckBoxViews;
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
