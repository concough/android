package com.concough.android.concough;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.singletons.FontCacheSingleton;

public class SignupMoreInfo3Activity extends AppCompatActivity {
    private static final String TAG = "SignupMoreInfo3Activity";

    final private CharSequence myList[] = { "کارشناسی", "کارشناسی ارشد"};
    private TextView infoTextView;
    private Button nextButton;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SignupMoreInfo3Activity.class);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_more_info3);

        TextView infoTextView = (TextView) findViewById(R.id.signupInfo3A_infoTextViewLine1);
        infoTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        Button nextButton = (Button) findViewById(R.id.signupInfo3A_nextButton);
        nextButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

        final Button selectButton = (Button) findViewById(R.id.signupInfo3A_selectStateButton);
        selectButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        registerForContextMenu(selectButton);

        final AlertDialog.Builder ad = new AlertDialog.Builder(this);
        //ad.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

        ad.setTitle("متقاضی شرکت در کدام آزمون هستید؟");

        ad.setItems(myList, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                Toast.makeText(getApplicationContext(),
                        " شما گزینه " + myList[arg1]+ " را انتخاب کرده اید ",
                        Toast.LENGTH_LONG).show();
                selectButton.setText(myList[arg1]);

            }
        });

        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),
                        "شما هیچ گزینه ای را انتخب نکرده اید", Toast.LENGTH_LONG)
                        .show();
            }
        });

        View.OnClickListener li = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.show();
            }
        };

        selectButton.setOnClickListener(li);




    }



}
