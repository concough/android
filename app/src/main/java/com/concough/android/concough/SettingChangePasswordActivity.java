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

import com.concough.android.general.AlertClass;
import com.concough.android.rest.AuthRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.KeyChainAccessProxy;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.settings.ConstantsKt.getPASSWORD_KEY;

public class SettingChangePasswordActivity extends AppCompatActivity {

    private Button saveButton;
    private EditText passwordEdit;
    private EditText passwordEditConfirm;
    private TextView changePassLabel;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SettingChangePasswordActivity.class);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_change_password);

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
                        passwordEdit.setGravity(Gravity.END);

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


                if (pass1 != "" && pass2 != "") {

                    SettingChangePasswordActivity.this.changePassword(pass1, pass2);
                } else {
                    //TODO: show alert password is empty Message:Form SubMessage:Empty Field
                }

            }
        });


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
                                    if (httpErrorType == HTTPErrorType.Success) {
                                        if (jsonObject != null) {
                                            String status = jsonObject.get("status").getAsString();

                                            switch (status) {
                                                case "OK":
                                                    try {
                                                        final String modified = jsonObject.get("modified").getAsString();

                                                        final Date  modifiedDate = FormatterSingleton.getInstance().getUTCShortDateFormatter().parse(modified);

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
                                                                // TODO: show error with ErrorResult and on OK send to ForgotPass
                                                                break;
                                                            }
                                                            case "MultiRecord":
                                                            case "RemoteDBError":
                                                            case "BadData":
                                                                // TODO: Show AuthProfile error message and make ForgotPassword Activity
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
                                        // TODO: show error with msgType = "HTTPError" and error
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
                                    // TODO: hide loading dialog
                                    if (networkErrorType != null) {
                                        switch (networkErrorType) {
                                            case HostUnreachable:
                                            case NoInternetAccess:
                                                // TODO: show top error message with NetworkError
                                                break;
                                            default:
                                                // TODO: show top error message with NetworkError
                                                break;
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
    }


    private void changeSetting(String pass, Date modified) {
        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername(getApplicationContext());
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
                                //TODO : show top message HttpError
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
                            // TODO: hide loading dialog
                            if (networkErrorType != null) {
                                switch (networkErrorType) {
                                    case HostUnreachable:
                                    case NoInternetAccess:
                                        // TODO: show top error message with NetworkError
                                        break;
                                    default:
                                        // TODO: show top error message with NetworkError
                                        break;
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
