package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.concough.android.rest.ProfileRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.KeyChainAccessProxy;
import com.google.gson.JsonObject;

import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.settings.ConstantsKt.getPASSWORD_KEY;
import static com.concough.android.settings.ConstantsKt.getUSERNAME_KEY;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextView signupTextView;
    private Button loginButton;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private TextView loginHintTextView;
    private Button registerButton;


    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        View.OnClickListener registerButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = SignupActivity.newIntent(LoginActivity.this);
                startActivity(i);
                finish();
            }
        };
        signupTextView = (TextView) findViewById(R.id.loginA_signupTextView);
        usernameEdit = (EditText) findViewById(R.id.loginA_usernameEdit);
        passwordEdit = (EditText) findViewById(R.id.loginA_passwordEdit);
        loginHintTextView = (TextView) findViewById(R.id.loginA_loginHintTextView);
        registerButton = (Button) findViewById(R.id.loginA_registerButton);
        loginButton = (Button) findViewById(R.id.loginA_loginButton);

        registerButton.setOnClickListener(registerButtonListener);


        View.OnClickListener loginButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Login Clicked", Toast.LENGTH_LONG).show();

                LoginActivity.this.login();
            }
        };
        loginButton.setOnClickListener(loginButtonListener);


        usernameEdit.requestFocus();

        signupTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        usernameEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        passwordEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        loginHintTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        registerButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        loginButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
    }

    private void login() {
        // get username and password
        String username = LoginActivity.this.usernameEdit.getText().toString().trim();
        final String password = LoginActivity.this.passwordEdit.getText().toString().trim();

        if (!"".equals(username) && !"".equals(password)) {
            // TODO: Show loading

            if (username.startsWith("0"))
                username = username.substring(1);
            username = "98" + username;

            TokenHandlerSingleton.getInstance(getApplicationContext()).setUsernameAndPassword(username, password);
//            final String finalUsername = username;

            new LoginTask().execute(username, password);
        } else {
            // TODO: show message with msgType = "Form" and msgSubType = "EmptyFields"
        }
    }

    private void getProfile() {
        new GetProfileTask().execute();
    }

    private class GetProfileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {
            ProfileRestAPIClass.getProfileData(LoginActivity.this, new Function2<JsonObject, HTTPErrorType, Unit>() {
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


                                                    Date birthdayDate = FormatterSingleton.getInstance().getUTCShortDateFormatter().parse(birthday);
                                                    Date modifiedDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(modified);

                                                    if (!"".equals(firstname) && !"".equals(lastname) && !"".equals(gender) && !"".equals(grade)) {
                                                        UserDefaultsSingleton.getInstance(getApplicationContext()).createProfile(firstname, lastname, grade, gender, birthdayDate, modifiedDate);
                                                    }

                                                    if (UserDefaultsSingleton.getInstance(getApplicationContext()).hasProfile()) {

                                                        Intent homeIntent = HomeActivity.newIntent(LoginActivity.this);
                                                        startActivity(homeIntent);
                                                        finish();

                                                    } else {
                                                        // Profile not created
                                                        Intent moreInfoIntent = SignupMoreInfo1Activity.newIntent(LoginActivity.this);
                                                        startActivity(moreInfoIntent);
                                                        finish();
                                                    }

                                                } catch (Exception exc) {
                                                    Log.d(TAG, exc.toString());
                                                    //
                                                }

                                            }
                                            break;
                                        }
                                        case "Error": {
                                            String errorType = jsonObject.get("error_type").getAsString();
                                            switch (errorType) {
                                                case "ProfileNotExist": {
                                                    // Profile not created
                                                    Intent moreInfoIntent = SignupMoreInfo1Activity.newIntent(LoginActivity.this);
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

    private class LoginTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO: show loading
        }

        @Override
        protected Void doInBackground(final Object... params) {
            TokenHandlerSingleton.getInstance(getApplicationContext()).authorize(new Function1<HTTPErrorType, Unit>() {

                String username = (String) params[0];
                String password = (String) params[1];

                @Override
                public Unit invoke(final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO: hide loading
                            if (httpErrorType == HTTPErrorType.Success) {
                                if (TokenHandlerSingleton.getInstance(getApplicationContext()).isAuthorized()) {
                                    KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getUSERNAME_KEY(), username);
                                    KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getPASSWORD_KEY(), password);

                                    LoginActivity.this.getProfile();
                                }


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
    }
}
