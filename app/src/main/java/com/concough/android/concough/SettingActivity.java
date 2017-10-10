package com.concough.android.concough;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.concough.android.general.AlertClass;
import com.concough.android.models.EntrancePackageHandler;
import com.concough.android.models.EntranceQuestionStarredModelHandler;
import com.concough.android.models.PurchasedModel;
import com.concough.android.models.PurchasedModelHandler;
import com.concough.android.rest.DeviceRestAPIClass;
import com.concough.android.rest.ProfileRestAPIClass;
import com.concough.android.singletons.DeviceInformationSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.GradeType;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.ProfileStruct;
import com.concough.android.utils.KeyChainAccessProxy;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmResults;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class SettingActivity extends BottomNavigationActivity {
    private final String TAG = "SettingActivity";

    private RecyclerView recyclerView;
    private RecycleAdapter recycleAdapter;

    private AlertDialog showedAlertDialog;
    AlertDialog.Builder alertDialog;

    private KProgressHUD loadingProgress;

//    private GradeType gradeType = null;
//    private GradeType[] names;
    private ArrayList<Pair<String, String>> namesPair;

    private boolean isEditing = false;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_setting;
    }

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, SettingActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setMenuSelectedIndex(3);
        super.onCreate(savedInstanceState);

        recyclerView = (RecyclerView) findViewById(R.id.settingA_recycleView);
        recycleAdapter = new RecycleAdapter(getApplicationContext());
        recyclerView.setAdapter(recycleAdapter);

        namesPair = new ArrayList<>();


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //names = GradeType.values();
        actionBarSet();

    }


    private void actionBarSet() {
        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {
                switch (id) {
                    case R.drawable.help_icon: {
                        String url = getResources().getString(R.string.settingMenu_S_url);
                        Intent i = SettingsWebViewActivity.newIntent(SettingActivity.this, url, "درباره ما");
                        startActivity(i);
                        break;
                    }
                    case R.drawable.share_icon: {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/*");
                        share.putExtra(Intent.EXTRA_TEXT, "**HELLO BABY**  \nhttps://zhycan.com  ");
                        share.putExtra(Intent.EXTRA_SUBJECT, "CONCOUGH SUBJECT");

                        Uri uri = Uri.parse("android.resource://com.concough.android.concough/raw/zhycan_logo");
                        share.putExtra(Intent.EXTRA_STREAM, uri);


                        startActivity(Intent.createChooser(share, "لطفا به اشتراک بزار زودتر"));

                        break;
                    }
                }
            }

            @Override
            public void OnBackClicked() {

            }
        };

        ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();

        ButtonDetail buttonDetail = new ButtonDetail();
        buttonDetail.imageSource = R.drawable.help_icon;
        buttonDetailArrayList.add(buttonDetail);

        buttonDetail = new ButtonDetail();
        buttonDetail.imageSource = R.drawable.share_icon;
        buttonDetailArrayList.add(buttonDetail);

        super.createActionBar("تنظیمات", false, buttonDetailArrayList);
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

//            GradeType i = this.getItem(position);
            String i = objects.get(position);
            if (i != null) {
                TextView v = (TextView) convertView.findViewById(R.id.item1);
                v.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                v.setText(i.toString());

            }

            return convertView;
        }


    }

    private void postProfile(String grade, String gradeString) {
        new PostProfileGradeTask().execute(grade, gradeString);
    }


    private void getProfileGradeList() {
        if (SettingActivity.this.namesPair.size() > 0) {
            SettingActivity.this.changeGradeButtonClicked();
        } else {
            new GetProfileGradeListTask().execute();
        }
    }

    private class PostProfileGradeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {

            ProfileRestAPIClass.putProfileGrade(params[0], getApplicationContext(), new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    Date modifiedDate = null;


                                    if (status.equals("OK")) {
                                        String modified = jsonObject.get("modified").getAsString();
                                        try {
                                            modifiedDate = FormatterSingleton.getInstance().getUTCShortDateFormatter().parse(modified);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        UserDefaultsSingleton.getInstance(getApplicationContext()).updateGrade(params[0], params[1], modifiedDate);
                                        SettingActivity.this.recycleAdapter.notifyDataSetChanged();
                                    } else if (status.equals("Error")) {
                                        String errorType = jsonObject.get("error_type").getAsString();
                                        switch (errorType) {
                                            case "UserNotExist": {
                                                AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "AuthProfile", errorType, "error", null);
                                                break;
                                            }
                                            default:
                                                break;
                                        }
                                    }
                                }
                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new PostProfileGradeTask().execute(params);
                            } else {
                                AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                    AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                    break;
                                }
                                default: {
                                    AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

            loadingProgress = AlertClass.showLoadingMessage(SettingActivity.this);
            loadingProgress.show();

        }

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
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    Date modifiedDate = null;

                                    if (status.equals("OK")) {
                                        JsonArray dataArray = jsonObject.get("record").getAsJsonArray();
                                        Pair<String, String> tempPair;
                                        JsonObject jo;
                                        for (JsonElement je : dataArray) {
                                            jo = je.getAsJsonObject();
                                            tempPair = new Pair<String, String>(jo.get("code").getAsString(), jo.get("title").getAsString());
                                            SettingActivity.this.namesPair.add(tempPair);
                                        }

                                        SettingActivity.this.changeGradeButtonClicked();

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
                                AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                    AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                    break;
                                }
                                default: {
                                    AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

            loadingProgress = AlertClass.showLoadingMessage(SettingActivity.this);
            loadingProgress.show();

        }

    }

    private void changeGradeButtonClicked() {

        ArrayList<String> gradeArray = new ArrayList<String>();
        for (Pair<String, String> pair : namesPair) {
            gradeArray.add(pair.second);
        }


        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.cc_alert_dialog_listview, null);

        alertDialog = new AlertDialog.Builder(SettingActivity.this);
        alertDialog.setView(convertView);

        showedAlertDialog = alertDialog.create();


        TextView customAlertDialogTitle = new TextView(SettingActivity.this);
        customAlertDialogTitle.setGravity(Gravity.CENTER);
        customAlertDialogTitle.setTextColor(Color.BLACK);
        customAlertDialogTitle.setPadding(0, 50, 0, 10);
        customAlertDialogTitle.setText("لطفا یکی از گزینه های زیر را انتخاب نمایید");
        customAlertDialogTitle.setTextSize(14);
        customAlertDialogTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray));
        customAlertDialogTitle.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


        showedAlertDialog.setCustomTitle(customAlertDialogTitle);
        showedAlertDialog.show();
        showedAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SettingActivity.this.recycleAdapter.notifyDataSetChanged();
            }
        });

        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        final AlertDialogCustomize adapter = new AlertDialogCustomize(SettingActivity.this, R.layout.cc_alert_dialog_textview, gradeArray);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (showedAlertDialog != null) {
                    showedAlertDialog.dismiss();

                    SettingActivity.this.postProfile(SettingActivity.this.namesPair.get(position).first,SettingActivity.this.namesPair.get(position).second);

                }

            }
        });

    }

    private void acquireMe(Boolean isLogout) {
        new AcquireTask().execute(isLogout);
    }

    private class AcquireTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(final Boolean... params) {
            DeviceRestAPIClass.deviceAcquire(getApplicationContext(), new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();

                                    if (status.equals("OK")) {
                                        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();

                                        String device_name = "android";
                                        String device_model = Build.MANUFACTURER + " " + Build.MODEL;
                                        Boolean is_me = true;

                                        if (jsonObject.has("data")) {
                                            device_name = jsonObject.get("data").getAsJsonObject().get("device_name").getAsString();
                                            device_model = jsonObject.get("data").getAsJsonObject().get("device_model").getAsString();
                                            is_me = false;
                                        }

                                        DeviceInformationSingleton.getInstance(getApplicationContext()).setDeviceState(username, device_name, device_model, false, is_me);

                                        // navigate to startup
                                        if (!params[0]) {
                                            Intent intent = StartupActivity.newIntent(SettingActivity.this);
                                            SettingActivity.this.startActivity(intent);
                                            SettingActivity.this.finish();
                                            return;
                                        }

                                    } else if (status.equals("Error")) {
                                        String errorType = jsonObject.get("error_type").getAsString();
                                        switch (errorType) {
                                            case "DeviceNotRegistered":
                                            case "UserNotExist": {
                                                break;
                                            }
                                            default:
                                                break;
                                        }
                                    }
                                }

                                if (params[0]) {
                                    String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                    if (KeyChainAccessProxy.getInstance(getApplicationContext()).clearAllValue() && UserDefaultsSingleton.getInstance(getApplicationContext()).clearAll()) {
                                        TokenHandlerSingleton.getInstance(getApplicationContext()).invalidateTokens();
                                        DeviceInformationSingleton.getInstance(getApplicationContext()).clearAll(username);
                                        Intent i = StartupActivity.newIntent(getApplicationContext());
                                        startActivity(i);
                                        finish();
                                    }

                                }
                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new AcquireTask().execute(params);
                            } else {
                                AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
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
                                    AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                    break;
                                }
                                default: {
                                    AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
                                    break;
                                }

                            }

                            if (params[0]) {
                                String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                if (KeyChainAccessProxy.getInstance(getApplicationContext()).clearAllValue() && UserDefaultsSingleton.getInstance(getApplicationContext()).clearAll()) {
                                    TokenHandlerSingleton.getInstance(getApplicationContext()).invalidateTokens();
                                    DeviceInformationSingleton.getInstance(getApplicationContext()).clearAll(username);
                                    Intent i = StartupActivity.newIntent(getApplicationContext());
                                    startActivity(i);
                                    finish();
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

            loadingProgress = AlertClass.showLoadingMessage(SettingActivity.this);
            loadingProgress.show();

        }

    }

    private enum SettingHolderType {
        USER_DETAIL(1),
        USER_EDIT(2),
        CHANGE_PASSWORD(3),
        INVITE_FRIENDS(4),
        DELETE_CACHE(5),
        ERROR_REPORT(6),
        ABOUT(7),
        LOGOUT(8),
        LOCK(9);

        private final int value;

        private SettingHolderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    private class RecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;

        public RecycleAdapter(Context context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = null;
            switch (viewType) {
                case 1:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_userinfo, parent, false);
                    return new UserInforViewHolder(view);
                case 2:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_useredit, parent, false);
                    return new UserEditViewHolder(view);
                case 3:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_link_blue, parent, false);
                    return new LinksViewHolder(view);
                case 4:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_link_blue, parent, false);
                    return new LinksViewHolder(view);
                case 5:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_link_blue, parent, false);
                    return new LinksViewHolder(view);
                case 6:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_link_arrow, parent, false);
                    return new WithArrowViewHolder(view);
                case 7:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_link_arrow, parent, false);
                    return new WithArrowViewHolder(view);
                case 8:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_link_blue, parent, false);
                    return new LinksViewHolder(view);
                case 9:
                    view = LayoutInflater.from(this.context).inflate(R.layout.cc_setting_link_blue, parent, false);
                    return new LinksViewHolder(view);
            }


            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            String text;
            int image;
            String url;


            if (SettingActivity.this.isEditing == false) {
                switch (position) {
                    case 0:
                        ((UserInforViewHolder) holder).setupHolder();
                        break;

                    case 1:
                        ((UserEditViewHolder) holder).setupHolder();
                        break;

                    case 2:
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = ErrorReportActivity.newIntent(SettingActivity.this);
                                startActivity(i);
                            }
                        });
                        text = getResources().getString(R.string.settingA_S_bugReport);
                        image = R.drawable.bug_filled_100;
                        ((WithArrowViewHolder) holder).setupHolder(text, image, null, 0);
                        break;

                    case 3:
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = getResources().getString(R.string.settingA_S_aboutUs_url);
                                Intent i = SettingsWebViewActivity.newIntent(SettingActivity.this, url, "درباره ما");
                                startActivity(i);
                            }
                        });
                        text = getResources().getString(R.string.settingA_S_aboutUs);
                        image = R.drawable.about_filled_100;
                        ((WithArrowViewHolder) holder).setupHolder(text, image, null, null);
                        break;

                    case 4:
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SettingActivity.this.acquireMe(false);
                            }
                        });
                        text = getResources().getString(R.string.settingA_S_lockDevice);
                        image = R.drawable.lock_icon;
                        ((LinksViewHolder) holder).setupHolder(text, image, null, 5);
                        break;
                }

            } else {
                switch (position) {
                    case 0:
                        ((UserInforViewHolder) holder).setupHolder();
                        break;

                    case 1:
                        ((UserEditViewHolder) holder).setupHolder();
                        break;

                    case 2:
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = SettingChangePasswordActivity.newIntent(SettingActivity.this);
                                startActivity(i);
                            }
                        });
                        text = getResources().getString(R.string.settingA_S_changePassword);
                        image = R.drawable.password_filled_100;
                        ((LinksViewHolder) holder).setupHolder(text, image, null, 5);
                        break;
//
//                    case 3:
//                        text = getResources().getString(R.string.settingA_S_inviteFriends);
//                        image = R.drawable.invite_filled_100;
//                        ((LinksViewHolder) holder).setupHolder(text, image, null, 5);
//                        break;

                    case 3:
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertClass.showAlertMessageCustom(SettingActivity.this, "آیا مطمین هستید؟", "تنها اطلاعات محصولات حذف خواهند شد و مجددا قابل بارگزاری است", "بله", "خیر", new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                        RealmResults<PurchasedModel> items = null;
                                        if (username != null) {
                                            items = PurchasedModelHandler.getAllPurchased(getApplicationContext(), username);
                                        }

                                        if (items != null) {
                                            for (PurchasedModel item : items) {
                                                SettingActivity.this.deletePurchaseData(item.productUniqueId);

                                                if (PurchasedModelHandler.resetDownloadFlags(getApplicationContext(), username, item.id)) {
                                                    EntrancePackageHandler.removePackage(getApplicationContext(), username, item.productUniqueId);
                                                    EntranceQuestionStarredModelHandler.removeByEntranceId(getApplicationContext(), username, item.productUniqueId);
                                                }
                                            }
                                            SettingActivity.this.recycleAdapter.notifyDataSetChanged();
                                            AlertClass.showTopMessage(SettingActivity.this, findViewById(R.id.container), "ActionResult", "FreeMemorySuccess", "success", null);
                                        }

                                        return null;
                                    }
                                });


                            }

                        });
                        text = getResources().getString(R.string.settingA_S_clearCache);
                        image = R.drawable.housekeeping_100;
                        ((LinksViewHolder) holder).setupHolder(text, image, null, 5);
                        break;


                    case 5:
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertClass.showAlertMessageCustom(SettingActivity.this, "آیا مطمین هستید؟", "خروج موقت از سیستم!", "بله", "خیر", new Function0<Unit>() {
                                    @Override
                                    public Unit invoke() {
                                        SettingActivity.this.acquireMe(true);

                                        return null;
                                    }
                                });


                            }

                        });
                        text = getResources().getString(R.string.settingA_S_signout);
                        image = R.drawable.logout_rounded_filled_100;
                        int linkColor = R.color.colorConcoughRedLight;
                        ((LinksViewHolder) holder).setupHolder(text, image, linkColor, 5);
                        break;

                    case 4:
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SettingActivity.this.acquireMe(false);
                            }
                        });
                        text = getResources().getString(R.string.settingA_S_lockDevice);
                        image = R.drawable.lock_icon;
                        ((LinksViewHolder) holder).setupHolder(text, image, null, 5);
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            if (SettingActivity.this.isEditing == false) {
                return 5;

            } else {
                return 6;

            }
        }

        @Override
        public int getItemViewType(int position) {
            //super.getItemViewType(position);

            if (SettingActivity.this.isEditing == false) {
                switch (position) {
                    case 0:
                        return SettingHolderType.USER_DETAIL.getValue();
                    case 1:
                        return SettingHolderType.USER_EDIT.getValue();
                    case 2:
                        return SettingHolderType.ERROR_REPORT.getValue();
                    case 3:
                        return SettingHolderType.ABOUT.getValue();
                    case 4:
                        return SettingHolderType.LOCK.getValue();
                }
            } else {
                switch (position) {
                    case 0:
                        return SettingHolderType.USER_DETAIL.getValue();
                    case 1:
                        return SettingHolderType.USER_EDIT.getValue();
                    case 2:
                        return SettingHolderType.CHANGE_PASSWORD.getValue();
                    case 3:
                        return SettingHolderType.DELETE_CACHE.getValue();
                    case 5:
                        return SettingHolderType.LOGOUT.getValue();
                    case 4:
                        return SettingHolderType.LOCK.getValue();
                }
            }


            return 0;

        }

        private class UserInforViewHolder extends RecyclerView.ViewHolder {

            private TextView userName;
            private TextView lastUpdate;
            private TextView editInformation;

            public UserInforViewHolder(View itemView) {
                super(itemView);

                userName = (TextView) itemView.findViewById(R.id.settingUserInfoL_userName);
                lastUpdate = (TextView) itemView.findViewById(R.id.settingUsereInfoL_lastUpdate);
                editInformation = (TextView) itemView.findViewById(R.id.settingUserInfoL_editInformation);

                userName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                lastUpdate.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                editInformation.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
            }

            public void setupHolder() {
                ProfileStruct profile = UserDefaultsSingleton.getInstance(getApplicationContext()).getProfile();

                if (profile != null) {
                    userName.setText(profile.getFirstname() + " " + profile.getLastname());
                    String persianDateString = "";
                    Date georgianDate;

                    georgianDate = profile.getModified();
                    persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);

                    lastUpdate.setText("آخرین بروزرسانی: " + persianDateString);
                }

                editInformation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SettingActivity.this.isEditing == false) {
                            SettingActivity.this.isEditing = true;
                            editInformation.setText("اتمام ویرایش");
                            SettingActivity.this.recycleAdapter.notifyDataSetChanged();

                        } else {
                            SettingActivity.this.isEditing = false;
                            editInformation.setText("ویرایش اطلاعات");
                            SettingActivity.this.recycleAdapter.notifyDataSetChanged();
                        }

                    }
                });


            }

        }


        private class UserEditViewHolder extends RecyclerView.ViewHolder {

            private TextView typeTitle;
            private TextView labelType;
            private TextView editTv;

            public UserEditViewHolder(View itemView) {
                super(itemView);


                typeTitle = (TextView) itemView.findViewById(R.id.settingUsereEditL_type);
                labelType = (TextView) itemView.findViewById(R.id.settingUserEditL_label);
                editTv = (TextView) itemView.findViewById(R.id.settingUserEditL_button);

                typeTitle.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                labelType.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                editTv.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder() {
                if (SettingActivity.this.isEditing == false) {
                    editTv.setVisibility(View.INVISIBLE);
                } else {
                    editTv.setVisibility(View.VISIBLE);
                }

                ProfileStruct profile = UserDefaultsSingleton.getInstance(getApplicationContext()).getProfile();


                if (profile != null) {
                    final String grade = profile.getGrade();
                    final String gradeString = profile.getGradeString();
                    typeTitle.setText(gradeString);
                } else {
                    AlertClass.showAlertMessageCustom(SettingActivity.this, "عدم دسترسی به اطلاعات کاربری",
                            "لطفا جهت رفع مشکل از بخش تنظیمات، گزینه خروج را انتخاب کنید و مجددا وارد شوید",
                            "خروج",
                            "متوجه شدم",
                            new Function0<Unit>() {
                                @Override
                                public Unit invoke() {
                                    if (KeyChainAccessProxy.getInstance(getApplicationContext()).clearAllValue() && UserDefaultsSingleton.getInstance(getApplicationContext()).clearAll()) {
                                        TokenHandlerSingleton.getInstance(getApplicationContext()).invalidateTokens();

                                        Intent i = StartupActivity.newIntent(getApplicationContext());
                                        startActivity(i);
                                        finish();
                                    }
                                    return null;
                                }
                            }
                    );

                }


                editTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    SettingActivity.this.getProfileGradeList();
                    }
                });

            }

        }


        private class LinksViewHolder extends RecyclerView.ViewHolder {

            private TextView textLink;
            private ImageView iconImage;

            public LinksViewHolder(View itemView) {
                super(itemView);

                textLink = (TextView) itemView.findViewById(R.id.settingLinkL_link);
                iconImage = (ImageView) itemView.findViewById(R.id.settingLinkL_image);

                textLink.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(String mText, @Nullable Integer mIcon, @Nullable Integer textColor, @Nullable Integer padding) {
                textLink.setText(mText);

                if (mIcon != null) {
                    iconImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), mIcon));
                }

                if (textColor != null) {
                    textLink.setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                }

//                if (padding != null) {
//                    itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingTop(), itemView.getPaddingRight(), padding);
//                }
            }

        }


        private class WithArrowViewHolder extends RecyclerView.ViewHolder {

            private TextView textLink;
            private ImageView iconImage;

            public WithArrowViewHolder(View itemView) {
                super(itemView);

                textLink = (TextView) itemView.findViewById(R.id.settingLinkL_link);
                iconImage = (ImageView) itemView.findViewById(R.id.settingLinkL_image);

                textLink.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(String mText, int mIcon, @Nullable Integer textColor, @Nullable Integer padding) {
                textLink.setText(mText);
                iconImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), mIcon));

                if (textColor != null) {
                    textLink.setTextColor(textColor);
                }

//                if (padding != null) {
//                    itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingTop(), itemView.getPaddingRight(), padding);
//                }


            }

        }


    }

    private void deletePurchaseData(String uniqueId) {
        File f = new File(SettingActivity.this.getFilesDir(), uniqueId);
        if (f.exists() && f.isDirectory()) {
            for (File fc : f.listFiles()) {
                fc.delete();
            }
            boolean rd = f.delete();
        }
    }

}



