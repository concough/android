package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.concough.android.extensions.ValidatorExtensionsKt;
import com.concough.android.general.AlertClass;
import com.concough.android.rest.DeviceRestAPIClass;
import com.concough.android.singletons.DeviceInformationSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.KeyChainAccessProxy;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonObject;

import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.rest.ProfileRestAPIClass.getProfileData;
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
    private Button rememberButton;
    private KProgressHUD loadingProgress;


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
        rememberButton = (Button) findViewById(R.id.loginA_rememberButton);

        rememberButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        rememberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ForgotPasswordActivity.newIntent(LoginActivity.this);
                startActivity(i);
            }
        });

        registerButton.setOnClickListener(registerButtonListener);


        View.OnClickListener loginButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Login Clicked", Toast.LENGTH_LONG).show();

                LoginActivity.this.login();
            }
        };
        loginButton.setOnClickListener(loginButtonListener);


        usernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        usernameEdit.setTextDirection(View.TEXT_DIRECTION_LTR);
                        usernameEdit.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        usernameEdit.setGravity(Gravity.END);

                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        usernameEdit.setTextDirection(View.TEXT_DIRECTION_RTL);
                        usernameEdit.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    } else {
                        usernameEdit.setGravity(Gravity.START);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


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

            if (ValidatorExtensionsKt.isValidPhoneNumber(username)) {
                if (username.startsWith("0"))
                    username = username.substring(1);
                username = "98" + username;

                TokenHandlerSingleton.getInstance(getApplicationContext()).setUsernameAndPassword(username, password);
//            final String finalUsername = username;

                new LoginTask().execute(username, password);
            } else {
                AlertClass.showAlertMessage(LoginActivity.this, "Form", "PhoneVerifyWrong", "error", null);
            }


        } else {
            AlertClass.showAlertMessage(LoginActivity.this, "Form", "EmptyFields", "error", null);
        }
    }

    private void getLockedStatus() {

    }

    private void getProfile() {
        new GetProfileTask().execute();
    }

    private class GetProfileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {

            getProfileData(LoginActivity.this, new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                        case "OK": {

                                            JsonObject profile = jsonObject.getAsJsonArray("record").get(0).getAsJsonObject();
                                            if (profile != null) {
                                                try {
                                                    String gender = profile.get("gender").getAsString();
                                                    String grade = profile.get("grade").getAsString();
                                                    String gradeString = profile.get("grade_string").getAsString();
                                                    String birthday = profile.get("birthday").getAsString();
                                                    String modified = profile.get("modified").getAsString();
                                                    String firstname = profile.get("user").getAsJsonObject().get("first_name").getAsString();
                                                    String lastname = profile.get("user").getAsJsonObject().get("last_name").getAsString();


                                                    Date birthdayDate = FormatterSingleton.getInstance().getUTCShortDateFormatter().parse(birthday);
                                                    Date modifiedDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(modified);

                                                    if (!"".equals(firstname) && !"".equals(lastname) && !"".equals(gender) && !"".equals(grade)) {
                                                        UserDefaultsSingleton.getInstance(getApplicationContext()).createProfile(firstname, lastname, grade, gradeString, gender, birthdayDate, modifiedDate);
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
                                AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                        AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

            loadingProgress = AlertClass.showLoadingMessage(LoginActivity.this);
            loadingProgress.show();
            //

        }

    }

    private class LoginTask extends AsyncTask<Object, Void, Void> {

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

                            if (httpErrorType == HTTPErrorType.Success) {
                                if (TokenHandlerSingleton.getInstance(getApplicationContext()).isAuthorized()) {
                                    KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getUSERNAME_KEY(), username);
                                    KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getPASSWORD_KEY(), password);

//                                    LoginActivity.this.getProfile();
                                    LoginActivity.this.getLockedStatus();
                                }


                            } else {
                                AlertClass.hideLoadingMessage(loadingProgress);
                                AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                        AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

            loadingProgress = AlertClass.showLoadingMessage(LoginActivity.this);
            loadingProgress.show();
        }
    }


    private class LockStatusTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {

            DeviceRestAPIClass.deviceCreate(LoginActivity.this, new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    switch (status) {
                                        case "OK": {
                                            String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                            if (username != null) {
                                                String deviceUniqueId = jsonObject.get("data").getAsJsonObject().get("device_unique_id").getAsString();
                                                Boolean deviceState = jsonObject.get("data").getAsJsonObject().get("state").getAsBoolean();
                                                String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;

                                                if (deviceUniqueId != null) {
                                                    String androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                                            Settings.Secure.ANDROID_ID);

                                                    if (androidId.equals(deviceUniqueId)) {
                                                        if (DeviceInformationSingleton.getInstance(getApplicationContext()).setDeviceState(username, "android", deviceModel, deviceState, false)) {
                                                            if (deviceState) {
                                                                getProfile();
                                                                return;
                                                            } else {

                                                                Intent i = StartupActivity.newIntent(LoginActivity.this);
                                                                startActivity(i);
                                                                finish();
                                                            }
                                                        }
                                                    }

                                                }

                                            }
                                            AlertClass.hideLoadingMessage(loadingProgress);
                                            break;
                                        }
                                        case "Error": {
                                            AlertClass.hideLoadingMessage(loadingProgress);

                                            String errorType = jsonObject.get("error_type").getAsString();
                                            switch (errorType) {
                                                case "AnotherDevice": {
                                                    final String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                                    if (username != null) {
                                                        AlertClass.showAlertMessage(LoginActivity.this, "DeviceInfoError", errorType, "error", new Function0<Unit>() {
                                                            @Override
                                                            public Unit invoke() {

                                                                String deviceName = jsonObject.get("error_data").getAsJsonObject().get("device_name").getAsString();
                                                                String deviceModel = jsonObject.get("error_data").getAsJsonObject().get("device_model").getAsString();

                                                                if (DeviceInformationSingleton.getInstance(getApplicationContext()).setDeviceState(username, deviceName, deviceModel, false, true)) {
                                                                    Intent i = StartupActivity.newIntent(LoginActivity.this);
                                                                    startActivity(i);
                                                                    finish();
                                                                }
                                                                return null;
                                                            }
                                                        });
                                                    }
                                                    break;
                                                }
                                                case "UserNotExist":
                                                case "DeviceNotRegistered":
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                        }
                                    }

                                }

                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new LockStatusTask().execute(params);
                            } else {
                                AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                        AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

//            loadingProgress = AlertClass.showLoadingMessage(LoginActivity.this);
//            loadingProgress.show();

        }

    }


}
