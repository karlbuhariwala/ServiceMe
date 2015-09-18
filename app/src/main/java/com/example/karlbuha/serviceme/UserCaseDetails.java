package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.MessageFormat;

import DataContract.GetUserCaseDetailRequestContainer;
import DataContract.GetUserCaseDetailReturnContainer;
import Helpers.BaseActivity;
import Helpers.Constants;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class UserCaseDetails extends BaseActivity implements MyResultReceiver.Receiver {
    private static String caseId;
    private static String userNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_case_details);

        Intent passedInIntent = getIntent();
        GetUserCaseDetailRequestContainer getUserCaseDetailRequestContainer = new GetUserCaseDetailRequestContainer();
        getUserCaseDetailRequestContainer.caseId = passedInIntent.getStringExtra(Constants.caseIdString);
        UserCaseDetails.caseId = getUserCaseDetailRequestContainer.caseId;
        String jsonString = new Gson().toJson(getUserCaseDetailRequestContainer);

        ApiCallService.CallService(this, true, "GetUserCaseDetail", jsonString, "3");
    }

    public void ListOfAgentsButtonOnClick(View view) {
        Intent intent = new Intent(this, ViewAgentsForCase.class);
        intent.putExtra(Constants.caseIdString, UserCaseDetails.caseId);
        startActivity(intent);
    }

    public void BackButtonOnClick(View view) {
        Intent intent = new Intent(this, UserCaseOverview.class);
        startActivity(intent);
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
                GetUserCaseDetailReturnContainer getUserCasesReturnContainer = new Gson().fromJson(result, GetUserCaseDetailReturnContainer.class);

                if(getUserCasesReturnContainer.returnCode.equals("101")) {
                    TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
                    titleTextView.setText(getUserCasesReturnContainer.caseDetails.Title);

                    TextView detailsValueTextView = (TextView) findViewById(R.id.detailsValueTextView);
                    detailsValueTextView.setText(getUserCasesReturnContainer.caseDetails.RequestDetails);

                    if (getUserCasesReturnContainer.caseDetails.Budget != 0) {
                        LinearLayout budgetLinearLayout = (LinearLayout) findViewById(R.id.budgetLinearLayout);
                        budgetLinearLayout.setVisibility(View.VISIBLE);
                        TextView budgetValueTextView = (TextView) findViewById(R.id.budgetValueTextView);
                        budgetValueTextView.setText(Integer.toString(getUserCasesReturnContainer.caseDetails.Budget));
                    }

                    if (getUserCasesReturnContainer.caseDetails.AssignedAgentId != null) {
                        LinearLayout assignedAgentLinearLayout = (LinearLayout) findViewById(R.id.assignedAgentLinearLayout);
                        assignedAgentLinearLayout.setVisibility(View.VISIBLE);

                        TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
                        nameTextView.setText(getUserCasesReturnContainer.contextualCaseDetails.AgentName);

                        TextView quoteTimelineValueTextView = (TextView) findViewById(R.id.quoteTimelineValueTextView);

                        String value = MessageFormat.format(getResources().getString(R.string.quote_timeline_format), getUserCasesReturnContainer.contextualCaseDetails.Quote, getUserCasesReturnContainer.contextualCaseDetails.Timeline);
                        quoteTimelineValueTextView.setText(value);

                        TextView paymentStatusValueTextView = (TextView) findViewById(R.id.paymentStatusValueTextView);
                        paymentStatusValueTextView.setText(getUserCasesReturnContainer.contextualCaseDetails.PaymentStatus);

                        final EditText scratchPadEditText = (EditText) findViewById(R.id.scratchPadEditText);
                        scratchPadEditText.setText(getUserCasesReturnContainer.contextualCaseDetails.UserNotes);
                        UserCaseDetails.userNotes = getUserCasesReturnContainer.contextualCaseDetails.UserNotes == null ? "" : getUserCasesReturnContainer.contextualCaseDetails.UserNotes;
                        scratchPadEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (!UserCaseDetails.userNotes.equals(s.toString())) {
                                    EnableNotesSave();
                                    scratchPadEditText.removeTextChangedListener(this);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });
                    }
                }

                break;
        }
    }

    private void EnableNotesSave() {
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setText(getResources().getString(R.string.save_camel_case));
        saveButton.setEnabled(true);
    }

    public void SaveButtonOnClick (View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_case_details, menu);
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
