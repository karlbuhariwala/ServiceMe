package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import DataContract.DataModels.CaseDetails;
import DataContract.GetUserCasesRequestContainer;
import DataContract.GetUserCasesReturnContainer;
import Helpers.BaseActivity;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class UserCaseOverview extends BaseActivity implements MyResultReceiver.Receiver {
    private static List<CaseDetails> casesCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_case_overview);

        UserCaseOverview.casesCache = null;

        GetUserCasesRequestContainer getUserCasesRequestContainer = new GetUserCasesRequestContainer();
        getUserCasesRequestContainer.userId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);

        if(getUserCasesRequestContainer.userId == null || getUserCasesRequestContainer.userId.equals("")){
            Intent intent = new Intent(this, NewUser.class);
            startActivity(intent);
        }
        else {
            String jsonString = new Gson().toJson(getUserCasesRequestContainer);

            MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
            myResultReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
            intent.putExtra("receiver", myResultReceiver);
            intent.putExtra("command", "query");
            intent.putExtra("successCode", "3");
            intent.putExtra("apiCall", "GetUserCases");
            intent.putExtra("data", jsonString);
            startService(intent);
        }
    }

    public void CreateNewRequestButtonOnClick(View view){
        Intent intent = new Intent(this, UserNewUpdateCase.class);
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
                GetUserCasesReturnContainer getUserCasesReturnContainer = new Gson().fromJson(result, GetUserCasesReturnContainer.class);

                if(getUserCasesReturnContainer.returnCode.equals("101")){
                    if(getUserCasesReturnContainer.cases.size() > 0) {
                        TextView noRequestsText = (TextView) findViewById(R.id.noRequestsText);
                        noRequestsText.setVisibility(View.GONE);
                    }

                    UserCaseOverview.casesCache = getUserCasesReturnContainer.cases;
                    LinearLayout casesLinearLayout = (LinearLayout) findViewById(R.id.casesLinearLayout);
                    this.CreateListOfCases(getUserCasesReturnContainer.cases, casesLinearLayout);
                }

                break;
        }
    }

    private void CreateListOfCases(List<CaseDetails> cases, LinearLayout casesLinearLayout) {
        for(CaseDetails singleCase : cases){
            LinearLayout caseLinearLayout = this.GetLinearLayout(singleCase.CaseId);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            int px15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            linearLayoutParams.setMargins(0, 0, 0, px15);

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
            assignedToTextView.setText(singleCase.AssignedAgentName);
            assignedToTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
            caseLinearLayout.addView(assignedToTextView, linearLayoutParams1);


            if(singleCase.NewPhoneCall && false) {
                ImageView phoneImageView = new ImageView(this);
                phoneImageView.setImageResource(R.drawable.ic_action_phone_icon);
                LinearLayout.LayoutParams linearLayoutParams2 = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                int px10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                linearLayoutParams2.setMargins(px15, 0, 0, px10);
                caseLinearLayout.addView(phoneImageView, linearLayoutParams2);
            }
            // Add chat, email, phone icon

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
        Intent intent = new Intent(this, UserCaseDetails.class);
        intent.putExtra("caseId", caseId);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout casesLinearLayout = (LinearLayout) findViewById(R.id.casesLinearLayout);
        if(UserCaseOverview.casesCache != null && casesLinearLayout.getChildCount() > 0) {
            casesLinearLayout.removeAllViews();
            this.CreateListOfCases(UserCaseOverview.casesCache, casesLinearLayout);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_case_overview, menu);
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
            case R.id.action_agent_overview:
                Intent intent = new Intent(this, AgentCaseOverview.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
