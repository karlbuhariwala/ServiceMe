package Helpers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.karlbuha.serviceme.R;

public abstract class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        SetupUI(findViewById(R.id.mainRelativeLayout));
    }

    public void MainRelativeLayoutClick(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void SetupUI(View view) {
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainRelativeLayoutClick(v);
                }
            });

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                SetupUI(innerView);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SetupUI(findViewById(R.id.mainRelativeLayout));
    }
}
