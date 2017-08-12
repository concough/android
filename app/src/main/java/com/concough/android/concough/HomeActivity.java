package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.concough.android.rest.ActivityRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.structures.ConcoughActivityStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

public class HomeActivity extends BottomNavigationActivity {

    public static final String TAG = "HomeActivity";


    private ArrayList<ConcoughActivityStruct> concoughActivityStructs;
    private boolean moreFeedExist = true;
    RecyclerView recycleView;
    HomeActivityAdapter homeActivityAdapter;
    LinearLayoutManager mLayoutManager;
    private boolean loading = true;
    private boolean isRefresh;
    int homaActivityCheck = 0;
    String lastCreatedStr = "";

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setMenuSelectedIndex(0);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);

        recycleView = (RecyclerView) findViewById(R.id.homeA_recycle);
        homeActivityAdapter = new HomeActivityAdapter(this, new ArrayList<ConcoughActivityStruct>());
        recycleView.setAdapter(homeActivityAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(mLayoutManager);

        HomeActivity.this.isRefresh = false;

        homeActivity(null);


        final PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.homeA_swipeRefreshLayout);
        // layout.setColorSchemeColors(Color.CYAN);
        layout.setColorSchemeColors(Color.TRANSPARENT, Color.GRAY, Color.GRAY, Color.GRAY);
        layout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layout.setRefreshing(false);
                HomeActivity.this.isRefresh = true;
                homeActivity(null);

            }
        });


            recycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (HomeActivity.this.moreFeedExist) {

                        HomeActivity.this.loading = false;
                        HomeActivity.this.isRefresh = false;
                        HomeActivity.this.homeActivity(HomeActivity.this.lastCreatedStr);
                        Toast.makeText(HomeActivity.this, "Loading new Data ....", Toast.LENGTH_SHORT).show();


                    }
                }
            });



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


                                        HomeActivity.this.moreFeedExist = true;

                                        if(aBoolean) {
                                            homeActivityAdapter.setItems(localList);
                                        } else {
                                            homeActivityAdapter.addItem(localList);
                                        }


                                        String lastIndexCreatedStr = localList.get(localList.size() - 1).getCreatedStr();
                                        HomeActivity.this.lastCreatedStr = lastIndexCreatedStr;


                                        homeActivityAdapter.notifyDataSetChanged();


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

    private enum ConcoughActivityType {
        ENTRANCE_CREATE(1), ENTRANCE_UPDATE(2);

        private final int value;

        private ConcoughActivityType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
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
            private ImageView entranceLogo;
            private TextView dateTopLeft;
            private TextView concourText;
            private TextView entranceType;
            private TextView entranceSetGroup;
            private TextView additionalData;
            private TextView sellCount;
            private TextView dateJalali;

            private JsonObject extraData;


            public ItemHolder(View itemView) {
                super(itemView);

                concourText = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_concourText);
                dateTopLeft = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_dateTopLeft);
                entranceType = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_entranceType);
                entranceSetGroup = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_entranceSetGroup);
                additionalData = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_additionalData);
                sellCount = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_sellCount);
                dateJalali = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_dateJalali);
                entranceLogo = (ImageView) itemView.findViewById(R.id.itemEntranceCreateI_entranceLogo);

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

                int imageId = concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
                MediaRestAPIClass.downloadEsetImage(getApplicationContext(), imageId, entranceLogo, new Function2<JsonObject, HTTPErrorType, Unit>() {
                    @Override
                    public Unit invoke(JsonObject jsonObject, HTTPErrorType httpErrorType) {
                        Log.d(TAG, "invoke: " + jsonObject);
                        return null;
                    }
                }, new Function1<NetworkErrorType, Unit>() {
                    @Override
                    public Unit invoke(NetworkErrorType networkErrorType) {
                        return null;
                    }
                });

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
            private ImageView entranceLogo;
            private TextView dateTopLeft;
            private TextView entranceType;
            private TextView entranceSetGroup;
            private TextView additionalData;
            private TextView sellCount;
            private TextView dateJalali;

            private JsonObject extraData;


            public EntranceUpdateHolder(View itemView) {
                super(itemView);

                dateTopLeft = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_dateTopLeft);
                entranceType = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceType);
                entranceSetGroup = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceSetGroup);
                additionalData = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_additionalData);
                sellCount = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_sellCount);
                dateJalali = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_dateJalali);
                entranceLogo = (ImageView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceLogo);

                entranceType.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceSetGroup.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                additionalData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());


            }

            public void setupHolder(ConcoughActivityStruct concoughActivityStruct) {
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


                int dateNumber = concoughActivityStruct.getTarget().getAsJsonObject().get("year").getAsInt();
                dateTopLeft.setText(FormatterSingleton.getInstance().getNumberFormatter().format(dateNumber));
                dateTopLeft.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                String sellCountText = FormatterSingleton.getInstance().getNumberFormatter().format(concoughActivityStruct.getTarget().getAsJsonObject().get("stats").getAsJsonArray().get(0).getAsJsonObject().get("purchased").getAsInt());
                sellCount.setText(sellCountText);
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                entranceType.setText("کنکور " + concoughActivityStruct.getTarget().getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString() + " " + concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString());
                entranceSetGroup.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString() + " (" + concoughActivityStruct.getTarget().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString() + ")");

                int imageId = concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
                MediaRestAPIClass.downloadEsetImage(getApplicationContext(), imageId, entranceLogo, new Function2<JsonObject, HTTPErrorType, Unit>() {
                    @Override
                    public Unit invoke(JsonObject jsonObject, HTTPErrorType httpErrorType) {
                        Log.d(TAG, "invoke: " + jsonObject);
                        return null;
                    }
                }, new Function1<NetworkErrorType, Unit>() {
                    @Override
                    public Unit invoke(NetworkErrorType networkErrorType) {
                        return null;
                    }
                });

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
            if (viewType == ConcoughActivityType.ENTRANCE_UPDATE.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_entrance_update, parent, false);
                return new EntranceUpdateHolder(view);

            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_entrance_create, parent, false);
                return new ItemHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ConcoughActivityStruct oneItem = this.concoughActivityStructList.get(position);


            switch (oneItem.getActivityType()) {
                case "ENTRANCE_UPDATE":
                    EntranceUpdateHolder itemHolder = (EntranceUpdateHolder) holder;
                    itemHolder.setupHolder(oneItem);
                    itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = EntranceDetailActivity.newIntent(HomeActivity.this, oneItem.getTarget().getAsJsonObject().get("unique_key").getAsString() , "Home" );
                            startActivity(i);
                        }
                    });
                    break;
                default:
                    ItemHolder itemHolder2 = (ItemHolder) holder;
                    itemHolder2.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = EntranceDetailActivity.newIntent(HomeActivity.this, oneItem.getTarget().getAsJsonObject().get("unique_key").getAsString() , "Home" );
                            startActivity(i);
                        }
                    });
                    itemHolder2.setupHolder(oneItem);
            }

        }


        @Override
        public int getItemCount() {
            return concoughActivityStructList.size();
        }

        @Override
        public int getItemViewType(int position) {
            switch (concoughActivityStructList.get(position).getActivityType()) {
                case "ENTRANCE_UPDATE":
                    return ConcoughActivityType.ENTRANCE_UPDATE.getValue();
                default:
                    return ConcoughActivityType.ENTRANCE_CREATE.getValue();
            }
        }


    }
}





