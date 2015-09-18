package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import DataContract.DataModels.UserProfile;
import DataContract.GetAgentsForAutoCompleteRequestContainer;
import DataContract.GetAgentsForAutoCompleteReturnContainer;
import DataContract.GetAgentsForCaseRequestContainer;
import DataContract.GetAgentsForCaseReturnContainer;
import Helpers.BaseActivity;
import Helpers.Constants;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class ViewAgentsForCase extends BaseActivity implements MyResultReceiver.Receiver {
    private static String caseId;
    private static List<UserProfile> allAgentsCache;
    private static String autoCompleteSuggestString = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_agents_for_case);

        GetAgentsForCaseRequestContainer getAgentsForCaseRequestContainer = new GetAgentsForCaseRequestContainer();
        Intent intent = getIntent();
        getAgentsForCaseRequestContainer.caseId = intent.getStringExtra(Constants.caseIdString);
        ViewAgentsForCase.caseId = getAgentsForCaseRequestContainer.caseId;
        String jsonString = new Gson().toJson(getAgentsForCaseRequestContainer);

        ApiCallService.CallService(this, true, "GetAgentsForCase", jsonString, "3");

        ViewAgentsForCase.autoCompleteSuggestString = "";
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.agentAutoCompleteTextView);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autoCompleteTextView.isPerformingCompletion()
                        || s.length() < 3
                        || ViewAgentsForCase.autoCompleteSuggestString.equals(s.subSequence(0, 2).toString())) {
                    return;
                }

                ViewAgentsForCase.autoCompleteSuggestString = s.subSequence(0, 2).toString();
                CallAutoComplete(ViewAgentsForCase.autoCompleteSuggestString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do nothing
            }
        });
    }

    private void CallAutoComplete(String text){
        GetAgentsForAutoCompleteRequestContainer getAgentsForAutoCompleteRequestContainer = new GetAgentsForAutoCompleteRequestContainer();
        getAgentsForAutoCompleteRequestContainer.text = text;
        String jsonString = new Gson().toJson(getAgentsForAutoCompleteRequestContainer);

        ApiCallService.CallService(this, false, "GetAgentsForAutoComplete", jsonString, "4");
    }

    public void AddFavoriteAgentsButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void AddAgentsButtonOnClick(View view){
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.agentAutoCompleteTextView);
        String name = autoCompleteTextView.getText().toString();
        LinearLayout agentInfoLinearLayout = (LinearLayout) findViewById(R.id.agentInfoLinearLayout);
        // Todo: Possible bug when last 4 digits and name the same. Low possibility
        for (UserProfile agent : ViewAgentsForCase.allAgentsCache){
            int end = agent.PhoneNumber.length() - 1;
            if(name.equals(agent.Name + " (" + agent.PhoneNumber.substring(end - 4, end) + ")")){
                this.AddAgentToView(agent, agentInfoLinearLayout);
            }
        }

        autoCompleteTextView.setText("");
    }

    public void BackButtonOnClick(View view) {
        Intent intent = new Intent(this, UserCaseDetails.class);
        intent.putExtra(Constants.caseIdString, ViewAgentsForCase.caseId);
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
                GetAgentsForCaseReturnContainer getUserCasesReturnContainer = new Gson().fromJson(result, GetAgentsForCaseReturnContainer.class);

                if(getUserCasesReturnContainer.returnCode.equals("101")){
                    LinearLayout agentInfoLinearLayout = (LinearLayout) findViewById(R.id.agentInfoLinearLayout);
                    for(UserProfile agent : getUserCasesReturnContainer.agents){
                        this.AddAgentToView(agent, agentInfoLinearLayout);
                    }
                }

                break;
            case 4:
                result = resultData.getString("results");
                GetAgentsForAutoCompleteReturnContainer getAgentsForAutoCompleteReturnContainer = new Gson().fromJson(result, GetAgentsForAutoCompleteReturnContainer.class);

                // Todo: Think about same names.
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.agentAutoCompleteTextView);
                ViewAgentsForCase.allAgentsCache = getAgentsForAutoCompleteReturnContainer.agents;
                List<String> names = new ArrayList<>();
                for (UserProfile agent : getAgentsForAutoCompleteReturnContainer.agents){
                    int end = agent.PhoneNumber.length() - 1;
                    names.add(agent.Name + " (" + agent.PhoneNumber.substring(end - 4, end) + ")");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
                autoCompleteTextView.setAdapter(adapter);
                break;
        }
    }

    public void AgentItemOnClick(View view){
        Intent intent = new Intent(this, UserAgentCaseDetails.class);
        intent.putExtra(Constants.caseIdString, ViewAgentsForCase.caseId);
        intent.putExtra(Constants.agentIdString, view.getContentDescription());
        startActivity(intent);
    }

    private TableRow getTableRow2(double rating, int numberOfRatings) {
        TableRow tableRow = new TableRow(this);

        LinearLayout ratingBarLinearLayout = new LinearLayout(this);
        TableRow.LayoutParams tableRowLayoutParamsCol2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRowLayoutParamsCol2.column = 2;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        tableRowLayoutParamsCol2.setMargins(px, 0, 0, 0);
        tableRow.addView(ratingBarLinearLayout, tableRowLayoutParamsCol2);

        RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyleSmall);
        ratingBar.setNumStars(5);
        ratingBar.setIsIndicator(true);
        ratingBar.setRating((float) rating);
        ratingBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.purple), PorterDuff.Mode.SRC_ATOP);
        ratingBarLinearLayout.addView(ratingBar);

        TextView textView = new TextView(this);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Small);
        textView.setTypeface(null, Typeface.ITALIC);
        String formatString = "{0}  ({1} {2})";
        textView.setText(MessageFormat.format(formatString, Double.toString(rating), Integer.toString(numberOfRatings), getResources().getString(R.string.rating_text)));
        TableRow.LayoutParams tableRowLayoutParamsCol3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRowLayoutParamsCol3.column = 3;
        tableRow.addView(textView, tableRowLayoutParamsCol3);

        return tableRow;

    }

    private TableRow getTableRow1(String name) {
        TableRow tableRow = new TableRow(this);
        TextView textView = new TextView(this);
        TableRow.LayoutParams tableRowLayoutParams = new TableRow.LayoutParams(2);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        textView.setText(name);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        tableRowLayoutParams.setMargins(px, 0 ,0 ,0);
        tableRow.addView(textView, tableRowLayoutParams);
        return tableRow;
    }

    private void AddAgentToView(UserProfile agent, LinearLayout agentInfoLinearLayout) {
        TableLayout table = this.getTableLayout(agent.UserId);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        linearLayoutParams.setMargins(0, 0, 0, px);
        agentInfoLinearLayout.addView(table, linearLayoutParams);

        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        TableRow tableRow1 = this.getTableRow1(agent.Name);
        table.addView(tableRow1, tableLayoutParams);

        TableRow tableRow2 = this.getTableRow2(agent.Rating, agent.NumberOfRatings);
        table.addView(tableRow2, tableLayoutParams);
    }

    private TableLayout getTableLayout(String agentId){
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setContentDescription(agentId);
        tableLayout.setClickable(true);

        tableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AgentItemOnClick(v);
            }
        });

        return tableLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_agents_for_case, menu);
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
