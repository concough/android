package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.concough.android.general.AlertClass;
import com.concough.android.rest.ProfileRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.GradeType;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.SignupMoreInfoStruct;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.settings.ConstantsKt.getCONNECTION_MAX_RETRY;

public class SignupMoreInfo3Activity extends TopNavigationActivity {
    private static final String TAG = "SignupMoreInfo3Activity";

    private GradeType names[];
    private String selectedGradeType = "";
    private String selectedGradeStringType = "";
    private ArrayList<Pair<String, String>> namesPair;

    private TextView infoTextView;
    private TextView customAlertDialogTitle;
    private Button nextButton;
    private Button selectButton;

    private AlertDialog showedAlertDialog;
    private AlertDialog.Builder alertDialog;

    AlertDialogCustomize adapter;

    private KProgressHUD loadingProgress;
    private Integer retryCounter = 0;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SignupMoreInfo3Activity.class);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_more_info3);

        names = GradeType.values();
        namesPair = new ArrayList<>();

        TextView infoTextView = (TextView) findViewById(R.id.signupInfo3A_infoTextViewLine1);
        infoTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        TextView info2TextView = (TextView) findViewById(R.id.signupInfo3A_infoTextView);
        infoTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

        TextView stateTextView = (TextView) findViewById(R.id.signupInfo3A_stateTextView);
        stateTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

        nextButton = (Button) findViewById(R.id.signupInfo3A_nextButton);
        nextButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

        selectButton = (Button) findViewById(R.id.signupInfo3A_selectStateButton);
        selectButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        registerForContextMenu(selectButton);

        getProfileGradeList();

        // alert dialog
        LayoutInflater inflater = getLayoutInflater();
        final View convertView = (View) inflater.inflate(R.layout.cc_alert_dialog_listview, null);
        final ListView lv = (ListView) convertView.findViewById(R.id.lv);

        alertDialog = new AlertDialog.Builder(SignupMoreInfo3Activity.this);
        alertDialog.setView(convertView);

        showedAlertDialog = alertDialog.create();

        TextView customAlertDialogTitle = new TextView(this);
        customAlertDialogTitle.setGravity(Gravity.CENTER);
        customAlertDialogTitle.setTextColor(Color.BLACK);
        customAlertDialogTitle.setPadding(0, 50, 0, 10);
        customAlertDialogTitle.setText("لطفا یکی از گزینه های زیر را انتخاب نمایید");
        customAlertDialogTitle.setTextSize(14);
        customAlertDialogTitle.setTextColor(getResources().getColor(R.color.colorConcoughGray));
        customAlertDialogTitle.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        showedAlertDialog.setCustomTitle(customAlertDialogTitle);


        View.OnClickListener li = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (namesPair.size() > 0) {
                    ArrayList<String> gradeArray = new ArrayList<String>();
                    for (Pair<String, String> pair : namesPair) {
                        gradeArray.add(pair.second);
                    }
                    adapter = new AlertDialogCustomize(SignupMoreInfo3Activity.this, R.layout.cc_alert_dialog_textview, gradeArray);
                    lv.setAdapter(adapter);

                    selectedGradeType = namesPair.get(0).first;
                    selectedGradeStringType = namesPair.get(0).second;

                    SignupMoreInfo1Activity.signupInfo.setGrade(selectedGradeType);
                    SignupMoreInfo1Activity.signupInfo.setGradeString(selectedGradeStringType);

                    showedAlertDialog.show();
                } else {
                    getProfileGradeList();
                }
            }
        };

        selectButton.setOnClickListener(li);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (showedAlertDialog != null) {
                    showedAlertDialog.dismiss();
                    int intId = (int) id;
                    selectButton.setText(namesPair.get(intId).second);
                    selectedGradeType = namesPair.get(intId).first;

                    SignupMoreInfo1Activity.signupInfo.setGrade(namesPair.get(intId).first);
                    SignupMoreInfo1Activity.signupInfo.setGradeString(namesPair.get(intId).second);
                }

            }
        });

        nextButton.setEnabled(false);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(SignupMoreInfo1Activity.signupInfo.getGrade())) {
                    new PostProfileTask().execute(SignupMoreInfo1Activity.signupInfo);
                }
            }
        });

        actionBarSet();
    }


    private void actionBarSet() {
        ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();

        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {

            }

            @Override
            public void OnBackClicked() {
                onBackPressed();
            }

            @Override
            public void OnTitleClicked() {

            }
        };


        super.createActionBar("کنکوق", true, buttonDetailArrayList);
    }


    private class AlertDialogCustomize extends ArrayAdapter<String> {
        private ArrayList<String> objects;

        public AlertDialogCustomize(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<String> objects) {
            super(context, resource, objects);
            this.objects = objects;
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cc_alert_dialog_textview, null);
            }

            String i = objects.get(position);
            if (i != null) {
                TextView v = (TextView) convertView.findViewById(R.id.item1);
                v.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                v.setText(i.toString());

            }

            return convertView;
        }


    }

    private void getProfileGradeList() {
        new GetProfileGradeListTask().execute();
    }

    private class GetProfileGradeListTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {

            ProfileRestAPIClass.getProfileGradeList(getApplicationContext(), new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (httpErrorType == HTTPErrorType.Success) {
                                SignupMoreInfo3Activity.this.retryCounter = 0;

                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();

                                    if (status.equals("OK")) {
                                        JsonArray dataArray = jsonObject.get("record").getAsJsonArray();
                                        Pair<String, String> tempPair;
                                        JsonObject jo;
                                        for (JsonElement je : dataArray) {
                                            jo = je.getAsJsonObject();
                                            tempPair = new Pair<String, String>(jo.get("code").getAsString(), jo.get("title").getAsString());
                                            SignupMoreInfo3Activity.this.namesPair.add(tempPair);
                                        }

                                        if (namesPair.size() > 0) {
                                            selectButton.setText(namesPair.get(0).second);

                                            SignupMoreInfo1Activity.signupInfo.setGrade(namesPair.get(0).first);
                                            SignupMoreInfo1Activity.signupInfo.setGradeString(namesPair.get(0).second);

                                            nextButton.setEnabled(true);
                                        }

                                    } else if (status.equals("Error")) {
                                        String errorType = jsonObject.get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EmptyArray": {
                                                break;
                                            }
                                            default:
                                                break;
                                        }
                                    }
                                }
                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new GetProfileGradeListTask().execute(params);
                            } else {
                                if (SignupMoreInfo3Activity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                    SignupMoreInfo3Activity.this.retryCounter += 1;
                                    new GetProfileGradeListTask().execute(params);
                                } else {
                                    SignupMoreInfo3Activity.this.retryCounter = 0;
                                    AlertClass.showTopMessage(SignupMoreInfo3Activity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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

                            if (SignupMoreInfo3Activity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                SignupMoreInfo3Activity.this.retryCounter += 1;
                                new GetProfileGradeListTask().execute(params);
                            } else {
                                SignupMoreInfo3Activity.this.retryCounter = 0;
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(SignupMoreInfo3Activity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(SignupMoreInfo3Activity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

            if (!isFinishing()) {
                if (loadingProgress == null) {
                    loadingProgress = AlertClass.showLoadingMessage(SignupMoreInfo3Activity.this);
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


    private class PostProfileTask extends AsyncTask<SignupMoreInfoStruct, Void, Void> {
        @Override
        protected Void doInBackground(final SignupMoreInfoStruct... params) {

            ProfileRestAPIClass.postProfileData(params[0], SignupMoreInfo3Activity.this, new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (httpErrorType == HTTPErrorType.Success) {
                                SignupMoreInfo3Activity.this.retryCounter = 0;

                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    switch (status) {
                                        case "OK": {
                                            Date modified = new Date();
                                            String modifiedStr = jsonObject.get("modified").getAsString();
                                            if (!"".equals(modifiedStr)) {
                                                try {
                                                    modified = FormatterSingleton.getInstance().getUTCDateFormatter().parse(modifiedStr);
                                                } catch (Exception exc) {
                                                }
                                            }

                                            UserDefaultsSingleton.getInstance(getApplicationContext()).createProfile(params[0].getFirstname(), params[0].getLastname(), params[0].getGrade(), params[0].getGradeString(),
                                                    params[0].getGender(), params[0].getBirthday(), modified);

                                            Intent i = HomeActivity.newIntent(SignupMoreInfo3Activity.this);
                                            startActivity(i);
                                            SignupMoreInfo3Activity.this.finish();
                                            break;
                                        }
                                        case "Error": {
                                            String errorType = jsonObject.get("error_type").getAsString();
                                            switch (errorType) {
                                                case "UserNotExist": {
                                                    AlertClass.showTopMessage(SignupMoreInfo3Activity.this, findViewById(R.id.container), "AuthProfile", errorType, "error", null);
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
                                new PostProfileTask().execute(params);
                            } else {
                                if (SignupMoreInfo3Activity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                    SignupMoreInfo3Activity.this.retryCounter += 1;
                                    new PostProfileTask().execute(params);
                                } else {
                                    SignupMoreInfo3Activity.this.retryCounter = 0;
                                    AlertClass.showTopMessage(SignupMoreInfo3Activity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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

                            if (SignupMoreInfo3Activity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                SignupMoreInfo3Activity.this.retryCounter += 1;
                                new PostProfileTask().execute(params);
                            } else {
                                SignupMoreInfo3Activity.this.retryCounter = 0;
                                if (networkErrorType != null) {
                                    switch (networkErrorType) {
                                        case NoInternetAccess:
                                        case HostUnreachable: {
                                            AlertClass.showTopMessage(SignupMoreInfo3Activity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                            break;
                                        }
                                        default: {
                                            AlertClass.showTopMessage(SignupMoreInfo3Activity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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
                    loadingProgress = AlertClass.showLoadingMessage(SignupMoreInfo3Activity.this);
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


}
