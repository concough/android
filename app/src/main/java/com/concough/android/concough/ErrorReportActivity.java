package com.concough.android.concough;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.concough.android.singletons.FontCacheSingleton;

public class ErrorReportActivity extends AppCompatActivity {
    private static String TAG = "ErrorReportActivity";

    private Button  reportButton;
    private TextView  editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);


        TextView infoTextView = (TextView) findViewById(R.id.errorReport_infoTextView);
        reportButton = (Button) findViewById(R.id.errorReport_reportButton);
        editText = (TextView) findViewById(R.id.errorReport_editText);


        infoTextView.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getBold());
        reportButton.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getBold());
        editText.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getRegular());


    }
}
