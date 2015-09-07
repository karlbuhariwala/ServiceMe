package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import DataContract.GetTagsForAutoCompleteRequestContainer;
import DataContract.GetTagsForAutoCompleteReturnContainer;
import Helpers.BaseActivity;
import Helpers.MyPopupWindow;
import Helpers.MyProgressWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class AgentTagSetup extends BaseActivity implements MyResultReceiver.Receiver {
    private static final String TAG_CHECK_BOXES = "TagCheckBoxes";
    private static String autoCompleteSuggestString;
    private static List<String> allTagsCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_tag_setup);

        AgentTagSetup.autoCompleteSuggestString = "";
        final AutoCompleteTextView tagsAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
        tagsAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tagsAutoCompleteTextView.isPerformingCompletion()
                        || s.length() < 3
                        || AgentTagSetup.autoCompleteSuggestString.equals(s.subSequence(0, 2).toString())) {
                    return;
                }

                AgentTagSetup.autoCompleteSuggestString = s.subSequence(0, 2).toString();
                CallAutoComplete(AgentTagSetup.autoCompleteSuggestString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do nothing
            }
        });
    }

    private void CallAutoComplete(String text){
        GetTagsForAutoCompleteRequestContainer getTagsForAutoCompleteRequestContainer = new GetTagsForAutoCompleteRequestContainer();
        getTagsForAutoCompleteRequestContainer.text = text;
        String jsonString = new Gson().toJson(getTagsForAutoCompleteRequestContainer);

        MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        intent.putExtra("successCode", "3");
        intent.putExtra("apiCall", "GetTagsForAutoComplete");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    public void AddTagsButtonOnClick(View view) {
        AutoCompleteTextView tagAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
        String tagToAdd = tagAutoCompleteTextView.getText().toString();
        if (AgentTagSetup.allTagsCache != null && AgentTagSetup.allTagsCache.contains(tagToAdd)) {
            List<String> tagsToAdd = new ArrayList<>();
            tagsToAdd.add(0, tagToAdd);
            CreateTags(tagsToAdd);
            tagAutoCompleteTextView.setText("");
        } else {
            new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.unknown_tag_text));
        }
    }

    public void DoneButtonOnClick(View view) {
    }

    public void BackButtonOnClick(View view) {
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
    }

    private void CreateTags(List<String> tags) {
        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        for (String tag : tags) {
            RelativeLayout.LayoutParams tagCheckboxLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            CheckBox tagCheckBox = new CheckBox(this);
            tagCheckBox.setText(tag);
            tagCheckBox.setChecked(true);
            tagCheckBox.setContentDescription(TAG_CHECK_BOXES);
            tagsLinearLayout.addView(tagCheckBox, 1, tagCheckboxLayoutParams);
        }
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
                GetTagsForAutoCompleteReturnContainer getTagsForAutoCompleteReturnContainer = new Gson().fromJson(result, GetTagsForAutoCompleteReturnContainer.class);

                if (getTagsForAutoCompleteReturnContainer.returnCode.equals("101")) {
                    final AutoCompleteTextView tagsAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getTagsForAutoCompleteReturnContainer.suggestedTags);
                    AgentTagSetup.allTagsCache = getTagsForAutoCompleteReturnContainer.suggestedTags;
                    tagsAutoCompleteTextView.setAdapter(adapter);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_agent_tag_setup, menu);
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
