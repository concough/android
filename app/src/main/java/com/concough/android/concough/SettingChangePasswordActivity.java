package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.general.AlertClass;
import com.concough.android.rest.AuthRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.KeyChainAccessProxy;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.settings.ConstantsKt.getPASSWORD_KEY;

public class SettingChangePasswordActivity extends BottomNavigationActivity {
    private static final String TAG="SettingChangePasswordActivity";


    private Button saveButton;
    private EditText passwordEdit;
    private EditText passwordEditConfirm;
    private TextView changePassLabel;

    private KProgressHUD loadingProgress;


    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SettingChangePasswordActivity.class);
        return i;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_setting_change_password;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setMenuSelectedIndex(3);
        super.onCreate(savedInstanceState);


        saveButton = (Button) findViewById(R.id.settingChangePasswordA_saveButton);
        changePassLabel = (TextView) findViewById(R.id.settingChangePasswordA_changePassLabel);
        passwordEdit = (EditText) findViewById(R.id.settingChangePasswordA_passwordEdit);
        passwordEditConfirm = (EditText) findViewById(R.id.settingChangePasswordA_passwordEditConfirm);


        saveButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
        changePassLabel.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        passwordEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        passwordEditConfirm.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());


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
                String pass1 = passwordEdit.getText().toString().trim();
                String pass2 = passwordEditConfirm.getText().toString().trim();


                if (pass1.equals("") && pass2.equals("") ) {
                    SettingChangePasswordActivity.this.changePassword(pass1, pass2);
                } else {
                    AlertClass.showTopMessage(SettingChangePasswordActivity.this,findViewById(R.id.container),"Form","EmptyFields","error",null);
                }

            }
        });

        actionBarSet();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }




    private void actionBarSet() {
        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {
            }

            @Override
            public void OnBackClicked() {
                onBackPressed();
            }
        };

        super.createActionBar("کنکوق", true, null);
    }


    private void changePassword(String pass1, String pass2) {
        new SettingChangePasswordTask().execute(pass1, pass2);

    }


    private class SettingChangePasswordTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {

            AuthRestAPIClass.changePassword(params[0], params[1], getApplicationContext(),
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
                                                        final String modified = jsonObject.get("modified").getAsString();

                                                        final Date modifiedDate = FormatterSingleton.getInstance().getUTCShortDateFormatter().parse(modified);

                                                        AlertClass.showAlertMessage(SettingChangePasswordActivity.this, "ActionResult", "ChangePasswordSuccess", "success", new Function0<Unit>() {
                                                            @Override
                                                            public Unit invoke() {
                                                                SettingChangePasswordActivity.this.changeSetting(params[1], modifiedDate);
                                                                return null;
                                                            }
                                                        });

                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }

                                                    break;
                                                case "Error":
                                                    try {
                                                        String error_type = jsonObject.get("error_type").toString();
                                                        switch (error_type) {
                                                            case "PassCannotChange": {
                                                                AlertClass.showTopMessage(SettingChangePasswordActivity.this,findViewById(R.id.container),"AuthProfile","ForgotPass","error",null);
                                                                break;
                                                            }
                                                            case "MultiRecord":
                                                            case "RemoteDBError":
                                                            case "BadData":
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
                                        new SettingChangePasswordTask().execute(params);
                                    } else {
                                        AlertClass.showTopMessage(SettingChangePasswordActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                    }
                                }
                            });
                            return null;
                        }
                    },
                    new Function1<NetworkErrorType, Unit>() {
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
                                                AlertClass.showTopMessage(SettingChangePasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                                break;
                                            }
                                            default: {
                                                AlertClass.showTopMessage(SettingChangePasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
                                                break;
                                            }

                                        }
                                    }

                                }
                            });
                            return null;
                        }
                    }
            );
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingProgress = AlertClass.showLoadingMessage(SettingChangePasswordActivity.this);
            loadingProgress.show();

        }

    }


    private void changeSetting(String pass, Date modified) {
        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
        if (username != null) {
            TokenHandlerSingleton.getInstance(getApplicationContext()).setUsernameAndPassword(username, pass);
            KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getPASSWORD_KEY(), pass);
            UserDefaultsSingleton.getInstance(getApplicationContext()).updateModified(modified);

            TokenHandlerSingleton.getInstance(getApplicationContext()).authorize(new Function1<HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (httpErrorType == HTTPErrorType.Success) {
                                SettingChangePasswordActivity.this.finish();
                            } else {
                                AlertClass.showTopMessage(SettingChangePasswordActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(SettingChangePasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(SettingChangePasswordActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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
    }
}