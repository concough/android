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
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.SignupStruct;
import com.google.gson.JsonObject;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.extensions.ValidatorExtensionsKt.isValidPhoneNumber;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button sendCodeButton;
    private Button loginButton;
    private EditText usernameEdittext;
    private SignupStruct signupStruct;

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


        usernameEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                        usernameEdittext.setTextAlignment((View.LAYOUT_DIRECTION_LTR));
                        usernameEdittext.setTextDirection(View.TEXT_DIRECTION_LTR);
                        usernameEdittext.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        usernameEdittext.setGravity(Gravity.END);

                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        usernameEdittext.setTextDirection(View.TEXT_DIRECTION_RTL);
                        usernameEdittext.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        usernameEdittext.setGravity(Gravity.START);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
            AuthRestAPIClass.forgotPassword(params[0], new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                                ForgotPasswordActivity.this.signupStruct.setUsername(params[0]);
                                                ForgotPasswordActivity.this.signupStruct.setPreSignupId(jsonObject.get("id").getAsInt());

                                                Intent submitCodeIntent = SignupCodeActivity.newIntent(ForgotPasswordActivity.this, "ForgotPass", ForgotPasswordActivity.this.signupStruct);
                                                startActivity(submitCodeIntent);

                                            } catch (Exception exc) {
                                            }
                                            break;
                                        case "Error":
                                            try {
                                                String errorType = jsonObject.get("error_type").getAsString();
                                                switch (errorType) {
                                                    case "UserNotExist":
                                                        // TODO: Show error message eith msgType = "AuthProfile"
                                                        break;
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
                                    case HostUnreachable:
                                    case NoInternetAccess:
                                        // TODO: Show error message "NetworkError" with type = "error"
                                        break;
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
