package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.MessageFormat;

import DataContract.GetAgentDetailsRequestContainer;
import DataContract.GetAgentDetailsReturnContainer;
import DataContract.GetRecommendedAgentsRequestContainer;
import DataContract.GetRecommendedAgentsReturnContainer;
import Helpers.BaseActivity;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class NewCaseAgentInfo extends BaseActivity implements MyResultReceiver.Receiver{
    private static GetRecommendedAgentsRequestContainer caseInfoCache;
    private static GetRecommendedAgentsReturnContainer selectedAgentsCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_case_agent_info);

        Intent receivedIntent = getIntent();
        if(receivedIntent.getStringExtra("agentId") != null) {
            GetAgentDetailsRequestContainer getAgentDetailsRequestContainer = new GetAgentDetailsRequestContainer();
            getAgentDetailsRequestContainer.agentId = receivedIntent.getStringExtra("agentId");
            getAgentDetailsRequestContainer.userId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
            String jsonString = new Gson().toJson(getAgentDetailsRequestContainer);

            MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
            myResultReceiver.setReceiver(this);
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
            intent.putExtra("receiver", myResultReceiver);
            intent.putExtra("command", "query");
            intent.putExtra("successCode", "3");
            intent.putExtra("apiCall", "GetAgentDetails");
            intent.putExtra("data", jsonString);
            startService(intent);
        }

        if(receivedIntent.getStringExtra("caseInfo") != null){
            NewCaseAgentInfo.caseInfoCache = new Gson().fromJson(receivedIntent.getStringExtra("caseInfo"), GetRecommendedAgentsRequestContainer.class);
        }

        if(receivedIntent.getStringExtra("selectedAgents") != null){
            NewCaseAgentInfo.selectedAgentsCache = new Gson().fromJson(receivedIntent.getStringExtra("selectedAgents"), GetRecommendedAgentsReturnContainer.class);
        }
    }

    public void BackButtonOnClick(View view){
        Intent intent = new Intent(this, SelectAgentForCase.class);
        String jsonString = new Gson().toJson(NewCaseAgentInfo.caseInfoCache);
        intent.putExtra("caseInfo", jsonString);
        jsonString = new Gson().toJson(NewCaseAgentInfo.selectedAgentsCache);
        intent.putExtra("agentInfo", jsonString);
        startActivity(intent);
    }

    public void AddAsFavoriteButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void RemoveAsFavoriteButtonOnClick(View view) {
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
                GetAgentDetailsReturnContainer getAgentDetailsReturnContainer = new Gson().fromJson(result, GetAgentDetailsReturnContainer.class);

                if (getAgentDetailsReturnContainer.returnCode.equals("101")) {
                    TextView agentNameTextView = (TextView) findViewById(R.id.nameTextView);
                    agentNameTextView.setText(getAgentDetailsReturnContainer.agentProfile.Name);

                    RatingBar agentRatingBar = (RatingBar) findViewById(R.id.agentRatingBar);
                    agentRatingBar.setRating((float) getAgentDetailsReturnContainer.agentProfile.Rating);
                    LayerDrawable stars = (LayerDrawable) agentRatingBar.getProgressDrawable();
                    stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.purple), PorterDuff.Mode.SRC_ATOP);

                    TextView ratingTextView = (TextView) findViewById(R.id.ratingTextView);
                    String formatString = "{0}  ({1} {2})";
                    ratingTextView.setText(MessageFormat.format(formatString, Double.toString(getAgentDetailsReturnContainer.agentProfile.Rating), Integer.toString(getAgentDetailsReturnContainer.agentProfile.NumberOfRatings), getResources().getString(R.string.rating_text)));

                    String tagsString = "";
                    for (String tag : getAgentDetailsReturnContainer.agentProfile.Tags) {
                        tagsString += tag + ", ";
                    }

                    TextView tagValueTextView = (TextView) findViewById(R.id.tagValueTextView);
                    tagValueTextView.setText(tagsString.substring(0, tagsString.length() - 3));

                    TextView areaOfServiceValueTextView = (TextView) findViewById(R.id.areaOfServiceValueTextView);
                    areaOfServiceValueTextView.setText(getAgentDetailsReturnContainer.agentProfile.AreaOfService);

                    if (getAgentDetailsReturnContainer.isFavorite) {
                        Button addAsFavoriteButton = (Button) findViewById(R.id.addAsFavoriteButton);
                        addAsFavoriteButton.setVisibility(View.GONE);
                        Button removeAsFavoriteButton = (Button) findViewById(R.id.removeAsFavoriteButton);
                        removeAsFavoriteButton.setVisibility(View.VISIBLE);
                    }
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_case_agent_info, menu);
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
