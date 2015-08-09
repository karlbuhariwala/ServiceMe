package Helpers;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.karlbuha.serviceme.R;

public class MyPopupWindow {
    private PopupWindow popupWindow;

    public void InitiatePopupWindow(Activity context, String text) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.pop_up_window, (ViewGroup) context.findViewById(R.id.popUpWindowLinearLayout));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

            Button btnClosePopup = (Button) layout.findViewById(R.id.btn_close_popup);
            btnClosePopup.setOnClickListener(cancel_button_click_listener);

            TextView textView = (TextView) layout.findViewById(R.id.popUpTextView);
            textView.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            popupWindow.dismiss();
        }
    };
}
