package Helpers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.karlbuha.serviceme.R;

public class MyRatingPopupWindow {
    private PopupWindow popupWindow;

    public void InitiatePopupWindow(BaseActivity context, String text) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.rating_pop_up_window, (ViewGroup) context.findViewById(R.id.popUpWindowLinearLayout));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

            Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(cancel_button_click_listener);

            Button saveButton = (Button) layout.findViewById(R.id.saveFeedbackButton);
            saveButton.setOnClickListener(save_button_click_listener);

            RatingBar ratingBar = (RatingBar) layout.findViewById(R.id.ratingBar);
            LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(layout.getResources().getColor(R.color.purple), PorterDuff.Mode.SRC_ATOP);

            TextView textView = (TextView) layout.findViewById(R.id.popUpTextView);
            textView.setText(text);
            context.MainRelativeLayoutClick(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            popupWindow.dismiss();
        }
    };

    private View.OnClickListener save_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            popupWindow.dismiss();
        }
    };
}
