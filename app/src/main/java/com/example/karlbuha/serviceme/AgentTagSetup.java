package com.example.karlbuha.serviceme;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import DataContract.AddAgentTagsRequestContainer;
import DataContract.AddAgentTagsReturnContainer;
import DataContract.GetTagsForAutoCompleteRequestContainer;
import DataContract.GetTagsForAutoCompleteReturnContainer;
import helpers.BaseActivity;
import helpers.MyPopupWindow;
import helpers.MyProgressWindow;
import webApi.ApiCallService;
import webApi.MyResultReceiver;


public class AgentTagSetup extends BaseActivity implements MyResultReceiver.Receiver {
    private static final String TAG_CHECK_BOXES = "TagCheckBoxes";
    private static final String TAG_CODE_LINEAR_LAYOUT = "CodeLinearLayout";
    private static final String TAG_CODE_EDIT_TEXT = "CodeEditText";
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
        intent.putExtra("showProgress", "false");
        intent.putExtra("apiCall", "GetTagsForAutoComplete");
        intent.putExtra("data", jsonString);
        startService(intent);
    }

    public void CannotFindTagButtonOnClick(View view) {
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
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
        AddAgentTagsRequestContainer addAgentTagsRequestContainer = new AddAgentTagsRequestContainer();
        addAgentTagsRequestContainer.tagCodeList = new ArrayList<>();

        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        ArrayList<View> tagsCheckBoxViews = this.GetCheckedTags(tagsLinearLayout);
        for (View i : tagsCheckBoxViews) {
            if (((CheckBox) i).isChecked()) {
                String tag = ((CheckBox) i).getText().toString();
                ArrayList<View> editTextViews = new ArrayList<>();
                tagsLinearLayout.findViewsWithText(editTextViews, tag + TAG_CODE_EDIT_TEXT, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                int code = 0;
                if(editTextViews.size() == 1) {
                    String value = ((EditText) editTextViews.get(0)).getText().toString();
                    if(value.length() < 4) {
                        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.code_length_incorrect_text) + tag);
                        return;
                    }

                    code = Integer.parseInt(value);
                }

                addAgentTagsRequestContainer.tagCodeList.add(new Pair<>(tag, code));
            }
        }

        String jsonString = new Gson().toJson(addAgentTagsRequestContainer);
        ApiCallService.CallService(this, true, "SetAgentTags", jsonString, "4" );
    }

    public void BackButtonOnClick(View view) {
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
    }

    private ArrayList<View> GetCheckedTags(LinearLayout tagsLinearLayout) {
        ArrayList<View> tagsCheckBoxViews = new ArrayList<>();
        tagsLinearLayout.findViewsWithText(tagsCheckBoxViews, TAG_CHECK_BOXES, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        return tagsCheckBoxViews;
    }

    private void CreateTags(List<String> tags) {
        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
        for (String tag : tags) {
            LinearLayout tagLinearLayout = new LinearLayout(this);
            tagLinearLayout.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams tagLinearLayoutLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            tagsLinearLayout.addView(tagLinearLayout, 1, tagLinearLayoutLayoutParams);

            LinearLayout.LayoutParams checkboxLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            CheckBox tagCheckBox = new CheckBox(this);
            tagCheckBox.setText(tag);
            tagCheckBox.setChecked(true);
            tagCheckBox.setContentDescription(TAG_CHECK_BOXES);
            tagLinearLayout.addView(tagCheckBox, 0, checkboxLayoutParams);

            LinearLayout.LayoutParams codeLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout codeLinearLayout = new LinearLayout(this);
            codeLinearLayout.setContentDescription(tag + TAG_CODE_LINEAR_LAYOUT);
            codeLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            codeLayoutParams.setMargins(px, 0, 0, 0);
            tagLinearLayout.addView(codeLinearLayout, 1, codeLayoutParams);

            LinearLayout.LayoutParams childLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            TextView textView = new TextView(this);
            textView.setText("Code: ");
            textView.setVisibility(View.GONE);
            codeLinearLayout.addView(textView, 0, childLayoutParams);

            EditText editText = new EditText(this);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setContentDescription(tag + TAG_CODE_EDIT_TEXT);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            editText.setHint("0000");
            editText.setVisibility(View.GONE);
            codeLinearLayout.addView(editText, 1, childLayoutParams);

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
                break;
            case 4:
                result = resultData.getString("results");
                AddAgentTagsReturnContainer addAgentTagsReturnContainer = new Gson().fromJson(result, AddAgentTagsReturnContainer.class);

                switch(addAgentTagsReturnContainer.returnCode){
                    case "101":
                        Intent intent = new Intent(this, AgentCaseOverview.class);
                        startActivity(intent);
                        break;
                    case "102":
                        LinearLayout tagsLinearLayout = (LinearLayout) findViewById(R.id.tagsLinearLayout);
                        for (String tag : addAgentTagsReturnContainer.tagsThatNeedCodes){
                            ArrayList<View> codeLinearLayouts = new ArrayList<>();
                            tagsLinearLayout.findViewsWithText(codeLinearLayouts, tag + TAG_CODE_LINEAR_LAYOUT, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                            if(codeLinearLayouts.size() == 1) {
                                int childCount = ((LinearLayout) codeLinearLayouts.get(0)).getChildCount();
                                for (int i = 0; i < childCount; i++) {
                                    ((LinearLayout) codeLinearLayouts.get(0)).getChildAt(i).setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        TextView enterpriseTagTextView = (TextView) findViewById(R.id.enterpriseTagTextView);
                        enterpriseTagTextView.setVisibility(View.VISIBLE);
                        break;
                    case "103":
                        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.code_incorrect_text) + addAgentTagsReturnContainer.tagWithIncorrectCode);
                        break;
                }

                break;
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
