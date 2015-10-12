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

import DataContract.DataModels.CaseDetails;
import DataContract.DataModels.ContextualCaseDetails;
import DataContract.GetAgentCaseDetailsRequestContainer;
import DataContract.GetAgentCaseDetailsReturnContainer;
import helpers.BaseActivity;
import helpers.Constants;
import helpers.MyPopupWindow;
import helpers.MyProgressWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;

public class AgentCaseDetails extends BaseActivity implements MyResultReceiver.Receiver{
    private static String agentNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_case_details);

        Intent intent = getIntent();
        GetAgentCaseDetailsRequestContainer getAgentCaseDetailsRequestContainer = new GetAgentCaseDetailsRequestContainer();
        getAgentCaseDetailsRequestContainer.caseId = intent.getStringExtra(Constants.agentIdString);

        String jsonString = new Gson().toJson(getAgentCaseDetailsRequestContainer);
        ApiCallService.CallService(this, true, "GetAgentCaseDetails", jsonString, "3");
    }

    public void QuotationAndTimelineButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void BackButtonOnClick(View view) {
        Intent intent = new Intent(this, AgentCaseOverview.class);
        startActivity(intent);
    }

    public void SetMetadataButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void ResolveButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void SaveButtonOnClick(View view) {
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
                GetAgentCaseDetailsReturnContainer getAgentCaseDetailsReturnContainer = new Gson().fromJson(result, GetAgentCaseDetailsReturnContainer.class);

                if (getAgentCaseDetailsReturnContainer.returnCode.equals("101")) {
                    this.UpdateUI(getAgentCaseDetailsReturnContainer.caseInfo, getAgentCaseDetailsReturnContainer.contextualCaseDetails);
                }

                break;
        }
    }

    private void UpdateUI(CaseDetails caseInfo, ContextualCaseDetails contextualCaseDetails) {
        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(caseInfo.Title);

        TextView detailsValueTextView = (TextView) findViewById(R.id.detailsValueTextView);
        detailsValueTextView.setText(caseInfo.RequestDetails);

        if (caseInfo.Budget == 0) {
            LinearLayout budgetLinearLayout = (LinearLayout) findViewById(R.id.budgetLinearLayout);
            budgetLinearLayout.setVisibility(View.VISIBLE);

            TextView budgetTextView = (TextView) findViewById(R.id.budgetTextView);
            budgetTextView.setText(caseInfo.Budget);
        }

        if (contextualCaseDetails.Quote != null || contextualCaseDetails.PaymentStatus != null) {
            LinearLayout quoteTimelineLinearLayout = (LinearLayout) findViewById(R.id.quoteTimelineLinearLayout);
            quoteTimelineLinearLayout.setVisibility(View.VISIBLE);

            Button quotationAndTimelineButton = (Button) findViewById(R.id.quotationAndTimelineButton);
            quotationAndTimelineButton.setVisibility(View.GONE);

            if (contextualCaseDetails.Quote != null) {
                TextView quoteTimelineValueTextView = (TextView) findViewById(R.id.quoteTimelineValueTextView);
                String value = MessageFormat.format(getResources().getString(R.string.quote_timeline_format), contextualCaseDetails.Quote, contextualCaseDetails.Timeline);
                quoteTimelineValueTextView.setText(value);
            }

            if (contextualCaseDetails.PaymentStatus != null) {
                TextView paymentStatusValueTextView = (TextView) findViewById(R.id.paymentStatusValueTextView);
                paymentStatusValueTextView.setText(contextualCaseDetails.PaymentStatus);
            }
        }

        if (contextualCaseDetails.AgentNotes != null) {
            EditText notesLabelTextView = (EditText) findViewById(R.id.scratchPadEditText);
            notesLabelTextView.setText(contextualCaseDetails.AgentNotes);

            AgentCaseDetails.agentNotes = contextualCaseDetails.AgentNotes;
        }
        else {
            AgentCaseDetails.agentNotes = "";
        }

        final EditText scratchPadEditText = (EditText) findViewById(R.id.scratchPadEditText);
        scratchPadEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!AgentCaseDetails.agentNotes.equals(s.toString())) {
                    EnableNotesSave();
                    scratchPadEditText.removeTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void EnableNotesSave() {
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setText(getResources().getString(R.string.save_camel_case));
        saveButton.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_agent_case_details, menu);
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
