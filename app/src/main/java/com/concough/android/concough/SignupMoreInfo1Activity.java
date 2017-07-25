package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.structures.Gender;
import com.concough.android.structures.SignupMoreInfoStruct;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupMoreInfo1Activity extends AppCompatActivity {
    private static final String TAG = "SignupMoreInfo1Activity";

    public static SignupMoreInfoStruct signupInfo = null;
    private Gender selectedGender = null;

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
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_more_info1);

        this.signupInfo = new SignupMoreInfoStruct();
        this.selectedGender = Gender.Male;

//        setTitle(getResources().getString(R.string.signupMoreInfo1A_app_name).toString());
        //setTitle("");

        //View view = (View) LayoutInflater.from(getApplicationContext()).inflate(R.layout.cc_archiveActivity_action_bar, null);
        //TextView concoughTextView = (TextView) findViewById(R.id.actionbarA_concough);
        //concoughTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        //setContentView(view);


        //actionbar codes
//        ViewGroup actionBarLayout = (ViewGroup) this.getLayoutInflater().inflate( R.layout.cc_archiveActivity_action_bar,null);
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


        // Configure Self
        String firstname = this.signupInfo.getFirstname();
        if (firstname != null)
            if (firstname != "")
                firstNameEdit.setText(firstname);

        String lastname = this.signupInfo.getLastname();
        if (lastname != null)
            if (lastname != "")
                lastNameEdit.setText(lastname);

        String gender = this.signupInfo.getGender();
        if (gender != null) {
            this.selectedGender = Gender.valueOf(gender);
            this.linearClick(this.selectedGender);
        } else {
            this.linearClick(Gender.Male);

        }

        View.OnClickListener listenerNeutral = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
                SignupMoreInfo1Activity.this.selectedGender = Gender.Other;
                linearClick(Gender.Other);
            }
        };

        View.OnClickListener listenerFemale = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
                SignupMoreInfo1Activity.this.selectedGender = Gender.Female;
                linearClick(Gender.Female);
            }
        };

        View.OnClickListener listenerMale = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
                SignupMoreInfo1Activity.this.selectedGender = Gender.Male;
                linearClick(Gender.Male);
            }
        };

        linearLayoutNeutral.setOnClickListener(listenerNeutral);
        linearLayoutFemale.setOnClickListener(listenerFemale);
        linearLayoutMale.setOnClickListener(listenerMale);


        View.OnClickListener listenerNext = new View.OnClickListener() {
            @Override
            public void onClick(View li) {
//                 Toast.makeText(getApplicationContext(),"Next Clicked",Toast.LENGTH_LONG).show();
                String firstname = SignupMoreInfo1Activity.this.firstNameEdit.getText().toString().trim();
                String lastname = SignupMoreInfo1Activity.this.lastNameEdit.getText().toString().trim();
                Log.d(TAG, "onClick: " + firstname);
                Log.d(TAG, "onClick: " + lastname);

                if (!"".equals(firstname) && !"".equals(lastname)) {
                    SignupMoreInfo1Activity.this.signupInfo.setFirstname(firstname);
                    SignupMoreInfo1Activity.this.signupInfo.setLastname(lastname);
                    SignupMoreInfo1Activity.this.signupInfo.setGender(SignupMoreInfo1Activity.this.selectedGender.name());

                    Intent i = SignupMoreInfo2Activity.newIntent(SignupMoreInfo1Activity.this);
                    startActivity(i);
                } else {
                    // TODO: Show message with msgType = "Form" and msgSubType = "EmptyFields"
                }

            }
        };
        nextButton = (Button) findViewById(R.id.signupInfo1A_nextButton);
        nextButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        nextButton.setOnClickListener(listenerNext);

    }



    private void linearClick(Gender index) {
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
            case Other:
                neutralImage.setBorderWidth(6);
                neutralText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                neutralImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                break;
            case Female:
                femaleImage.setBorderWidth(6);
                femaleText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                femaleImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                break;
            case Male:
                maleImage.setBorderWidth(6);
                maleText.setTextColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                maleImage.setBorderColor(ContextCompat.getColor(this,R.color.colorConcoughBlue));
                break;
        }
    }
}
