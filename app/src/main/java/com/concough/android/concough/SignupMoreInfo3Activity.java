package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.concough.android.rest.ProfileRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.GradeType;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.SignupMoreInfoStruct;
import com.google.gson.JsonObject;

import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class SignupMoreInfo3Activity extends AppCompatActivity {
    private static final String TAG = "SignupMoreInfo3Activity";

    private GradeType names[];
    private GradeType selectedGradeType = null;
    private TextView infoTextView;
    private Button nextButton;
    private AlertDialog showedAlertDialog;
    AlertDialog.Builder alertDialog;
    private TextView customAlertDialogTitle;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SignupMoreInfo3Activity.class);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_more_info3);

        names = GradeType.values();

        TextView infoTextView = (TextView) findViewById(R.id.signupInfo3A_infoTextViewLine1);
        infoTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        TextView stateTextView = (TextView) findViewById(R.id.signupInfo3A_stateTextView);
        stateTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

        Button nextButton = (Button) findViewById(R.id.signupInfo3A_nextButton);
        nextButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

        final Button selectButton = (Button) findViewById(R.id.signupInfo3A_selectStateButton);
        selectButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        registerForContextMenu(selectButton);

        // alert dialog
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.cc_alert_dialog_listview, null);

        alertDialog = new AlertDialog.Builder(SignupMoreInfo3Activity.this);
        alertDialog.setView(convertView);

        showedAlertDialog = alertDialog.create();

        TextView customAlertDialogTitle = new TextView(this);
        customAlertDialogTitle.setGravity(Gravity.CENTER);
        customAlertDialogTitle.setTextColor(Color.BLACK);
        customAlertDialogTitle.setPadding(0, 50, 0, 10);
        customAlertDialogTitle.setText("لطفا یکی از گزینه های زیر را انتخاب نمایید");
        customAlertDialogTitle.setTextSize(14);
        customAlertDialogTitle.setTextColor(getResources().getColor(R.color.colorConcoughGray));
        customAlertDialogTitle.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        showedAlertDialog.setCustomTitle(customAlertDialogTitle);

        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        AlertDialogCustomize adapter = new AlertDialogCustomize(this, R.layout.cc_alert_dialog_textview, names);
        lv.setAdapter(adapter);
        selectButton.setText(names[0].toString());
        this.selectedGradeType = names[0];
        SignupMoreInfo1Activity.signupInfo.setGrade(this.selectedGradeType.name());


        View.OnClickListener li = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showedAlertDialog.show();
            }
        };
        selectButton.setOnClickListener(li);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (showedAlertDialog != null) {
                    showedAlertDialog.dismiss();
                    int intId = (int) id;
                    selectButton.setText(SignupMoreInfo3Activity.this.names[intId].toString());

                    SignupMoreInfo3Activity.this.selectedGradeType = SignupMoreInfo3Activity.this.names[intId];
                    SignupMoreInfo1Activity.signupInfo.setGrade(SignupMoreInfo3Activity.this.selectedGradeType.name());
                }

            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new PostProfileTask().execute(SignupMoreInfo1Activity.signupInfo);

            }
        });
    }


    private class AlertDialogCustomize extends ArrayAdapter<GradeType> {
        private GradeType[] objects;

        public AlertDialogCustomize(@NonNull Context context, @LayoutRes int resource, @NonNull GradeType[] objects) {
            super(context, resource, objects);
            this.objects = objects;
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cc_alert_dialog_textview, null);
            }

            GradeType i = this.getItem(position);
            if (i != null) {
                TextView v = (TextView) convertView.findViewById(R.id.item1);
                v.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                v.setText(i.toString());

            }

            return convertView;
        }


    }


    private class PostProfileTask extends AsyncTask<SignupMoreInfoStruct, Void, Void> {
        @Override
        protected Void doInBackground(final SignupMoreInfoStruct... params) {

            ProfileRestAPIClass.postProfileData(params[0], SignupMoreInfo3Activity.this, new Function2<JsonObject, HTTPErrorType, Unit>()
            {
                @Override
                public Unit invoke ( final JsonObject jsonObject, final HTTPErrorType httpErrorType)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: hide loading
                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    switch (status) {
                                        case "OK": {
                                            Date modified = new Date();
                                            String modifiedStr = jsonObject.get("modified").getAsString();
                                            if (!"".equals(modifiedStr)) {
                                                try{
                                                    modified = FormatterSingleton.getInstance().getUTCDateFormatter().parse(modifiedStr);
                                                } catch (Exception exc) {
                                                }
                                            }

                                            UserDefaultsSingleton.getInstance(getApplicationContext()).createProfile(params[0].getFirstname(), params[0].getLastname(), params[0].getGrade(),
                                                    params[0].getGender(), params[0].getBirthday(), modified);

                                            Intent i = HomeActivity.newIntent(SignupMoreInfo3Activity.this);
                                            startActivity(i);
                                            SignupMoreInfo3Activity.this.finish();
                                            break;
                                        }
                                        case "Error": {
                                            String errorType = jsonObject.get("error_type").getAsString();
                                            switch (errorType) {
                                                case "UserNotExist": {
                                                    // TODO: Show message with msgTYpe = "AuthProfile"
                                                    break;
                                                }
                                                default:
                                                    break;
                                            }
                                            break;
                                        }
                                    }

                                }

                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new PostProfileTask().execute(params);
                            } else {

                                // TODO: show error with msgType = "HTTPError" and error
                            }
                        }
                    });

                    return null;
                }
            },new Function1<NetworkErrorType, Unit>(){
                @Override
                public Unit invoke(final NetworkErrorType networkErrorType){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: hide loading
                            if (networkErrorType != null) {
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        // TODO: Show error message "NetworkError" with type = "error"
                                        break;
                                    }
                                    default:
                                        // TODO: Show error message "NetworkError" with type = ""
                                        break;

                                }
                            }

                        }
                    });
                    return null;
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO: show loading
        }
    }


}
