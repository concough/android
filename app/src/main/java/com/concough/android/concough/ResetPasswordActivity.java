package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.concough.android.rest.AuthRestAPIClass;
import com.concough.android.rest.ProfileRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.SignupStruct;
import com.google.gson.JsonObject;

import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText passwordEdit;
    private EditText passwordEditConfirm;
    private Button saveButton;

    private final static String SIGNUP_STRUCTURE_KEY = "SignupS";
    private SignupStruct infoStruct;

    public static Intent newIntent(Context packageContext, SignupStruct ss, Boolean clearStack) {
        Intent i = new Intent(packageContext, ResetPasswordActivity.class);
        if (clearStack == true)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        i.putExtra(SIGNUP_STRUCTURE_KEY, ss);
        return i;
    }

    public static Intent newIntent(Context packageContext, SignupStruct ss) {
        return newIntent(packageContext, ss, Boolean.TRUE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        getSupportActionBar().hide();

        TextView changePasswordTextView = (TextView) findViewById(R.id.resetPasswordA_changePasswordTextView);
        passwordEdit = (EditText) findViewById(R.id.resetPasswordA_passwordEdit);
        passwordEditConfirm = (EditText) findViewById(R.id.resetPasswordA_passwordEditConfirm);
        saveButton = (Button) findViewById(R.id.resetPasswordA_saveButton);

        changePasswordTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        passwordEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        passwordEditConfirm.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        saveButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        infoStruct = (SignupStruct) getIntent().getSerializableExtra(SIGNUP_STRUCTURE_KEY);

        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        passwordEdit.setTextDirection(View.TEXT_DIRECTION_LTR);
                        passwordEdit.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        passwordEdit.setGravity(Gravity.END);

                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        passwordEdit.setTextDirection(View.TEXT_DIRECTION_RTL);
                        passwordEdit.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        passwordEdit.setGravity(Gravity.START);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        passwordEditConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        passwordEditConfirm.setTextDirection(View.TEXT_DIRECTION_LTR);
                        passwordEditConfirm.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        passwordEditConfirm.setGravity(Gravity.END);

                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        passwordEditConfirm.setTextDirection(View.TEXT_DIRECTION_RTL);
                        passwordEditConfirm.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        passwordEditConfirm.setGravity(Gravity.START);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPasswordActivity.this.resetPassword(infoStruct);
            }
        });


    }

    private void resetPassword(SignupStruct infoStruct) {
        new ResetPasswordTask().execute(infoStruct);

    }

    private void startUp() {
        TokenHandlerSingleton.getInstance(getApplicationContext()).assureAuthorized(true,
                new Function2<Boolean, HTTPErrorType, Unit>() {
                    @Override
                    public Unit invoke(final Boolean aBoolean, final HTTPErrorType httpErrorType) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(httpErrorType == HTTPErrorType.Success && aBoolean ) {
                                    getProfile();
                                } else {
                                    LoginActivity.newIntent(ResetPasswordActivity.this);
                                    finish();
                                }

                            }
                        });

                        return null;
                    }
                }, new Function1<NetworkErrorType, Unit>() {
                    @Override
                    public Unit invoke(final NetworkErrorType networkErrorType) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO: hide loading
                                if (networkErrorType != null) {
                                    LoginActivity.newIntent(ResetPasswordActivity.this);
                                    finish();
                                }
                            }
                        });
                        return null;
                    }
                });
    }

    private class ResetPasswordTask extends AsyncTask<SignupStruct, Void, Void> {
        @Override
        protected Void doInBackground(final SignupStruct... params) {
            AuthRestAPIClass.resetPassword(params[0].getUsername(), Integer.valueOf(params[0].getPreSignupId()), String.valueOf(passwordEdit),
                    String.valueOf(passwordEditConfirm), Integer.valueOf(params[0].getPassword()),
                    new Function2<JsonObject, HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (httpErrorType == HTTPErrorType.Success) {
                                        if (jsonObject != null) {
                                            String status = jsonObject.get("status").getAsString();
                                            switch (status) {
                                                case "OK":
                                                    try {
                                                        TokenHandlerSingleton.getInstance(getApplicationContext()).setUsernameAndPassword(params[0].getUsername(), passwordEdit.toString());
                                                        startUp();

                                                    } catch (Exception exc) {

                                                    }
                                                    break;
                                                case "Error":
                                                    try {
                                                        String error_type = jsonObject.get("error_type").toString();
                                                        switch (error_type) {
                                                            case "ExpiredCode": {
                                                                // TODO: show error with ErrorResult and on OK send to ForgotPass
                                                                break;
                                                            }
                                                            case "UserNotExist":
                                                            case "PreAuthNotExist":
                                                                // TODO: Show AuthProfile error message and make ForgotPassword Activity
                                                                break;
                                                            default: break;
                                                        }
                                                    } catch (Exception exc) {

                                                    }
                                                    break;
                                            }

                                        }
                                    } else if (httpErrorType == HTTPErrorType.Refresh) {
                                        new ResetPasswordTask().execute(params);
                                    } else {
                                        // TODO: show error with msgType = "HTTPError" and error
                                    }
                                }
                            });
                            return null;
                        }
                    },
                    new Function1<NetworkErrorType, Unit>() {
                        @Override
                        public Unit invoke(NetworkErrorType networkErrorType) {
                            return null;
                        }
                    }
            );
            return null;
        }
    }

    private void getProfile() {
        new GetProfileTask().execute();
    }


    private class GetProfileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {
            ProfileRestAPIClass.getProfileData(ResetPasswordActivity.this, new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: hide loading
                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    switch (status) {
                                        case "OK": {

                                            JsonObject profile = jsonObject.getAsJsonArray("record").get(0).getAsJsonObject();
                                            if (profile != null) {
                                                try {
                                                    String gender = profile.get("gender").getAsString();
                                                    String grade = profile.get("grade").getAsString();
                                                    String birthday = profile.get("birthday").getAsString();
                                                    String modified = profile.get("modified").getAsString();
                                                    String firstname = profile.get("user").getAsJsonObject().get("first_name").getAsString();
                                                    String lastname = profile.get("user").getAsJsonObject().get("last_name").getAsString();


                                                    Date birthdayDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(birthday);
                                                    Date modifiedDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(modified);

                                                    if (!"".equals(firstname) && !"".equals(lastname) && !"".equals(gender) && !"".equals(grade)) {
                                                        UserDefaultsSingleton.getInstance(getApplicationContext()).createProfile(firstname, lastname, grade, gender, birthdayDate, modifiedDate);
                                                    }

                                                    if (UserDefaultsSingleton.getInstance(getApplicationContext()).hasProfile()) {

                                                        Intent homeIntent = HomeActivity.newIntent(ResetPasswordActivity.this);
                                                        startActivity(homeIntent);

                                                    } else {
                                                        // Profile not created
                                                        Intent moreInfoIntent = SignupMoreInfo1Activity.newIntent(ResetPasswordActivity.this);
                                                        startActivity(moreInfoIntent);
                                                    }

                                                } catch (Exception exc) {}

                                            }
                                            break;
                                        }
                                        case "Error": {
                                            String errorType = jsonObject.get("error_type").getAsString();
                                            switch (errorType) {
                                                case "ProfileNotExist": {
                                                    // Profile not created
                                                    Intent moreInfoIntent = SignupMoreInfo1Activity.newIntent(ResetPasswordActivity.this);
                                                    startActivity(moreInfoIntent);
                                                    finish();
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
                                new GetProfileTask().execute(params);
                            } else {
                                // TODO: show error with msgType = "HTTPError" and error
                            }
                        }
                    });

                    return null;
                }
            }, new Function1<NetworkErrorType, Unit>() {
                @Override
                public Unit invoke(final NetworkErrorType networkErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: hide loading
                            if (networkErrorType != null) {
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable:
                                    {
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
