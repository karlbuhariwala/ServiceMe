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
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.MessageFormat;

import DataContract.AssignCaseRequestContainer;
import DataContract.AssignCaseReturnContainer;
import DataContract.GetAgentContextCaseDetailsRequestContainer;
import DataContract.GetAgentContextCaseDetailsReturnContainer;
import Helpers.BaseActivity;
import Helpers.Constants;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class UserAgentCaseDetails extends BaseActivity implements MyResultReceiver.Receiver {
    private static String caseId;
    private static String agentId;
    private static String userNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_agent_case_details);

        GetAgentContextCaseDetailsRequestContainer getAgentContextCaseDetailsRequestContainer = new GetAgentContextCaseDetailsRequestContainer();
        Intent intent = getIntent();
        getAgentContextCaseDetailsRequestContainer.agentId = intent.getStringExtra(Constants.agentIdString);
        getAgentContextCaseDetailsRequestContainer.caseId = intent.getStringExtra(Constants.caseIdString);
        getAgentContextCaseDetailsRequestContainer.userId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        UserAgentCaseDetails.caseId = getAgentContextCaseDetailsRequestContainer.caseId;
        UserAgentCaseDetails.agentId = getAgentContextCaseDetailsRequestContainer.agentId;
        String jsonString = new Gson().toJson(getAgentContextCaseDetailsRequestContainer);

        ApiCallService.CallService(this, true, "GetAgentContextCaseDetails", jsonString, "3");
    }

    public void BackButtonOnClick(View view) {
        Intent intent = new Intent(this, ViewAgentsForCase.class);
        intent.putExtra(Constants.caseIdString, UserAgentCaseDetails.caseId);
        startActivity(intent);
    }

    public void AssignCaseButtonOnClick(View view) {
        AssignCaseRequestContainer assignCaseRequestContainer = new AssignCaseRequestContainer();
        assignCaseRequestContainer.agentId = UserAgentCaseDetails.agentId;
        assignCaseRequestContainer.caseId = UserAgentCaseDetails.caseId;
        String jsonString = new Gson().toJson(assignCaseRequestContainer);

        ApiCallService.CallService(this, true, "AssignAgentToCase", jsonString, "4");
    }

    public void SaveButtonOnClick (View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
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
                GetAgentContextCaseDetailsReturnContainer getAgentContextCaseDetailsReturnContainer = new Gson().fromJson(result, GetAgentContextCaseDetailsReturnContainer.class);

                if(getAgentContextCaseDetailsReturnContainer.returnCode.equals("101")){
                    TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
                    nameTextView.setText(getAgentContextCaseDetailsReturnContainer.contextualCaseDetails.AgentName);

                    TextView quoteTimelineValueTextView = (TextView) findViewById(R.id.quoteTimelineValueTextView);

                    String value = MessageFormat.format(getResources().getString(R.string.quote_timeline_format), getAgentContextCaseDetailsReturnContainer.contextualCaseDetails.Quote, getAgentContextCaseDetailsReturnContainer.contextualCaseDetails.Timeline);
                    quoteTimelineValueTextView.setText(value);

                    TextView paymentStatusValueTextView = (TextView) findViewById(R.id.paymentStatusValueTextView);
                    paymentStatusValueTextView.setText(getAgentContextCaseDetailsReturnContainer.contextualCaseDetails.PaymentStatus);

                    final EditText scratchPadEditText = (EditText) findViewById(R.id.scratchPadEditText);
                    scratchPadEditText.setText(getAgentContextCaseDetailsReturnContainer.contextualCaseDetails.UserNotes);
                    UserAgentCaseDetails.userNotes = getAgentContextCaseDetailsReturnContainer.contextualCaseDetails.UserNotes == null ? "" : getAgentContextCaseDetailsReturnContainer.contextualCaseDetails.UserNotes;
                    scratchPadEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (!UserAgentCaseDetails.userNotes.equals(s.toString())) {
                                EnableNotesSave();
                                scratchPadEditText.removeTextChangedListener(this);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }

                break;
            case 4:
                result = resultData.getString("results");
                AssignCaseReturnContainer assignCaseReturnContainer = new Gson().fromJson(result, AssignCaseReturnContainer.class);

                if(assignCaseReturnContainer.returnCode.equals("101")) {
                    new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.assigned_confirmation_text));
                }
                else {
                    new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.not_assigned_text));
                }

                break;
        }
    }

    private void EnableNotesSave() {
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setText(getResources().getString(R.string.save_camel_case));
        saveButton.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_agent_case_details, menu);
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
