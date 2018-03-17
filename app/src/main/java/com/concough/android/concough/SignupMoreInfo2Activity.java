package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.utils.PersianCalendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SignupMoreInfo2Activity extends TopNavigationActivity {
    private static String TAG = "SignupMoreInfo2Activity";

    private NumberPicker numberPicker;
    private int minValue;
    private int maxValue;
    private int selectedYear;
    private String[] dateValuesArray;

    private TextView infoTextView;
    private Button nextButton;


    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SignupMoreInfo2Activity.class);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_more_info2);

        infoTextView = (TextView) findViewById(R.id.signupInfo2A_infoTextView);
        numberPicker = (NumberPicker) findViewById(R.id.signupInfo2A_numberPicker);

        int jdate = PersianCalendar.getPersianYear(new Date());

        infoTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minValue = jdate - 100;
        maxValue = jdate - 10;
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setValue(maxValue - 5);

        dateValuesArray = new String[100];
        int j = minValue;
        for (int i = 0; i < 100; i++) {
            dateValuesArray[i] = String.valueOf(j);
            j++;
        }

        numberPicker.setDisplayedValues(dateValuesArray);
        numberPicker.setWrapSelectorWheel(false);

        View.OnClickListener nextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    selectedYear = numberPicker.getValue();
                    Calendar gDate = PersianCalendar.getGregorainCalendar(selectedYear, 1, 1, 1, 1, 1);

                    String s = FormatterSingleton.getInstance().getUTCDateFormatter().format(gDate.getTime());
                    SignupMoreInfo1Activity.signupInfo.setBirthday(FormatterSingleton.getInstance().getUTCShortDateFormatter().parse(s));

                    Intent i = SignupMoreInfo3Activity.newIntent(SignupMoreInfo2Activity.this);
                    startActivity(i);
                } catch (ParseException e) {

            }


                //Toast.makeText(getApplicationContext(), "Date Picked: " + String.valueOf(selectedYear), Toast.LENGTH_LONG).show();
            }

        };

        nextButton = (Button) findViewById(R.id.signupInfo2A_nextButton);
        nextButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        nextButton.setOnClickListener(nextListener);

        actionBarSet();
    }

    private void actionBarSet() {
        ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();

        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {

            }

            @Override
            public void OnBackClicked() {
                onBackPressed();
            }
        };


        super.createActionBar("کنکوق", true, buttonDetailArrayList);
    }


}
