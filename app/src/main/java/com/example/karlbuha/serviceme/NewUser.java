package com.example.karlbuha.serviceme;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.String;


public class NewUser extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Button sendVerificationCodeButton = (Button) findViewById(R.id.sendVerificationCodeButton);
        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
                String phoneNumber = phoneNumberEditText.getText().toString();
                TextView phoneNumberTextView = (TextView) findViewById(R.id.phoneNumberTextView);
                if(!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
                    phoneNumberTextView.setTextColor(getResources().getColor(R.color.red));
                }
                else
                {
                    phoneNumberTextView.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
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
