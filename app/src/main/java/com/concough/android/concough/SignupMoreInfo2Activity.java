package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.singletons.FontCacheSingleton;

import java.util.Date;

public class SignupMoreInfo2Activity extends AppCompatActivity {
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
//        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_more_info2);

        infoTextView = (TextView)findViewById(R.id.signupInfo2A_infoTextView);
        infoTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        final NumberPicker numberPicker = (NumberPicker) findViewById(R.id.signupInfo2A_numberPicker);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        int jdate = PersianCalendar.getPersianYear(new Date());
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
                selectedYear = Integer.valueOf(numberPicker.getValue());
                Toast.makeText(getApplicationContext(), "Date Picked: " + String.valueOf(selectedYear), Toast.LENGTH_LONG).show();
            }

        };


        nextButton = (Button) findViewById(R.id.signupInfo2A_nextButton);
        nextButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        nextButton.setOnClickListener(nextListener);

    }
}
