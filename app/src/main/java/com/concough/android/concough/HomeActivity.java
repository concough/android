package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concough.android.rest.ActivityRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.structures.ConcoughActivityStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.PersianCalendar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

import static android.R.attr.key;
import static android.R.attr.value;
import static java.util.stream.Collectors.joining;
import static okhttp3.Protocol.get;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "HomeActivity";

    private ArrayList<ConcoughActivityStruct> concoughActivityStructs;
    private boolean moreFeedExist = true;
    RecyclerView recycleView;
    HomeActivityAdapter homeActivityAdapter;
    int homaActivityCheck = 0;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recycleView = (RecyclerView) findViewById(R.id.homeA_recycle);

        homeActivityAdapter = new HomeActivityAdapter(this, new ArrayList<ConcoughActivityStruct>());
        recycleView.setAdapter(homeActivityAdapter);

        recycleView.setLayoutManager(new LinearLayoutManager(this));


        homeActivity(null);


    }

    private void homeActivity(String date) {
        new HomeActivityTask().execute(date);
    }

    private class HomeActivityTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(final String... params) {
            ActivityRestAPIClass.updateActivity(params[0], getApplicationContext(), new Function3<Boolean, JsonElement, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final Boolean aBoolean, final JsonElement jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {
                                    JsonArray jsonArray = jsonObject.getAsJsonArray();

                                    if (jsonArray.size() > 0) {
                                        ArrayList<ConcoughActivityStruct> localList = new ArrayList<ConcoughActivityStruct>();
                                        ConcoughActivityStruct concoughActivityStruct;
                                        for (JsonElement item : jsonArray) {
                                            try {
                                                String createdStr = item.getAsJsonObject().get("created").getAsString();
                                                Date created = FormatterSingleton.getInstance().getUTCDateFormatter().parse(createdStr);

                                                String activityType = item.getAsJsonObject().get("activity_type").getAsString();
                                                JsonObject target = item.getAsJsonObject().get("target").getAsJsonObject();

                                                concoughActivityStruct = new ConcoughActivityStruct(created, createdStr, activityType, target);
                                                localList.add(concoughActivityStruct);

                                            } catch (Exception exc) {
                                                // TODO: Hanlde error of date cast
                                            }

                                        }
                                        if (aBoolean) {
                                            //homeActivityAdapter.setItems(localList);
                                            //HomeActivity.this.concoughActivityStructs = localList;
                                            moreFeedExist = true;
                                        } else {
                                        }
                                        homeActivityAdapter.addItem(localList);
                                        homeActivityAdapter.notifyDataSetChanged();

                                        if (homaActivityCheck < 5) {
                                            homaActivityCheck++;
                                            String s = null;
                                            new HomeActivityTask().execute(s);

                                        }


                                    } else {
                                        HomeActivity.this.moreFeedExist = false;
                                    }

                                }
                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new HomeActivityTask().execute(params);
                            } else {
                                // TODO: show error with msgType = "HTTPError" and error
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


    private class MediaTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
//            MediaRestAPIClass.downloadEsetImage(params[0], );
            return null;
        }
    }


    private class HomeActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;
        private ArrayList<ConcoughActivityStruct> concoughActivityStructList = new ArrayList<>();

        public HomeActivityAdapter(Context context, ArrayList<ConcoughActivityStruct> concoughActivityStructList) {
            this.context = context;
            this.concoughActivityStructList = concoughActivityStructList;
        }

        public void setItems(ArrayList<ConcoughActivityStruct> concoughActivityStructList) {
            this.concoughActivityStructList = concoughActivityStructList;
        }

        public void addItem(ArrayList<ConcoughActivityStruct> concoughActivityStructList) {
            this.concoughActivityStructList.addAll(concoughActivityStructList);
        }

        private class ItemHolder extends RecyclerView.ViewHolder {
            ImageView entranceLogo;
            TextView dateTopLeft;
            TextView concourText;
            TextView entranceType;
            TextView entranceSetGroup;
            TextView additionalData;
            TextView sellCount;
            TextView dateJalali;

            JsonObject extraData;


            public ItemHolder(View itemView) {
                super(itemView);

                concourText = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_concourText);
                dateTopLeft = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_dateTopLeft);
                entranceType = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_entranceType);
                entranceSetGroup = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_entranceSetGroup);
                additionalData = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_additionalData);
                sellCount = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_sellCount);
                dateJalali = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_dateJalali);

                concourText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                entranceType.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceSetGroup.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                additionalData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(ConcoughActivityStruct concoughActivityStruct) {
                int dateNumber = concoughActivityStruct.getTarget().getAsJsonObject().get("year").getAsInt();


                String datePublishString = concoughActivityStruct.getTarget().getAsJsonObject().get("last_published").getAsString();
                String lastUpdateString = concoughActivityStruct.getTarget().getAsJsonObject().get("last_update").getAsString();


                Date georgianDate = null;
                String persianDateString = "";
                String currentDateString = "";

                if (null != datePublishString) {
                    currentDateString = datePublishString;
                } else {
                    currentDateString = lastUpdateString;
                }

                try {
                    georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
                    persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                dateJalali.setText(persianDateString);


                dateTopLeft.setText(FormatterSingleton.getInstance().getNumberFormatter().format(dateNumber));
                dateTopLeft.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                String sellCountText = FormatterSingleton.getInstance().getNumberFormatter().format(concoughActivityStruct.getTarget().getAsJsonObject().get("stats").getAsJsonArray().get(0).getAsJsonObject().get("purchased").getAsInt());
                sellCount.setText(sellCountText);
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                entranceType.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString() + " " + concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString());
                entranceSetGroup.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString() + " (" + concoughActivityStruct.getTarget().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString() + ")");

                String s;
                s = concoughActivityStruct.getTarget().getAsJsonObject().get("extra_data").getAsString();
                extraData = new JsonParser().parse(s).getAsJsonObject();

                String extra = "";
                ArrayList<String> extraArray = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
                    extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
                }

                extra = TextUtils.join(" - ", extraArray);
                additionalData.setText(extra);
            }
        }



        private class EntranceUpdateHolder extends RecyclerView.ViewHolder {
            ImageView entranceLogo;
            TextView dateTopLeft;
            TextView entranceType;
            TextView entranceSetGroup;
            TextView additionalData;
            TextView sellCount;
            TextView dateJalali;

            JsonObject extraData;


            public EntranceUpdateHolder(View itemView) {
                super(itemView);

                dateTopLeft = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_dateTopLeft);
                entranceType = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceType);
                entranceSetGroup = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceSetGroup);
                additionalData = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_additionalData);
                sellCount = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_sellCount);
                dateJalali = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_dateJalali);

                entranceType.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceSetGroup.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                additionalData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(ConcoughActivityStruct concoughActivityStruct) {
                int dateNumber = concoughActivityStruct.getTarget().getAsJsonObject().get("year").getAsInt();


                String datePublishString = concoughActivityStruct.getTarget().getAsJsonObject().get("last_published").getAsString();
                String lastUpdateString = concoughActivityStruct.getTarget().getAsJsonObject().get("last_update").getAsString();


                Date georgianDate = null;
                String persianDateString = "";
                String currentDateString = "";

                if (null != datePublishString) {
                    currentDateString = datePublishString;
                } else {
                    currentDateString = lastUpdateString;
                }

                try {
                    georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
                    persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                dateJalali.setText(persianDateString);


                dateTopLeft.setText(FormatterSingleton.getInstance().getNumberFormatter().format(dateNumber));
                dateTopLeft.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                String sellCountText = FormatterSingleton.getInstance().getNumberFormatter().format(concoughActivityStruct.getTarget().getAsJsonObject().get("stats").getAsJsonArray().get(0).getAsJsonObject().get("purchased").getAsInt());
                sellCount.setText(sellCountText);
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                entranceType.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString() + " " + concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString());
                entranceSetGroup.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString() + " (" + concoughActivityStruct.getTarget().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString() + ")");

                String s;
                s = concoughActivityStruct.getTarget().getAsJsonObject().get("extra_data").getAsString();
                extraData = new JsonParser().parse(s).getAsJsonObject();

                String extra = "";
                ArrayList<String> extraArray = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
                    extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
                }

                extra = TextUtils.join(" - ", extraArray);
                additionalData.setText(extra);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_entrance_update, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemHolder itemHolder = (ItemHolder) holder;
            ConcoughActivityStruct oneItem = this.concoughActivityStructList.get(position);
            Log.d(TAG, oneItem.toString());
            itemHolder.setupHolder(oneItem);
        }

        @Override
        public int getItemCount() {
            return concoughActivityStructList.size();
        }
    }


}





