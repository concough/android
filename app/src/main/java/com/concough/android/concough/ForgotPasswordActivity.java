package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.concough.android.general.AlertClass;
import com.concough.android.rest.AuthRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.SignupStruct;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonObject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.extensions.ValidatorExtensionsKt.isValidPhoneNumber;
import static com.concough.android.extensions.EditTextExtensionKt.DirectionFix;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button sendCodeButton;
    private Button loginButton;
    private EditText usernameEdittext;
    private SignupStruct signupStruct;
    private KProgressHUD loadingProgress;

    private String send_type = "sms";
    private String oldNumber = "";
    private String newNumber = "";

    public void setSend_type(String send_type) {
        this.send_type = send_type;

        switch (send_type) {
            case "call":
                sendCodeButton.setText("ارسال کد از طریق تماس");
                sendCodeButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.concough_border_outline_red_style));
                sendCodeButton.setEnabled(true);
                break;
            case "sms":
                sendCodeButton.setText("ارسال کد");
                sendCodeButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.concough_border_outline_style));
                break;
            case "":
                sendCodeButton.setText("فردا سعی نمایید...");
                sendCodeButton.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.concough_border_outline_gray_style));
                break;
        }
    }

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, ForgotPasswordActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().hide();

        this.signupStruct = new SignupStruct();

        TextView forgotTextView = (TextView) findViewById(R.id.forgotPasswordA_forgotTextView);
        TextView loginHintTextView = (TextView) findViewById(R.id.forgotPasswordA_loginHintTextView);
        usernameEdittext = (EditText) findViewById(R.id.forgotPasswordA_usernameEdit);
        sendCodeButton = (Button) findViewById(R.id.forgotPasswordA_sendCodeButton);
        loginButton = (Button) findViewById(R.id.forgotPasswordA_loginButton);


        forgotTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        loginHintTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        usernameEdittext.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        sendCodeButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        loginButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

        DirectionFix(usernameEdittext);

        usernameEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
              DirectionFix(usernameEdittext);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String username = usernameEdittext.getText().toString().trim();
                if(isValidPhoneNumber(username)){
                    if(username.startsWith("0"))
                        username=username.substring(1);

                    username = "98" + username;

                    newNumber = username;
                    if(!oldNumber.equals(newNumber)) {
                        setSend_type("sms");
                        oldNumber= newNumber;
                    }
                }
            }
        });

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEdittext.getText().toString().trim();
                if (isValidPhoneNumber(username)) {
                    if (username.startsWith("0"))
                        username = username.substring(1);

                    username = "98" + username;

                    ForgotPasswordActivity.this.forgotPassword(username);
                } else {
                    AlertClass.showTopMessage(ForgotPasswordActivity.this, findViewById(R.id.container), "Form", "PhoneVerifyWrong", "error", null);
                }
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = LoginActivity.newIntent(ForgotPasswordActivity.this);
                startActivity(i);
                finish();
            }
        });
    }

    private void forgotPassword(String username) {
        new ForgotPasswordTask().execute(username);
    }

    private class ForgotPasswordTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {


            AuthRestAPIClass.forgotPassword(params[0], send_type, new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    switch (status) {
                                        case "OK":
                                            try {
                                                ForgotPasswordActivity.this.signupStruct.setUsername(params[0]);
                                                ForgotPasswordActivity.this.signupStruct.setPreSignupId(jsonObject.get("id").getAsInt());

                                                Intent submitCodeIntent = SignupCodeActivity.newIntent(ForgotPasswordActivity.this, "ForgotPassA", ForgotPasswordActivity.this.signupStruct);
                                                startActivity(submitCodeIntent);

                                            } catch (Exception exc) {
                                            }
                                            break;
                                        case "Error":
                                            try {
                                                String errorType = jsonObject.get("error_type").getAsString();
                                                switch (errorType) {
                                                    case "UserNotExist":
                                                        AlertClass.showTopMessage(ForgotPasswordActivity.this, findViewById(R.id.container), "AuthProfile", errorType, "error", null);
                                                        break;
                                                    case "SMSSendError":
                                                    case "CallSendError": {
                                                        AlertClass.showAlertMessage(ForgotPasswordActivity.this, "AuthProfile", errorType, "error", null);
                                                        break;
                                                    }
                                                    case "ExceedToday": {
                                                        AlertClass.showAlertMessage(ForgotPasswordActivity.this, "AuthProfile", errorType, "error", new Function0<Unit>() {
                                                            @Override
                                                            public Unit invoke() {
                                                                ForgotPasswordActivity.this.setSend_type("call");
                                                                return null;
                                                            }
                                                        });
                                                        break;
                                                    }
                                                    case "ExceedCallToday": {
                                                        AlertClass.showAlertMessage(ForgotPasswordActivity.this, "AuthProfile", errorType, "error", new Function0<Unit>() {
                                                            @Override
                                                            public Unit invoke() {
                                                                ForgotPasswordActivity.this.setSend_type("");
                                                                sendCodeButton.setEnabled(false);
                                                                sendCodeButton.setBackgroundColor(ActivityCompat.getColor(ForgotPasswordActivity.this, R.color.colorConcoughGray2));
                                                                return null;
                                                            }
                                                });
                                                        break;
                                                    }
                                                    default:
                                                        break;
                                                }
                                            } catch (Exception exc) {
                                            }
                                            break;

                                    }
                                }
                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new ForgotPasswordTask().execute(params);
                            } else {
                                AlertClass.showTopMessage(ForgotPasswordActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (networkErrorType != null) {
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(ForgotPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ForgotPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
                                        break;
                                    }
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

            loadingProgress = AlertClass.showLoadingMessage(ForgotPasswordActivity.this);
            loadingProgress.show();
        }

    }
}
