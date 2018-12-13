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

import com.concough.android.extensions.EditTextExtensionKt;
import com.concough.android.extensions.ValidatorExtensionsKt;
import com.concough.android.general.AlertClass;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.EntranceOpenedCountModelHandler;
import com.concough.android.models.EntranceQuestionStarredModelHandler;
import com.concough.android.models.PurchasedModel;
import com.concough.android.models.PurchasedModelHandler;
import com.concough.android.rest.DeviceRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.rest.PurchasedRestAPIClass;
import com.concough.android.singletons.DeviceInformationSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.MediaCacheSingleton;
import com.concough.android.singletons.SynchronizationSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.KeyChainAccessProxy;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmResults;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.rest.ProfileRestAPIClass.getProfileData;
import static com.concough.android.settings.ConstantsKt.getCONNECTION_MAX_RETRY;
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
    private Integer retryCounter = 0;

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

        EditTextExtensionKt.DirectionFix(usernameEdit);
        EditTextExtensionKt.DirectionFix(passwordEdit);


        usernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditTextExtensionKt.DirectionFix(usernameEdit);

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
                EditTextExtensionKt.DirectionFix(passwordEdit);


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
                                LoginActivity.this.retryCounter = 0;

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
//
//                                                        LoginActivity.this.syncWithServer();

                                                        // Start Sync Server
                                                        SynchronizationSingleton.getInstance(LoginActivity.this).startSynchronizer();
                                                        // Navigate to Home Activity
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
                                if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                    LoginActivity.this.retryCounter += 1;
                                    new GetProfileTask().execute(params);
                                } else {
                                    LoginActivity.this.retryCounter = 0;
                                    AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                }
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

                            if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                LoginActivity.this.retryCounter += 1;
                                new GetProfileTask().execute(params);
                            } else {
                                LoginActivity.this.retryCounter = 0;
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

            if (!isFinishing()) {
                if (loadingProgress == null) {
                    loadingProgress = AlertClass.showLoadingMessage(LoginActivity.this);
                    loadingProgress.show();
                } else {
                    if (!loadingProgress.isShowing()) {
                        //loadingProgress = AlertClass.showLoadingMessage(HomeActivity.this);
                        loadingProgress.show();
                    }
                }
            }
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
                                LoginActivity.this.retryCounter = 0;

                                if (TokenHandlerSingleton.getInstance(getApplicationContext()).isAuthorized()) {
                                    KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getUSERNAME_KEY(), username);
                                    KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getPASSWORD_KEY(), password);

//                                    LoginActivity.this.getProfile();
                                    LoginActivity.this.getLockedStatus();
                                }


                            } else {
                                if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                    LoginActivity.this.retryCounter += 1;
                                    new LoginTask().execute(username, password);
                                } else {
                                    LoginActivity.this.retryCounter = 0;

                                    AlertClass.hideLoadingMessage(loadingProgress);
                                    AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                }
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
                            if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                LoginActivity.this.retryCounter += 1;
                                new LoginTask().execute(params);
                            } else {
                                LoginActivity.this.retryCounter = 0;
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

            if (!isFinishing()) {
                if (loadingProgress == null) {
                    loadingProgress = AlertClass.showLoadingMessage(LoginActivity.this);
                    loadingProgress.show();
                } else {
                    if (!loadingProgress.isShowing()) {
                        //loadingProgress = AlertClass.showLoadingMessage(HomeActivity.this);
                        loadingProgress.show();
                    }
                }
            }
        }
    }

    private void getLockedStatus() {
        new LockStatusTask().execute();
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
                                LoginActivity.this.retryCounter = 0;

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

                                                                if (DeviceInformationSingleton.getInstance(getApplicationContext()).setDeviceState(username, deviceName, deviceModel, false, false)) {
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
                                if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                    LoginActivity.this.retryCounter += 1;
                                    new LockStatusTask().execute(params);
                                } else {
                                    LoginActivity.this.retryCounter = 0;
                                    AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                }
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

                            if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                LoginActivity.this.retryCounter += 1;
                                new LockStatusTask().execute(params);
                            } else {
                                LoginActivity.this.retryCounter = 0;
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
//
//
//    private void syncWithServer() {
//        new SyncWithServerTask().execute();
//    }
//
//    private class SyncWithServerTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (!isFinishing()) {
//                        if (loadingProgress == null) {
//                            loadingProgress = AlertClass.showLoadingMessage(LoginActivity.this);
//                            loadingProgress.show();
//                        } else {
//                            if (!loadingProgress.isShowing()) {
//                                //loadingProgress = AlertClass.showLoadingMessage(HomeActivity.this);
//                                loadingProgress.show();
//                            }
//                        }
//                    }
//
//                }
//            });
//
//            PurchasedRestAPIClass.getPurchasedList(getApplicationContext(), new Function2<JsonElement, HTTPErrorType, Unit>() {
//                @Override
//                public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            AlertClass.hideLoadingMessage(loadingProgress);
//
//                            if (httpErrorType != HTTPErrorType.Success) {
//                                if (httpErrorType == HTTPErrorType.Refresh) {
//                                    new SyncWithServerTask().execute();
//                                } else {
//                                    if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
//                                        LoginActivity.this.retryCounter += 1;
//                                        new SyncWithServerTask().execute();
//                                    } else {
//                                        LoginActivity.this.retryCounter = 0;
//                                        AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
//                                    }
//                                }
//                            } else {
//                                LoginActivity.this.retryCounter = 0;
//                                if (jsonElement != null) {
//                                    String status = jsonElement.getAsJsonObject().get("status").getAsString();
//                                    switch (status) {
//                                        case "OK":
//                                            try {
//                                                ArrayList<Integer> purchasedId = new ArrayList<Integer>();
//                                                JsonArray records = jsonElement.getAsJsonObject().get("records").getAsJsonArray();
//                                                String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
//                                                if (username != null) {
//                                                    for (JsonElement record : records) {
//                                                        int id = record.getAsJsonObject().get("id").getAsInt();
//                                                        int downloaded = record.getAsJsonObject().get("downloaded").getAsInt();
//                                                        String createdStr = record.getAsJsonObject().get("created").getAsString();
//                                                        Date created = FormatterSingleton.getInstance().getUTCDateFormatter().parse(createdStr);
//
//                                                        JsonElement target = record.getAsJsonObject().get("target");
//                                                        String targetType = target.getAsJsonObject().get("product_type").getAsString();
//
//                                                        if (PurchasedModelHandler.getByUsernameAndId(getApplicationContext(), username, id) != null) {
//                                                            PurchasedModelHandler.updateDownloadTimes(getApplicationContext(), username, id, downloaded);
//
//                                                            if ("Entrance".equals(targetType)) {
//                                                                String uniqueId = target.getAsJsonObject().get("unique_key").getAsString();
//                                                                if (EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, uniqueId) == null) {
//                                                                    String org = target.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();
//                                                                    String type = target.getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString();
//                                                                    String setName = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString();
//                                                                    String group = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString();
//                                                                    int setId = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
//                                                                    int bookletsCount = target.getAsJsonObject().get("booklets_count").getAsInt();
//                                                                    int duration = target.getAsJsonObject().get("duration").getAsInt();
//                                                                    int year = target.getAsJsonObject().get("year").getAsInt();
//                                                                    int month = target.getAsJsonObject().get("month").getAsInt();
//
//                                                                    String extraStr = target.getAsJsonObject().get("extra_data").getAsString();
//                                                                    JsonElement extraData = null;
//                                                                    if (extraStr != null && !"".equals(extraStr)) {
//                                                                        try {
//                                                                            extraData = new JsonParser().parse(extraStr);
//                                                                        } catch (Exception exc) {
//                                                                            extraData = new JsonParser().parse("[]");
//                                                                        }
//                                                                    }
//
//                                                                    String lastPublishedStr = target.getAsJsonObject().get("last_published").getAsString();
//                                                                    Date lastPublished = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublishedStr);
//
//                                                                    EntranceStruct entrance = new EntranceStruct();
//                                                                    entrance.setEntranceSetId(setId);
//                                                                    entrance.setEntranceSetTitle(setName);
//                                                                    entrance.setEntranceOrgTitle(org);
//                                                                    entrance.setEntranceLastPublished(lastPublished);
//                                                                    entrance.setEntranceBookletCounts(bookletsCount);
//                                                                    entrance.setEntranceDuration(duration);
//                                                                    entrance.setEntranceExtraData(extraData);
//                                                                    entrance.setEntranceGroupTitle(group);
//                                                                    entrance.setEntranceTypeTitle(type);
//                                                                    entrance.setEntranceUniqueId(uniqueId);
//                                                                    entrance.setEntranceYear(year);
//                                                                    entrance.setEntranceMonth(month);
//
//                                                                    EntranceModelHandler.add(getApplicationContext(), username, entrance);
//
//                                                                }
//                                                            }
//                                                        } else {
//
//                                                            if ("Entrance".equals(targetType)) {
//                                                                String uniqueId = target.getAsJsonObject().get("unique_key").getAsString();
//
//                                                                if (PurchasedModelHandler.add(getApplicationContext(), id, username, false, downloaded, false, targetType, uniqueId, created)) {
//                                                                    String org = target.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();
//                                                                    String type = target.getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString();
//                                                                    String setName = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString();
//                                                                    String group = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString();
//                                                                    int setId = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
//                                                                    int bookletsCount = target.getAsJsonObject().get("booklets_count").getAsInt();
//                                                                    int duration = target.getAsJsonObject().get("duration").getAsInt();
//                                                                    int year = target.getAsJsonObject().get("year").getAsInt();
//                                                                    int month = target.getAsJsonObject().get("month").getAsInt();
//
//                                                                    String extraStr = target.getAsJsonObject().get("extra_data").getAsString();
//                                                                    JsonElement extraData = null;
//                                                                    if (extraStr != null &&  !"".equals(extraStr)) {
//                                                                        try {
//                                                                            extraData = new JsonParser().parse(extraStr);
//                                                                        } catch (Exception exc) {
//                                                                            extraData = new JsonParser().parse("[]");
//                                                                        }
//                                                                    }
//
//                                                                    String lastPublishedStr = target.getAsJsonObject().get("last_published").getAsString();
//                                                                    Date lastPublished = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublishedStr);
//
//                                                                    if (EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, uniqueId) == null) {
//                                                                        EntranceStruct entrance = new EntranceStruct();
//                                                                        entrance.setEntranceSetId(setId);
//                                                                        entrance.setEntranceSetTitle(setName);
//                                                                        entrance.setEntranceOrgTitle(org);
//                                                                        entrance.setEntranceLastPublished(lastPublished);
//                                                                        entrance.setEntranceBookletCounts(bookletsCount);
//                                                                        entrance.setEntranceDuration(duration);
//                                                                        entrance.setEntranceExtraData(extraData);
//                                                                        entrance.setEntranceGroupTitle(group);
//                                                                        entrance.setEntranceTypeTitle(type);
//                                                                        entrance.setEntranceUniqueId(uniqueId);
//                                                                        entrance.setEntranceYear(year);
//                                                                        entrance.setEntranceMonth(month);
//
//                                                                        EntranceModelHandler.add(getApplicationContext(), username, entrance);
//                                                                    }
//                                                                }
//                                                            }
//                                                        }
//
//                                                        purchasedId.add(id);
//                                                    }
//
//                                                    Integer[] dat = new Integer[purchasedId.size()];
//                                                    for (int i = 0; i < purchasedId.size(); i++) {
//                                                        dat[i] = purchasedId.get(i);
//                                                    }
//
//                                                    RealmResults<PurchasedModel> deletedItems = PurchasedModelHandler.getAllPurchasedNotIn(getApplicationContext(), username, dat);
//                                                    if (deletedItems.size() > 0) {
//                                                        for (PurchasedModel pm : deletedItems) {
//                                                            LoginActivity.this.deletePurchaseData(pm.productUniqueId);
//
//                                                            if ("Entrance".equals(pm.productType)) {
//                                                                if (EntranceModelHandler.removeById(getApplicationContext(), username, pm.productUniqueId)) {
//                                                                    //EntranceOpenedCountModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
//                                                                    EntranceQuestionStarredModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
//                                                                    PurchasedModelHandler.removeById(getApplicationContext(), username, pm.id);
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//
//                                                    //RealmResults<PurchasedModel> purchasedIn = PurchasedModelHandler.getAllPurchasedIn(BasketCheckoutActivity.this, username, purchasedIds);
//
//                                                    purchasedIds(dat);
//                                                }
//                                            } catch (Exception exc) {
//                                                Log.d(TAG, exc.getLocalizedMessage());
//                                            }
//                                            break;
//                                        case "Error":
//                                            String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
//                                            switch (errorType) {
//                                                case "EmptyArray":
//                                                    String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
//                                                    if (username != null) {
//                                                        RealmResults<PurchasedModel> items = PurchasedModelHandler.getAllPurchased(getApplicationContext(), username);
//
//                                                        for (PurchasedModel pm : items) {
//                                                            LoginActivity.this.deletePurchaseData(pm.productUniqueId);
//
//                                                            if ("Entrance".equals(pm.productType)) {
//                                                                if (EntranceModelHandler.removeById(getApplicationContext(), username, pm.productUniqueId)) {
//                                                                    EntranceOpenedCountModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
//                                                                    EntranceQuestionStarredModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
//                                                                    PurchasedModelHandler.removeById(getApplicationContext(), username, pm.id);
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//
//                                                    break;
//                                                default:
//                                                    break;
//                                            }
//                                            break;
//                                    }
//                                }
//
//                                Intent homeIntent = HomeActivity.newIntent(LoginActivity.this);
//                                startActivity(homeIntent);
//                                finish();
//                            }
//
//                        }
//                    });
//                    return null;
//                }
//            }, new Function1<NetworkErrorType, Unit>() {
//                @Override
//                public Unit invoke(final NetworkErrorType networkErrorType) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            AlertClass.hideLoadingMessage(loadingProgress);
//
//                            if (LoginActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
//                                LoginActivity.this.retryCounter += 1;
//                                new SyncWithServerTask().execute();
//                            } else {
//                                LoginActivity.this.retryCounter = 0;
//
//                                if (networkErrorType != null) {
//                                    switch (networkErrorType) {
//                                        case NoInternetAccess:
//                                        case HostUnreachable: {
//                                            AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
//                                            break;
//                                        }
//                                        default: {
//                                            AlertClass.showTopMessage(LoginActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
//                                            break;
//                                        }
//
//                                    }
//                                }
//                                Intent homeIntent = HomeActivity.newIntent(LoginActivity.this);
//                                startActivity(homeIntent);
//                                finish();
//                            }
//                        }
//                    });
//                    return null;
//                }
//            });
//
//            return null;
//        }
//    }
//
//
//    private void deletePurchaseData(String uniqueId) {
//        File f = new File(LoginActivity.this.getFilesDir(), uniqueId);
//        if (f.exists() && f.isDirectory()) {
////                                String[] children = f.list();
//            for (File fc : f.listFiles()) {
//                fc.delete();
//            }
//            boolean rd = f.delete();
//        }
//
//    }
//
//    private void purchasedIds(Integer[] ids) {
//        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
//
//        RealmResults<PurchasedModel> purchasedIn = PurchasedModelHandler.getAllPurchasedIn(getApplicationContext(), username, ids);
//        if (purchasedIn != null) {
//            for (PurchasedModel purchasedModel : purchasedIn) {
//                if (purchasedModel.productType.equals("Entrance")) {
//                    EntranceModel entranceModel = EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, purchasedModel.productUniqueId);
//                    if (entranceModel != null) {
//                        downloadImage(entranceModel.setId);
//                    }
//                }
//            }
//        }
//    }
//
//
//
//    private void downloadImage(final int imageId) {
//        final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
//
//        if (url != null) {
//            byte[]  data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
//            if (data != null) {
//
//                File folder = new File(getApplicationContext().getFilesDir(),"images");
//                File folder2 = new File(getApplicationContext().getFilesDir()+"/images","eset");
//                if (!folder.exists()) {
//                    folder.mkdir();
//                    folder2.mkdir();
//                }
//
//                File photo=new File(getApplicationContext().getFilesDir()+"/images/eset", String.valueOf(imageId));
//                if (photo.exists()) {
//                    photo.delete();
//                }
//
//                try {
//                    FileOutputStream fos=new FileOutputStream(photo.getPath());
//
//                    fos.write(data);
//                    fos.close();
//                }
//                catch (java.io.IOException e) {
//                    Log.e("PictureDemo", "Exception in photoCallback", e);
//                }
//            }
//        }
//
//    }
}
