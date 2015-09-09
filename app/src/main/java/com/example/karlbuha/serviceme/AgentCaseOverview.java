package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.MessageFormat;
import java.util.List;

import DataContract.DataModels.CaseDetails;
import DataContract.GetAgentCasesRequestContainer;
import DataContract.GetAgentCasesReturnContainer;
import Helpers.AppIdentity;
import Helpers.BaseActivity;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class AgentCaseOverview extends BaseActivity implements MyResultReceiver.Receiver {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_case_overview);

        GetAgentCasesRequestContainer getAgentCasesRequestContainer = new GetAgentCasesRequestContainer();
        getAgentCasesRequestContainer.agentId = (String) AppIdentity.GetResource(this, AppIdentity.userId);
        String jsonString = new Gson().toJson(getAgentCasesRequestContainer);

        ApiCallService.CallService(this, true, "GetAgentCases", jsonString, "3" );
    }

    public void MyQueueButtonOnClick(View view) {
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
                GetAgentCasesReturnContainer getAgentCasesReturnContainer = new Gson().fromJson(result, GetAgentCasesReturnContainer.class);

                if(getAgentCasesReturnContainer.returnCode.equals("101")){
                    TextView noRequestsText = (TextView) findViewById(R.id.noRequestsText);
                    noRequestsText.setVisibility(View.GONE);
                    this.CreateListOfCases(getAgentCasesReturnContainer.cases);
                }

                break;
        }
    }

    private void CreateListOfCases(List<CaseDetails> cases) {
        LinearLayout casesLinearLayout = (LinearLayout) findViewById(R.id.casesLinearLayout);
        for(CaseDetails singleCase : cases){
            LinearLayout caseLinearLayout = this.GetLinearLayout(singleCase.CaseId);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            linearLayoutParams.setMargins(0, 0, 0, px);
            casesLinearLayout.addView(caseLinearLayout, linearLayoutParams);

            TextView titleTextView = new TextView(this);
            titleTextView.setText(singleCase.Title);
            titleTextView.setTextAppearance(this, android.R.style.TextAppearance_Large);
            LinearLayout.LayoutParams linearLayoutParams1 = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            caseLinearLayout.addView(titleTextView, linearLayoutParams1);

            LinearLayout horizontalLinearLayout = new LinearLayout(this);
            horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            caseLinearLayout.addView(horizontalLinearLayout, linearLayoutParams1);

            TextView assignedToTextView = new TextView(this);
            assignedToTextView.setMaxLines(2);
            if(singleCase.AssignedAgentId == null){
                assignedToTextView.setText(MessageFormat.format(getResources().getString(R.string.name_status_format), singleCase.UserName, getResources().getString(R.string.unassigned_text)));
            }
            else{
                if(singleCase.IsEnterpriseTag){
                    assignedToTextView.setText(MessageFormat.format(getResources().getString(R.string.name_status_enterprise_format), singleCase.UserName, singleCase.AssignedAgentName));
                }
                else{
                    assignedToTextView.setText(MessageFormat.format(getResources().getString(R.string.name_status_format), singleCase.UserName, getResources().getString(R.string.assigned_text)));
                }
            }

            assignedToTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
            horizontalLinearLayout.addView(assignedToTextView, linearLayoutParams1);

            if(singleCase.NewPhoneCall) {
                ImageView phoneImageView = new ImageView(this);
                phoneImageView.setImageResource(R.drawable.ic_action_phone_icon);
                LinearLayout.LayoutParams linearLayoutParams2 = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                int px1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                linearLayoutParams2.setMargins(px, 0, 0, px1);

                horizontalLinearLayout.addView(phoneImageView, linearLayoutParams2);
            }
            // Add chat, email, phone icon
        }
    }

    private LinearLayout GetLinearLayout(String caseId) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setContentDescription(caseId);
        linearLayout.setClickable(true);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                GoToCaseDetails(v.getContentDescription().toString());
            }
        });

        return linearLayout;
    }

    private void GoToCaseDetails(String caseId) {
        Intent intent = new Intent(this, AgentCaseDetails.class);
        intent.putExtra("caseId", caseId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_agent_case_overview, menu);
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
