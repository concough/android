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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.concough.android.general.AlertClass;
import com.concough.android.rest.AuthRestAPIClass;
import com.concough.android.rest.DeviceRestAPIClass;
import com.concough.android.rest.ProfileRestAPIClass;
import com.concough.android.singletons.DeviceInformationSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.SignupStruct;
import com.concough.android.utils.KeyChainAccessProxy;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonObject;

import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.settings.ConstantsKt.getPASSWORD_KEY;
import static com.concough.android.settings.ConstantsKt.getUSERNAME_KEY;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText passwordEdit;
    private EditText passwordEditConfirm;
    private Button saveButton;
    private KProgressHUD loadingProgress;


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
                String pass1 = passwordEdit.getText().toString();
                String pass2 = passwordEditConfirm.getText().toString();

                if (!pass1.equals("") &&  !pass2.equals("")) {
                    if (pass1.equals(pass2)) {
                        ResetPasswordActivity.this.resetPassword(pass1, pass2);
                    } else {
                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "Form", "NotSameFields", "error", null);
                    }
                } else {
                    AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "Form", "EmptyFields", "error", null);
                }
            }
        });


    }

    private void resetPassword(String pass1, String pass2) {
        new ResetPasswordTask().execute(pass1, pass2);

    }

    private void startUp(final String username, final String password) {
        TokenHandlerSingleton.getInstance(getApplicationContext()).authorize(new Function1<HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final HTTPErrorType httpErrorType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (httpErrorType == HTTPErrorType.Success) {
                            KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getUSERNAME_KEY(), username);
                            KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getPASSWORD_KEY(), password);

                            if (TokenHandlerSingleton.getInstance(getApplicationContext()).isAuthorized()) {
                                ResetPasswordActivity.this.getLockStatus();
                            } else if (TokenHandlerSingleton.getInstance(getApplicationContext()).isAuthenticated()) {
                                TokenHandlerSingleton.getInstance(getApplicationContext()).assureAuthorized(true,
                                        new Function2<Boolean, HTTPErrorType, Unit>() {
                                            @Override
                                            public Unit invoke(final Boolean aBoolean, final HTTPErrorType httpErrorType) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (httpErrorType == HTTPErrorType.Success && aBoolean) {
                                                            ResetPasswordActivity.this.getLockStatus();
                                                        } else {
                                                            Intent i = LoginActivity.newIntent(ResetPasswordActivity.this);
                                                            ResetPasswordActivity.this.startActivity(i);
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
                                                        if (networkErrorType != null) {
                                                            Intent i = LoginActivity.newIntent(ResetPasswordActivity.this);
                                                            ResetPasswordActivity.this.startActivity(i);
                                                            finish();
                                                        }
                                                    }
                                                });
                                                return null;
                                            }
                                        });

                            } else {
                                Intent i = LoginActivity.newIntent(ResetPasswordActivity.this);
                                ResetPasswordActivity.this.startActivity(i);
                                finish();
                            }
                        } else {
                            AlertClass.hideLoadingMessage(loadingProgress);
                            AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);

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
                                    AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                    break;
                                }
                                default: {
                                    AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
                                    break;
                                }
                            }
                        }

                    }
                });
                return null;
            }
        });

    }

    private class ResetPasswordTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {

            AuthRestAPIClass.resetPassword(infoStruct.getUsername(), Integer.valueOf(infoStruct.getPreSignupId()), params[0],
                    params[1], Integer.valueOf(infoStruct.getPassword()),
                    new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                                        TokenHandlerSingleton.getInstance(getApplicationContext()).setUsernameAndPassword(infoStruct.getUsername(), params[0]);
                                                        startUp(infoStruct.getUsername(), params[0]);

                                                    } catch (Exception exc) {

                                                    }
                                                    break;
                                                case "Error":
                                                    try {
                                                        String error_type = jsonObject.get("error_type").getAsString();
                                                        switch (error_type) {
                                                            case "ExpiredCode": {
                                                                AlertClass.showAlertMessage(ResetPasswordActivity.this, "ErrorResult", error_type, "error", new Function0<Unit>() {
                                                                    @Override
                                                                    public Unit invoke() {
                                                                        Intent i = ForgotPasswordActivity.newIntent(ResetPasswordActivity.this);
                                                                        startActivity(i);
                                                                        finish();
                                                                        return null;
                                                                    }
                                                                });
                                                                break;
                                                            }
                                                            case "UserNotExist":
                                                            case "PreAuthNotExist":
                                                                AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "AuthProfile", error_type, "error", null);
                                                                Intent i = ForgotPasswordActivity.newIntent(ResetPasswordActivity.this);
                                                                startActivity(i);
                                                                finish();
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
                                        new ResetPasswordTask().execute(params);
                                    } else {
                                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                    }
                                }
                            });
                            return null;
                        }
                    },
                    new Function1<NetworkErrorType, Unit>() {
                        @Override
                        public Unit invoke(NetworkErrorType networkErrorType) {

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (networkErrorType != null) {
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
                                        break;
                                    }
                                }
                            }

                            return null;
                        }
                    }
            );
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingProgress = AlertClass.showLoadingMessage(ResetPasswordActivity.this);
            loadingProgress.show();
        }

    }

    private void getProfile() {
        new GetProfileTask().execute();
    }


    private void getLockStatus() {
        new LockStatusTask().execute();

    }

    private class LockStatusTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {

            DeviceRestAPIClass.deviceLock(ResetPasswordActivity.this, true, new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                                        if (DeviceInformationSingleton.getInstance(getApplicationContext()).setDeviceState(username, "android", deviceModel, deviceState, true)) {
                                                            if (deviceState) {
                                                                getProfile();
                                                                return;
                                                            } else {

                                                                Intent i = StartupActivity.newIntent(ResetPasswordActivity.this);
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
                                                        AlertClass.showAlertMessage(ResetPasswordActivity.this, "DeviceInfoError", errorType, "error", new Function0<Unit>() {
                                                            @Override
                                                            public Unit invoke() {

                                                                String deviceName = jsonObject.get("error_data").getAsJsonObject().get("device_name").getAsString();
                                                                String deviceModel = jsonObject.get("error_data").getAsJsonObject().get("device_model").getAsString();

                                                                if (DeviceInformationSingleton.getInstance(getApplicationContext()).setDeviceState(username, deviceName, deviceModel, false, false)) {
                                                                    Intent i = StartupActivity.newIntent(ResetPasswordActivity.this);
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
                                new ResetPasswordActivity.LockStatusTask().execute(params);
                            } else {
                                AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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


    private class GetProfileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgress = AlertClass.showLoadingMessage(ResetPasswordActivity.this);
            loadingProgress.show();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            ProfileRestAPIClass.getProfileData(ResetPasswordActivity.this, new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                                        UserDefaultsSingleton.getInstance(getApplicationContext()).createProfile(firstname, lastname, grade,gradeString, gender, birthdayDate, modifiedDate);
                                                    }

                                                    if (UserDefaultsSingleton.getInstance(getApplicationContext()).hasProfile()) {

                                                        Intent homeIntent = HomeActivity.newIntent(ResetPasswordActivity.this);
                                                        startActivity(homeIntent);

                                                    } else {
                                                        // Profile not created
                                                        Intent moreInfoIntent = SignupMoreInfo1Activity.newIntent(ResetPasswordActivity.this);
                                                        startActivity(moreInfoIntent);
                                                    }

                                                } catch (Exception exc) {
                                                }

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
                                AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ResetPasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

    }


}
