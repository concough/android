package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.concough.android.general.AlertClass;
import com.concough.android.rest.SettingsRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonObject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ErrorReportActivity extends BottomNavigationActivity {
    private static String TAG = "ErrorReportActivity";

    private Button reportButton;
    private TextView editText;

    private KProgressHUD loadingProgress;


    private boolean isKeyboardShow = false;


    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, ErrorReportActivity.class);
        return i;
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_error_report;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setMenuSelectedIndex(3);
        super.onCreate(savedInstanceState);

        TextView infoTextView = (TextView) findViewById(R.id.errorReport_infoTextView);
        reportButton = (Button) findViewById(R.id.errorReport_reportButton);
        editText = (EditText) findViewById(R.id.errorReport_editText);


        infoTextView.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getBold());
        reportButton.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getBold());
        editText.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getRegular());

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = editText.getText().toString();
                if (!description.equals("")) {
                    postBug(description);
                } else {
                    AlertClass.showAlertMessage(ErrorReportActivity.this, "Form", "EmptyFields", "error", null);
                }
            }
        });

        actionBarSet();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void postBug(String description) {
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
//               Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
        String osVersion = Build.VERSION.RELEASE;
        new PostProfileGradeTask().execute(description, deviceModel, osVersion);
    }

    private class PostProfileGradeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {

            SettingsRestAPIClass.postBug(getApplicationContext(), params[0], params[1], params[2], new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                                AlertClass.showAlertMessage(ErrorReportActivity.this, "ActionResult", "BugReportedSuccess", "success", new Function0<Unit>() {
                                                    @Override
                                                    public Unit invoke() {
                                                        ErrorReportActivity.this.finish();
                                                        return null;
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        case "Error":
                                            break;
                                    }

                                }
                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new PostProfileGradeTask().execute(params);
                            } else {
                                AlertClass.showTopMessage(ErrorReportActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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

                            switch (networkErrorType) {
                                case NoInternetAccess:
                                case HostUnreachable: {
                                    AlertClass.showTopMessage(ErrorReportActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                    break;
                                }
                                default: {
                                    AlertClass.showTopMessage(ErrorReportActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

            loadingProgress = AlertClass.showLoadingMessage(ErrorReportActivity.this);
            loadingProgress.show();

        }

    }

}
