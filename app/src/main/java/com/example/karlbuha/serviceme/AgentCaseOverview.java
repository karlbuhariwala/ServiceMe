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
import Helpers.BaseActivity;
import Helpers.Constants;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class AgentCaseOverview extends BaseActivity implements MyResultReceiver.Receiver {
    private static List<CaseDetails> casesCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_case_overview);

        AgentCaseOverview.casesCache = null;

        GetAgentCasesRequestContainer getAgentCasesRequestContainer = new GetAgentCasesRequestContainer();
        getAgentCasesRequestContainer.agentId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        String jsonString = new Gson().toJson(getAgentCasesRequestContainer);

        ApiCallService.CallService(this, true, "GetAgentCases", jsonString, "3");
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
                    if(getAgentCasesReturnContainer.cases.size() > 0) {
                        TextView noRequestsText = (TextView) findViewById(R.id.noRequestsText);
                        noRequestsText.setVisibility(View.GONE);
                    }

                    AgentCaseOverview.casesCache = getAgentCasesReturnContainer.cases;
                    LinearLayout casesLinearLayout = (LinearLayout) findViewById(R.id.casesLinearLayout);
                    this.CreateListOfCases(getAgentCasesReturnContainer.cases, casesLinearLayout);
                }

                break;
        }
    }

    private void CreateListOfCases(List<CaseDetails> cases, LinearLayout casesLinearLayout) {
        for(CaseDetails singleCase : cases){
            LinearLayout caseLinearLayout = this.GetLinearLayout(singleCase.CaseId);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            linearLayoutParams.setMargins(0, 0, 0, px);

            LinearLayout secondLevelLinearLayout = new LinearLayout(this);
            secondLevelLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            casesLinearLayout.addView(secondLevelLinearLayout, linearLayoutParams);
            secondLevelLinearLayout.addView(caseLinearLayout, linearLayoutParams);

            TextView titleTextView = new TextView(this);
            titleTextView.setText(singleCase.Title);
            titleTextView.setTextAppearance(this, android.R.style.TextAppearance_Large);
            LinearLayout.LayoutParams linearLayoutParams1 = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            caseLinearLayout.addView(titleTextView, linearLayoutParams1);

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
            caseLinearLayout.addView(assignedToTextView, linearLayoutParams1);

            String newMessage = new AppIdentityDb(this).GetResource(AppIdentityDb.newChatMessage + singleCase.CaseId);
            if (newMessage.equals("") || Integer.parseInt(newMessage) == 1) {
                ImageView chatImageView = new ImageView(this);
                chatImageView.setImageResource(R.drawable.ic_action_chat_icon);
                LinearLayout.LayoutParams linearLayoutParams2 = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                int px10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                linearLayoutParams2.setMargins(px10, 0, 0, px10);
                caseLinearLayout.addView(chatImageView, linearLayoutParams2);
            }

            LinearLayout.LayoutParams linearLayoutParams3 = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            int px20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            int px25 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
            ImageView arrowImageView = new ImageView(this);
            arrowImageView.setImageResource(R.drawable.ic_right_arrow);
            arrowImageView.setContentDescription(singleCase.CaseId);
            arrowImageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    GoToCaseDetails(v.getContentDescription().toString());
                }
            });
            linearLayoutParams3.setMargins(px25, px20, 0, 0);
            secondLevelLinearLayout.addView(arrowImageView, linearLayoutParams3);
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
        intent.putExtra(Constants.agentIdString, caseId);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout casesLinearLayout = (LinearLayout) findViewById(R.id.casesLinearLayout);
        if(AgentCaseOverview.casesCache != null && casesLinearLayout.getChildCount() > 0) {
            casesLinearLayout.removeAllViews();
            this.CreateListOfCases(AgentCaseOverview.casesCache, casesLinearLayout);
        }
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
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.action_user_overview:
                Intent intent = new Intent(this, UserCaseOverview.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
