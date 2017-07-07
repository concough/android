package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.singletons.FontCacheSingleton;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupMoreInfo1Activity extends AppCompatActivity {
    private static final String TAG = "SignupMoreInfo1Activity";

    private LinearLayout linearLayoutNeutral;
    private LinearLayout linearLayoutFemale;
    private LinearLayout linearLayoutMale;

    private CircleImageView neutralImage;
    private CircleImageView femaleImage;
    private CircleImageView maleImage;

    private TextView neutralText;
    private TextView femaleText;
    private TextView maleText;
    private TextView infoTextview;

    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private Button nextButton;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SignupMoreInfo1Activity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_more_info1);

//        setTitle(getResources().getString(R.string.signupMoreInfo1A_app_name).toString());
        //setTitle("");

        //View view = (View) LayoutInflater.from(getApplicationContext()).inflate(R.layout.action_bar, null);
        //TextView concoughTextView = (TextView) findViewById(R.id.actionbarA_concough);
        //concoughTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        //setContentView(view);


        //actionbar codes
//        ViewGroup actionBarLayout = (ViewGroup) this.getLayoutInflater().inflate( R.layout.action_bar,null);
//        TextView concoughTextView = (TextView) actionBarLayout.findViewById(R.id.actionbarA_concough);
//        concoughTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
//        ActionBar actionBar = this.getSupportActionBar();
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setCustomView(actionBarLayout);


        linearLayoutNeutral = (LinearLayout) findViewById(R.id.signupInfo1A_linearNeutral);
        linearLayoutFemale = (LinearLayout) findViewById(R.id.signupInfo1A_linearFemal);
        linearLayoutMale = (LinearLayout) findViewById(R.id.signupInfo1A_linearMale);

        neutralImage = (CircleImageView) findViewById(R.id.signupInfo1A_imageViewNeutral);
        femaleImage = (CircleImageView) findViewById(R.id.signupInfo1A_imageViewFemale);
        maleImage = (CircleImageView) findViewById(R.id.signupInfo1A_imageViewMAle);

        neutralText = (TextView) findViewById(R.id.signupInfo1A_textViewNeutral);
        femaleText = (TextView) findViewById(R.id.signupInfo1A_textViewFemale);
        maleText = (TextView) findViewById(R.id.signupInfo1A_textViewMAle);
        infoTextview = (TextView) findViewById(R.id.signupInfo1A_infoTextView);

        firstNameEdit = (EditText) findViewById(R.id.signupInfo1A_firstNameEdit);
        lastNameEdit = (EditText) findViewById(R.id.signupInfo1A_lastNameEdit);


        neutralText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        femaleText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        maleText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        infoTextview.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        firstNameEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        lastNameEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());


        View.OnClickListener listenerNeutral = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
                linearClick(1);
            }
        };

        View.OnClickListener listenerFemale = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
                linearClick(2);
            }
        };

        View.OnClickListener listenerMale = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
                linearClick(3);
            }
        };

        linearLayoutNeutral.setOnClickListener(listenerNeutral);
        linearLayoutFemale.setOnClickListener(listenerFemale);
        linearLayoutMale.setOnClickListener(listenerMale);


        View.OnClickListener listenerNext = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
                 Toast.makeText(getApplicationContext(),"Next Clicked",Toast.LENGTH_LONG).show();
                Intent i = SignupMoreInfo2Activity.newIntent(SignupMoreInfo1Activity.this);
                startActivity(i);
            }
        };
        nextButton = (Button) findViewById(R.id.signupInfo1A_nextButton);
        nextButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        nextButton.setOnClickListener(listenerNext);

    }

    private void linearClick(int index) {
        neutralImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughGray));
        femaleImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughGray));
        maleImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughGray));

        neutralImage.setBorderWidth(2);
        femaleImage.setBorderWidth(2);
        maleImage.setBorderWidth(2);

        neutralText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughGray));
        femaleText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughGray));
        maleText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughGray));



        switch (index) {
            case 1:
                neutralImage.setBorderWidth(6);
                neutralText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                neutralImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                break;
            case 2:
                femaleImage.setBorderWidth(6);
                femaleText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                femaleImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                break;
            case 3:
                maleImage.setBorderWidth(6);
                maleText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                maleImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                break;


        }


    }
}
