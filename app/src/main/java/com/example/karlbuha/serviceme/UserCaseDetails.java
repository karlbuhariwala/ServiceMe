package com.example.karlbuha.serviceme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import DataContract.GetUserCaseDetailRequestContainer;
import DataContract.GetUserCaseDetailReturnContainer;
import Helpers.Constants;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class UserCaseDetails extends Activity implements MyResultReceiver.Receiver {
    private static String caseId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_case_details);

        Intent passedInIntent = getIntent();
        GetUserCaseDetailRequestContainer getUserCaseDetailRequestContainer = new GetUserCaseDetailRequestContainer();
        getUserCaseDetailRequestContainer.caseId = passedInIntent.getStringExtra(Constants.caseIdString);
        UserCaseDetails.caseId = getUserCaseDetailRequestContainer.caseId;
        String jsonString = new Gson().toJson(getUserCaseDetailRequestContainer);

        ApiCallService.CallService(this, "GetUserCaseDetail", jsonString, "3");
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
                //show progress
                break;
            case 2:
                // handle the error
                break;
            case 3:
                result = resultData.getString("results");
                GetUserCaseDetailReturnContainer getUserCasesReturnContainer = new Gson().fromJson(result, GetUserCaseDetailReturnContainer.class);

                if(getUserCasesReturnContainer.returnCode.equals("101")){
                    TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
                    titleTextView.setText(getUserCasesReturnContainer.caseDetails.Title);

                    TextView detailsValueTextView = (TextView) findViewById(R.id.detailsValueTextView);
                    detailsValueTextView.setText(getUserCasesReturnContainer.caseDetails.RequestDetails);

                    if(getUserCasesReturnContainer.caseDetails.Budget != 0){
                        LinearLayout budgetLinearLayout = (LinearLayout) findViewById(R.id.budgetLinearLayout);
                        budgetLinearLayout.setVisibility(View.VISIBLE);
                        TextView budgetValueTextView = (TextView) findViewById(R.id.budgetValueTextView);
                        budgetValueTextView.setText(Integer.toString(getUserCasesReturnContainer.caseDetails.Budget));
                    }
                }

                break;
        }
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
