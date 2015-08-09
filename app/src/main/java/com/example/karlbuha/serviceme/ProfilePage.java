package com.example.karlbuha.serviceme;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import Helpers.MyPopupWindow;


public class ProfilePage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
    }

    public void AddEmailButtonOnClick(View view){
        Button addEmailButton = (Button)findViewById(R.id.addEmailAddressButton);
        LinearLayout emailLinearLayout = (LinearLayout) findViewById(R.id.emailLinearLayout);
        addEmailButton.setVisibility(View.GONE);
        emailLinearLayout.setVisibility(View.VISIBLE);
    }

    public void AddAddressButtonOnClick(View view){
        new MyPopupWindow().InitiatePopupWindow(this, getResources().getString(R.string.coming_soon_text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_page, menu);
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
