package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
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
import DataContract.GetRecommendedAgentsRequestContainer;
import DataContract.GetRecommendedAgentsReturnContainer;
import DataContract.SaveNewCaseRequestContainer;
import DataContract.SaveNewCaseReturnContainer;
import Helpers.BaseActivity;
import Helpers.Constants;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import Helpers.dbHelper.AppIdentityDb;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class SelectAgentForCase extends BaseActivity implements MyResultReceiver.Receiver {
    private static GetRecommendedAgentsReturnContainer selectedAgentsCache;
    private static String autoCompleteSuggestString = "";
    private static List<UserProfile> allAgentsCache;
    private static GetRecommendedAgentsRequestContainer caseInfoCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_agent_for_case);

        Intent intent = getIntent();
        if (intent.getStringExtra(Constants.agentInfoString) != null) {
            String jsonString = intent.getStringExtra(Constants.agentInfoString);
            SelectAgentForCase.selectedAgentsCache = new Gson().fromJson(jsonString, GetRecommendedAgentsReturnContainer.class);

            SelectAgentForCase.autoCompleteSuggestString = "";
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
                            || SelectAgentForCase.autoCompleteSuggestString.equals(s.subSequence(0, 2).toString())) {
                        return;
                    }

                    SelectAgentForCase.autoCompleteSuggestString = s.subSequence(0, 2).toString();
                    CallAutoComplete(SelectAgentForCase.autoCompleteSuggestString);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    //Do nothing
                }
            });
        }

        if (intent.getStringExtra(Constants.caseInfoString) != null) {
            SelectAgentForCase.caseInfoCache = new Gson().fromJson(intent.getStringExtra(Constants.caseInfoString), GetRecommendedAgentsRequestContainer.class);
        }
    }

    public void AddFavoriteAgentsButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void AddAgentsButtonOnClick(View view) {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.agentAutoCompleteTextView);
        String name = autoCompleteTextView.getText().toString();
        LinearLayout recAgentInfoLinearLayout = (LinearLayout) findViewById(R.id.recAgentInfoLinearLayout);
        // Todo: Possible bug when last 4 digits and name the same. Low possibility
        for (UserProfile agent : SelectAgentForCase.allAgentsCache) {
            int end = agent.PhoneNumber.length() - 1;
            if (name.equals(agent.Name + " (" + agent.PhoneNumber.substring(end - 4, end) + ")")) {
                this.AddAgentToView(agent, recAgentInfoLinearLayout);
                SelectAgentForCase.selectedAgentsCache.recommendedAgents.add(agent);
            }
        }

        autoCompleteTextView.setText("");
    }

    public void AgentItemOnClick(View view) {
        TableLayout tableLayout = (TableLayout) view;
        Intent intent = new Intent(this, NewCaseAgentInfo.class);
        String jsonString = new Gson().toJson(SelectAgentForCase.caseInfoCache);
        intent.putExtra("caseInfo", jsonString);
        jsonString = new Gson().toJson(SelectAgentForCase.selectedAgentsCache);
        intent.putExtra("selectedAgents", jsonString);
        intent.putExtra("agentId", tableLayout.getContentDescription().toString());
        startActivity(intent);
    }

    public void DoneButtonOnClick(View view) {
        SaveNewCaseRequestContainer saveNewCaseRequestContainer = new SaveNewCaseRequestContainer();
        saveNewCaseRequestContainer.caseInfo = SelectAgentForCase.caseInfoCache.caseDetails;
        saveNewCaseRequestContainer.caseInfo.UserId = new AppIdentityDb(this).GetResource(AppIdentityDb.userId);
        saveNewCaseRequestContainer.caseInfo.UserName = new AppIdentityDb(this).GetResource(AppIdentityDb.userName);

        LinearLayout recAgentInfoLinearLayout = (LinearLayout) findViewById(R.id.recAgentInfoLinearLayout);
        saveNewCaseRequestContainer.agentIds = new ArrayList<>();
        for (int i = 0; i < recAgentInfoLinearLayout.getChildCount(); i++) {
            TableLayout table = (TableLayout) recAgentInfoLinearLayout.getChildAt(i);
            saveNewCaseRequestContainer.agentIds.add(table.getContentDescription().toString());
        }

        String jsonString = new Gson().toJson(saveNewCaseRequestContainer);
        MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("successCode", "4");
        intent.putExtra("apiCall", "SaveNewCase");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    public void BackButtonOnClick(View view) {
        String jsonString = "";
        if (SelectAgentForCase.caseInfoCache != null) {
            jsonString = new Gson().toJson(SelectAgentForCase.caseInfoCache);
        }
        Intent intent = new Intent(this, UserNewUpdateCase.class);
        intent.putExtra("caseInfo", jsonString);
        startActivity(intent);
    }

    private void CallAutoComplete(String text) {
        GetAgentsForAutoCompleteRequestContainer getAgentsForAutoCompleteRequestContainer = new GetAgentsForAutoCompleteRequestContainer();
        getAgentsForAutoCompleteRequestContainer.text = text;
        String jsonString = new Gson().toJson(getAgentsForAutoCompleteRequestContainer);

        ApiCallService.CallService(this, false, "GetAgentsForAutoComplete", jsonString, "3");
    }

    private void AddAgentToView(UserProfile agent, LinearLayout layout) {
        TableLayout table = this.getTableLayout(agent.UserId);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        linearLayoutParams.setMargins(0, 0, 0, px);
        layout.addView(table, linearLayoutParams);

        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        TableRow tableRow1 = this.getTableRow1(agent.Name);
        table.addView(tableRow1, tableLayoutParams);

        TableRow tableRow2 = this.getTableRow2(agent.Rating, agent.NumberOfRatings);
        table.addView(tableRow2, tableLayoutParams);
    }

    private TableRow getTableRow2(double rating, int numberOfRatings) {
        TableRow tableRow = new TableRow(this);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(true);
        checkBox.setScaleX(1.5f);
        checkBox.setScaleY(1.5f);
        TableRow.LayoutParams tableRowLayoutParamsCol1 = new TableRow.LayoutParams(1);
        tableRow.addView(checkBox, tableRowLayoutParamsCol1);

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
        TableRow.LayoutParams tableRowLayoutParamsCol3 = new TableRow.LayoutParams(3);
        tableRow.addView(textView, tableRowLayoutParamsCol3);

        return tableRow;
    }

    private TableRow getTableRow1(String name) {
        TableRow tableRow = new TableRow(this);
        TextView textView = new TextView(this);
        TableRow.LayoutParams tableRowLayoutParams = new TableRow.LayoutParams(2);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        textView.setText(name);
        int px15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        tableRowLayoutParams.setMargins(px15, 0, 0, 0);
        tableRow.addView(textView, tableRowLayoutParams);

        ImageView infoImageView = new ImageView(this);
        infoImageView.setImageResource(R.drawable.ic_info_image);
        TableRow.LayoutParams tableRowLayoutParams1 = new TableRow.LayoutParams(4);
        int px5= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        tableRowLayoutParams1.setMargins(px15, px5, 0, 0);
        tableRow.addView(infoImageView, tableRowLayoutParams1);
        return tableRow;
    }

    private TableLayout getTableLayout(String agentId) {
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
                GetAgentsForAutoCompleteReturnContainer getAgentsForAutoCompleteReturnContainer = new Gson().fromJson(result, GetAgentsForAutoCompleteReturnContainer.class);

                // Todo: Think about same names.
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.agentAutoCompleteTextView);
                SelectAgentForCase.allAgentsCache = getAgentsForAutoCompleteReturnContainer.agents;
                List<String> names = new ArrayList<>();
                for (UserProfile agent : getAgentsForAutoCompleteReturnContainer.agents) {
                    int end = agent.PhoneNumber.length() - 1;
                    names.add(agent.Name + " (" + agent.PhoneNumber.substring(end - 4, end) + ")");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
                autoCompleteTextView.setAdapter(adapter);
                break;
            case 4:
                result = resultData.getString("results");
                SaveNewCaseReturnContainer saveNewCaseReturnContainer = new Gson().fromJson(result, SaveNewCaseReturnContainer.class);

                if (saveNewCaseReturnContainer.returnCode.equals("101")) {
                    Intent intent = new Intent(this, UserCaseOverview.class);
                    startActivity(intent);
                }

                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout recAgentInfoLinearLayout = (LinearLayout) findViewById(R.id.recAgentInfoLinearLayout);
        recAgentInfoLinearLayout.removeAllViews();
        for (UserProfile agent : SelectAgentForCase.selectedAgentsCache.recommendedAgents) {
            this.AddAgentToView(agent, recAgentInfoLinearLayout);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_agent_for_case, menu);
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
