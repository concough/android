package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.concough.android.general.AlertClass;
import com.concough.android.rest.SettingsRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.google.gson.JsonObject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ErrorReportActivity extends AppCompatActivity {
    private static String TAG = "ErrorReportActivity";

    private Button reportButton;
    private TextView editText;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, ErrorReportActivity.class);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);


        TextView infoTextView = (TextView) findViewById(R.id.errorReport_infoTextView);
        reportButton = (Button) findViewById(R.id.errorReport_reportButton);
        editText = (TextView) findViewById(R.id.errorReport_editText);


        infoTextView.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getBold());
        reportButton.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getBold());
        editText.setTypeface(FontCacheSingleton.getInstance(ErrorReportActivity.this).getRegular());

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = editText.getText().toString();
                if (description != "") {
                    postBug(description);
                } else {
                    AlertClass.showAlertMessage(getApplicationContext(), "Form", "EmptyFields", "warning", null);
                }
            }
        });


    }

    private void postBug(String description) {
        new PostProfileGradeTask().execute(description);
    }

    private class PostProfileGradeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {
            //TODO: show loading (must run on ui thread)
            SettingsRestAPIClass.postBug(getApplicationContext(), params[0], new Function2<JsonObject, HTTPErrorType, Unit>() {
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

            return null;
        }
    }

}
