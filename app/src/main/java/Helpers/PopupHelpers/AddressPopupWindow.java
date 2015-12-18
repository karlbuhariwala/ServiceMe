package Helpers.PopupHelpers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;

import com.example.karlbuha.serviceme.R;

import DataContract.DataModels.AddressContainer;
import Helpers.BaseActivity;
import Helpers.Interfaces.AddressPopupCallback;

public class AddressPopupWindow {
    private PopupWindow popupWindow;
    private View layout;
    private BaseActivity context;

    public void InitiatePopupWindow(BaseActivity context, AddressContainer addressContainer) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.layout = inflater.inflate(R.layout.address_pop_up_window, (ViewGroup) context.findViewById(R.id.popUpWindowLinearLayout));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

            this.context = context;

            if(addressContainer != null) {
                EditText firstLineAddressEditText = (EditText) layout.findViewById(R.id.firstLineAddressEditText);
                firstLineAddressEditText.setText(addressContainer.AddressLine1);

                EditText secondLineAddressEditText = (EditText) layout.findViewById(R.id.secondLineAddressEditText);
                secondLineAddressEditText.setText(addressContainer.AddressLine2);

                EditText cityEditText = (EditText) layout.findViewById(R.id.cityEditText);
                cityEditText.setText(addressContainer.City);

                EditText postalCodeEditText = (EditText) layout.findViewById(R.id.postalCodeEditText);
                postalCodeEditText.setText(addressContainer.PostalCode);

                EditText countryEditText = (EditText) layout.findViewById(R.id.countryEditText);
                countryEditText.setText(addressContainer.Country);
            }

            Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(cancel_button_click_listener);

            Button saveButton = (Button) layout.findViewById(R.id.saveFeedbackButton);
            saveButton.setOnClickListener(save_button_click_listener);

            LinearLayout linearLayout = (LinearLayout) layout.findViewById(R.id.popUpWindowLinearLayout);
            linearLayout.setOnClickListener(linear_layout_click_listener);

            RatingBar ratingBar = (RatingBar) layout.findViewById(R.id.ratingBar);
            LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(layout.getResources().getColor(R.color.purple), PorterDuff.Mode.SRC_ATOP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener linear_layout_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            context.MainRelativeLayoutClick(v);
        }
    };

    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            popupWindow.dismiss();
        }
    };

    private View.OnClickListener save_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            AddressContainer addressContainer = new AddressContainer();
            EditText firstLineAddressEditText = (EditText) layout.findViewById(R.id.firstLineAddressEditText);
            addressContainer.AddressLine1 = firstLineAddressEditText.getText().toString();

            EditText secondLineAddressEditText = (EditText) layout.findViewById(R.id.secondLineAddressEditText);
            addressContainer.AddressLine2 = secondLineAddressEditText.getText().toString();

            EditText cityEditText = (EditText) layout.findViewById(R.id.cityEditText);
            addressContainer.City = cityEditText.getText().toString();

            EditText postalCodeEditText = (EditText) layout.findViewById(R.id.postalCodeEditText);
            addressContainer.PostalCode = postalCodeEditText.getText().toString();

            EditText countryEditText = (EditText) layout.findViewById(R.id.countryEditText);
            addressContainer.Country = countryEditText.getText().toString();

            if(addressContainer.City.equals("") || addressContainer.Country.equals("")) {
                return;
            }

            ((AddressPopupCallback) context).ShowAddress(addressContainer);
            popupWindow.dismiss();
        }
    };
}
