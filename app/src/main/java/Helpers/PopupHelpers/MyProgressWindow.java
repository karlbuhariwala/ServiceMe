package Helpers.PopupHelpers;

import android.app.Activity;
import android.app.ProgressDialog;

import com.example.karlbuha.serviceme.R;

public class MyProgressWindow {
    private static ProgressDialog progressDialog;

    public static void ShowProgressWindow(Activity activity) {
        if(MyProgressWindow.progressDialog != null && MyProgressWindow.progressDialog.isShowing()) {
            MyProgressWindow.progressDialog.dismiss();
        }

        MyProgressWindow.progressDialog = new ProgressDialog(activity);
        MyProgressWindow.progressDialog.setTitle(activity.getResources().getString(R.string.please_wait_string));
        MyProgressWindow.progressDialog.show();
    }

    public static void DismissProgressWindow() {
        if(MyProgressWindow.progressDialog != null && MyProgressWindow.progressDialog.isShowing()) {
            MyProgressWindow.progressDialog.dismiss();
        }
    }
}
