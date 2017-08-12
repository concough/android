package com.concough.android.concough;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.concough.android.rest.ProfileRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.GradeType;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.structures.ProfileStruct;
import com.concough.android.utils.KeyChainAccessProxy;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class SettingActivity extends AppCompatActivity {
    private final String TAG = "SettingActivity";

    private RecyclerView recyclerView;
    private RecycleAdapter recycleAdapter;

    private AlertDialog showedAlertDialog;
    AlertDialog.Builder alertDialog;


    private GradeType gradeType = null;
    //    public static SignupMoreInfoStruct signupInfo = null;
    private GradeType names[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        recyclerView = (RecyclerView) findViewById(R.id.settingA_recycleView);
        recycleAdapter = new RecycleAdapter(getApplicationContext());
        recyclerView.setAdapter(recycleAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        names = GradeType.values();


//        this.signupInfo = new SignupMoreInfoStruct();

//        names = GradeType.values();
//        this.selectedGradeType = names[0];

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.setting_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settingMenuI_help:
                String url = getResources().getString(R.string.settingMenu_S_url);
                Intent i = SettingsWebViewActivity.newIntent(SettingActivity.this, url);
                startActivity(i);
                return true;

        }
        return false;
    }

    private class AlertDialogCustomize extends ArrayAdapter<GradeType> {
        private GradeType[] objects;

        public AlertDialogCustomize(@NonNull Context context, @LayoutRes int resource, @NonNull GradeType[] objects) {
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

            GradeType i = this.getItem(position);
            if (i != null) {
                TextView v = (TextView) convertView.findViewById(R.id.item1);
                v.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                v.setText(i.toString());

            }

            return convertView;
        }


    }

    private void postProfile(String date) {
        new PostProfileGradeTask().execute(date);

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
                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    String status = jsonObject.get("status").getAsString();
                                    Date modifiedDate = null;


                                    if (status == "OK") {
                                        String modified = jsonObject.get("modified").getAsString();
                                        try {
                                            modifiedDate = FormatterSingleton.getInstance().getUTCShortDateFormatter().parse(modified);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        UserDefaultsSingleton.getInstance(getApplicationContext()).updateGrade(params[0], modifiedDate);
                                        SettingActivity.this.recycleAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
                    return null;
                }
            }, new Function1<NetworkErrorType, Unit>() {
                @Override
                public Unit invoke(NetworkErrorType networkErrorType) {
                    return null;
                }
            });
            return null;
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
        LOGOUT(8);

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
            }


            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            String text;
            int image;
            String url;

            switch (position) {
                case 0:
                    ((UserInforViewHolder) holder).setupHolder();
                    break;

                case 1:
                    ((UserEditViewHolder) holder).setupHolder();
                    break;

                case 2:
                    text = getResources().getString(R.string.settingA_S_changePassword);
                    image = R.drawable.password_filled_100;
                    ((LinksViewHolder) holder).setupHolder(text, image, null);
                    break;

                case 3:
                    text = getResources().getString(R.string.settingA_S_inviteFriends);
                    image = R.drawable.invite_filled_100;
                    ((LinksViewHolder) holder).setupHolder(text, image, null);
                    break;

                case 4:
                    text = getResources().getString(R.string.settingA_S_clearCache);
                    image = R.drawable.housekeeping_100;
                    ((LinksViewHolder) holder).setupHolder(text, image, null);
                    break;

                case 5:
                    text = getResources().getString(R.string.settingA_S_bugReport);
                    image = R.drawable.bug_filled_100;
                    ((WithArrowViewHolder) holder).setupHolder(text, image, null);
                    break;

                case 6:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String url = getResources().getString(R.string.settingA_S_aboutUs_url);
                            Intent i = SettingsWebViewActivity.newIntent(SettingActivity.this, url);
                            startActivity(i);
                        }
                    });
                    text = getResources().getString(R.string.settingA_S_aboutUs);
                    image = R.drawable.about_filled_100;
                    ((WithArrowViewHolder) holder).setupHolder(text, image, null);
                    break;

                case 7:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO: 8/12/2017  SHOW ALERT FOR CONFIRM SIGNOUT ///

                            if (KeyChainAccessProxy.getInstance(getApplicationContext()).clearAllValue() && UserDefaultsSingleton.getInstance(getApplicationContext()).clearAll()) {
                                Intent i = StartupActivity.newIntent(getApplicationContext());
                                startActivity(i);
                                finish();

                            }
                        }
                    });
                    text = getResources().getString(R.string.settingA_S_signout);
                    image = R.drawable.logout_rounded_filled_100;
                    int linkColor = R.color.colorConcoughRedLight;
                    ((LinksViewHolder) holder).setupHolder(text, image, linkColor);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 8;
        }

        @Override
        public int getItemViewType(int position) {
            //super.getItemViewType(position);
            switch (position) {
                case 0:
                    return SettingHolderType.USER_DETAIL.getValue();
                case 1:
                    return SettingHolderType.USER_EDIT.getValue();
                case 2:
                    return SettingHolderType.CHANGE_PASSWORD.getValue();
                case 3:
                    return SettingHolderType.INVITE_FRIENDS.getValue();
                case 4:
                    return SettingHolderType.DELETE_CACHE.getValue();
                case 5:
                    return SettingHolderType.ERROR_REPORT.getValue();
                case 6:
                    return SettingHolderType.ABOUT.getValue();
                case 7:
                    return SettingHolderType.LOGOUT.getValue();
            }

            return 0;

        }

        private class UserInforViewHolder extends RecyclerView.ViewHolder {

            private TextView userName;
            private TextView lastUpdate;

            public UserInforViewHolder(View itemView) {
                super(itemView);

                userName = (TextView) itemView.findViewById(R.id.settingUserInfoL_userName);
                lastUpdate = (TextView) itemView.findViewById(R.id.settingUsereInfoL_lastUpdate);

                userName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                lastUpdate.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
            }

            public void setupHolder() {
                ProfileStruct profile = UserDefaultsSingleton.getInstance(getApplicationContext()).getProfile();

                userName.setText(profile.getFirstname() + " " + profile.getLastname());

                String persianDateString = "";
                Date georgianDate;

                georgianDate = profile.getModified();
                persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);

                lastUpdate.setText("آخرین بروزرسانی: " + persianDateString);
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
                ProfileStruct profile = UserDefaultsSingleton.getInstance(getApplicationContext()).getProfile();

                final String grade = profile.getGrade();
                typeTitle.setText(GradeType.selectWithString(grade).toString());


                editTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


// alert dialog
                        names = GradeType.values();

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
                        customAlertDialogTitle.setTextColor(getResources().getColor(R.color.colorConcoughGray));
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
                        final AlertDialogCustomize adapter = new AlertDialogCustomize(SettingActivity.this, R.layout.cc_alert_dialog_textview, names);
                        lv.setAdapter(adapter);

                        //editBtn.setText(names[0].toString());


                        //SettingActivity.this.selectedGradeType = SettingActivity.this.names[0];
//                        typeTitle.setText(SettingActivity.this.selectedGradeType.name());


//                        View.OnClickListener li = new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                showedAlertDialog.show();
//                            }
//                        };
//                        editBtn.setOnClickListener(li);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                if (showedAlertDialog != null) {
                                    showedAlertDialog.dismiss();
                                    int intId = (int) id;

                                    String gradeName = SettingActivity.this.names[position].name();
                                    SettingActivity.this.postProfile(grade);
                                    typeTitle.setText(gradeName);


                                    //typeTitle.setText(SettingActivity.this.names[intId].toString());

                                    // SettingActivity.this.selectedGradeType = SettingActivity.this.names[intId];
                                    //  SignupMoreInfo1Activity.signupInfo.setGrade(SettingActivity.this.selectedGradeType.name());
                                }

                            }
                        });


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

                textLink.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
            }

            public void setupHolder(String mText, int mIcon, @Nullable Integer textColor) {
                textLink.setText(mText);
                iconImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), mIcon));

                if (textColor != null) {
                    textLink.setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                }
            }

        }


        private class WithArrowViewHolder extends RecyclerView.ViewHolder {

            private TextView textLink;
            private ImageView iconImage;

            public WithArrowViewHolder(View itemView) {
                super(itemView);

                textLink = (TextView) itemView.findViewById(R.id.settingLinkL_link);
                iconImage = (ImageView) itemView.findViewById(R.id.settingLinkL_image);

                textLink.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
            }

            public void setupHolder(String mText, int mIcon, @Nullable Integer textColor) {
                textLink.setText(mText);
                iconImage.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), mIcon));

                if (textColor != null) {
                    textLink.setTextColor(textColor);
                }
            }

        }


    }

}



