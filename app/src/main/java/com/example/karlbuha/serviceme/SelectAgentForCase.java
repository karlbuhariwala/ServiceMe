package com.example.karlbuha.serviceme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.MessageFormat;
import java.util.List;

import DataContract.DataModels.UserProfile;
import DataContract.GetRecommendedAgentsReturnContainer;
import Helpers.MyPopupWindow;


public class SelectAgentForCase extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_agent_for_case);

        Intent intent = getIntent();
        if(intent.getStringExtra("agentInfo") != null) {
            String jsonString = intent.getStringExtra("agentInfo");
            GetRecommendedAgentsReturnContainer getRecommendedAgentsReturnContainer = new Gson().fromJson(jsonString, GetRecommendedAgentsReturnContainer.class);
            List<UserProfile> recommendedAgentList = getRecommendedAgentsReturnContainer.recommendedAgents;
            LinearLayout recAgentInfoLinearLayout = (LinearLayout) findViewById(R.id.recAgentInfoLinearLayout);
            for(UserProfile agent : recommendedAgentList){
                this.AddAgentToView(agent, recAgentInfoLinearLayout);
            }
        }
    }

    public void AddFavoriteAgentsButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    public void AddAgentsButtonOnClick(View view){

    }

    public void AgentItemOnClick(View view){

    }

    private void AddAgentToView(UserProfile agent, LinearLayout layout){
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

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setIsIndicator(true);
        ratingBar.setRating((float) rating);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.purple), PorterDuff.Mode.SRC_ATOP);
        TableRow.LayoutParams tableRowLayoutParamsCol2 = new TableRow.LayoutParams(2);
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        tableRowLayoutParamsCol2.setMargins(px, 0, 0, 0);
        tableRow.addView(ratingBar, tableRowLayoutParamsCol2);

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
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        tableRowLayoutParams.setMargins(px, 0 ,0 ,0);
        tableRow.addView(textView, tableRowLayoutParams);
        return tableRow;
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
