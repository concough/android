package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.concough.android.general.AlertClass;
import com.concough.android.rest.ActivityRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.MediaCacheSingleton;
import com.concough.android.structures.ConcoughActivityStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

import static com.concough.android.settings.ConstantsKt.getCONNECTION_MAX_RETRY;
import static com.concough.android.settings.SupportAbilitiesKt.getSUPPORT_ACTIVITY_TYPES;
import static com.concough.android.settings.SupportAbilitiesKt.isSupportActivityTypes;
import static com.concough.android.utils.DataConvertorsKt.monthToString;
import static com.concough.android.extensions.TypeExtensionsKt.timeAgoSinceDate;

public class HomeActivity extends BottomNavigationActivity {

    public static final String TAG = "HomeActivity";
    private static int counter = 0;

    private ArrayList<ConcoughActivityStruct> concoughActivityStructs;
    private boolean moreFeedExist = true;
    RecyclerView recycleView;
    HomeActivityAdapter homeActivityAdapter;
    LinearLayoutManager mLayoutManager;
    private boolean loading = true;
    private KProgressHUD loadingProgress;
    private boolean isRefresh;
    int homaActivityCheck = 0;
    String lastCreatedStr = "";

    private Integer retryCounter = 0;

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

        counter = 0;

        recycleView = (RecyclerView) findViewById(R.id.homeA_recycle);
        homeActivityAdapter = new HomeActivityAdapter(this, new ArrayList<ConcoughActivityStruct>());
        recycleView.setAdapter(homeActivityAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(mLayoutManager);


        recycleView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                Log.d(TAG, "onChildViewAttachedToWindow: " + view);
                RecyclerView.ViewHolder v = recycleView.findContainingViewHolder(view);
                if (v.getClass() == HomeActivityAdapter.ItemHolder.class) {
                    ((HomeActivityAdapter.ItemHolder) v).downloadImageHandler();
                    if (v.getAdapterPosition() >= homeActivityAdapter.getItemCount() - 5) {
                        if (HomeActivity.this.moreFeedExist) {
                            if (!loading) {
                                HomeActivity.this.homeActivity(HomeActivity.this.lastCreatedStr);
                                counter++;
                            }
                        }
                    }
                } else if (v.getClass() == HomeActivityAdapter.ItemMultiHolder.class) {
                    ((HomeActivityAdapter.ItemMultiHolder) v).downloadImageHandler();
                    if (v.getAdapterPosition() >= homeActivityAdapter.getItemCount() - 5) {
                        if (HomeActivity.this.moreFeedExist) {
                            if (!loading) {
                                HomeActivity.this.homeActivity(HomeActivity.this.lastCreatedStr);
                                counter++;
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
//                Log.d(TAG, "onChildViewDetachedFromWindow: "+view);
//              RecyclerView.ViewHolder v =  recycleView.findContainingViewHolder(view);
//              if (v.getClass() == HomeActivityAdapter.ItemHolder.class) {
//                ImageView  img = (ImageView) v.itemView.findViewById(R.id.itemEntranceCreateI_entranceLogo);
//                  Glide.clear(img);
//                  img.setImageResource(R.drawable.no_image);
//
//
//              }
            }
        });

        actionBarSet();
        HomeActivity.this.isRefresh = false;
        homeActivity(null);

        final PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.homeA_swipeRefreshLayout);
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
    }


    @Override
    protected void onPause() {
//        AlertClass.hideLoadingMessage(loadingProgress);
//        loadingProgress = null;
        super.onPause();
    }

    private void actionBarSet() {
        ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();

        ButtonDetail buttonDetail = new ButtonDetail();

        buttonDetail.imageSource = R.drawable.open_box_filled;
        buttonDetailArrayList.add(buttonDetail);

        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {
                switch (id) {
                    case R.drawable.open_box_filled: {
                        Intent i = ArchiveActivity.newIntent(HomeActivity.this);
                        startActivity(i);
                        break;
                    }
                }
            }

            @Override
            public void OnBackClicked() {

            }

            @Override
            public void OnTitleClicked() {
                if (HomeActivity.this.recycleView != null) {
                    HomeActivity.this.recycleView.smoothScrollToPosition(0);
                }
            }
        };

        super.createActionBar("خانه", false, buttonDetailArrayList);
    }

//    private void homeActivity(String date) {
//        HomeActivity.this.loading = true;
//        new HomeActivityTask().execute(date);
//
//        if (date == null) {
//            if (!isFinishing()) {
//                if (loadingProgress == null) {
//                    loadingProgress = AlertClass.showLoadingMessage(HomeActivity.this);
//                    loadingProgress.show();
//                } else {
//                    if (!loadingProgress.isShowing()) {
//                        //loadingProgress = AlertClass.showLoadingMessage(HomeActivity.this);
//                        loadingProgress.show();
//                    }
//                }
//            }
//        }
//
//    }

    private void homeActivity(String date) {
        HomeActivity.this.loading = true;

        if (date == null) {
            super.updateMainTitle("به روز رسانی ...");
        }

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
//                            AlertClass.hideLoadingMessage(loadingProgress);

                            try {
                                if (httpErrorType == HTTPErrorType.Success) {
                                    HomeActivity.super.updateMainTitle("کنکوق");

                                    HomeActivity.this.retryCounter = 0;

                                    if (jsonObject != null) {

                                        JsonArray jsonArray = jsonObject.getAsJsonArray();

                                        if (jsonArray.size() > 0) {
                                            ArrayList<ConcoughActivityStruct> localList = new ArrayList<ConcoughActivityStruct>();
                                            ConcoughActivityStruct concoughActivityStruct;
                                            for (JsonElement item : jsonArray) {

                                                String createdStr = item.getAsJsonObject().get("created").getAsString();
                                                Date created = FormatterSingleton.getInstance().getUTCDateFormatter().parse(createdStr);

                                                String activityType = item.getAsJsonObject().get("activity_type").getAsString();
                                                JsonObject target = item.getAsJsonObject().get("target").getAsJsonObject();

                                                if (isSupportActivityTypes(activityType)) {
                                                    concoughActivityStruct = new ConcoughActivityStruct(created, createdStr, activityType, target);
                                                    localList.add(concoughActivityStruct);
                                                }
                                            }

                                            HomeActivity.this.moreFeedExist = true;

                                            if (aBoolean) {
                                                homeActivityAdapter.setItems(localList);
                                            } else {
                                                homeActivityAdapter.addItem(localList);
                                            }


                                            String lastIndexCreatedStr = localList.get(localList.size() - 1).getCreatedStr();
                                            HomeActivity.this.lastCreatedStr = lastIndexCreatedStr;


//                                            homeActivityAdapter.notifyDataSetChanged();
//                                            homeActivityAdapter.notifyItemRangeChanged(homeActivityAdapter.getItemCount() - 10,10);
                                            homeActivityAdapter.notifyDataSetChanged();
                                            HomeActivity.this.loading = false;

                                        } else {
                                            HomeActivity.this.moreFeedExist = false;
                                            HomeActivity.this.loading = false;
                                            homeActivityAdapter.notifyDataSetChanged();
//                                            homeActivityAdapter.notifyItemRangeChanged(homeActivityAdapter.getItemCount() - 10,10);
                                        }
                                    }

                                } else if (httpErrorType == HTTPErrorType.Refresh) {
                                    new HomeActivityTask().execute(params);
                                } else {
                                    if (HomeActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                        HomeActivity.this.retryCounter += 1;
                                        new HomeActivityTask().execute(params);
                                    } else {
                                        HomeActivity.this.retryCounter = 0;
                                        HomeActivity.super.updateMainTitle("کنکوق");
                                        HomeActivity.this.moreFeedExist = false;
                                        HomeActivity.this.homeActivityAdapter.notifyDataSetChanged();
                                    }
                                }


                            } catch (Exception exc) {
                                HomeActivity.this.loading = false;
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
                            HomeActivity.this.loading = false;
                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (HomeActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                HomeActivity.this.retryCounter += 1;
                                new HomeActivityTask().execute(params);
                            } else {
                                HomeActivity.this.retryCounter = 0;
                                HomeActivity.super.updateMainTitle("کنکوق");
                                HomeActivity.this.moreFeedExist = false;
                                HomeActivity.this.homeActivityAdapter.notifyDataSetChanged();

                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(HomeActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(HomeActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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
    }

    private class MediaTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
//            MediaRestAPIClass.downloadEsetImage(params[0], );
            return null;
        }
    }

    private enum ConcoughActivityType {
        ENTRANCE_CREATE(1), ENTRANCE_UPDATE(2), EMPTY_HOLDER(50), LOADING_HOLDER(51),
        ENTRANCE_MULTI(3);

        private final int value;

        ConcoughActivityType(int value) {
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

        public class ItemHolder extends RecyclerView.ViewHolder {
            private ImageView entranceLogo;
            private TextView dateYear;
            private TextView dateMonth;
            private TextView concourText;
            private TextView entranceType;
            private TextView entranceSetGroup;
            private TextView additionalData;
            private TextView sellCount;
            private TextView dateJalali;

            private JsonObject extraData;
            ConcoughActivityStruct concoughActivityStructLocal;


            public ItemHolder(View itemView) {
                super(itemView);

                concourText = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_concourText);
                dateYear = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_dateYear);
                dateMonth = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_dateMonth);
                entranceType = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_entranceType);
                entranceSetGroup = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_entranceSetGroup);
                additionalData = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_additionalData);
                sellCount = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_sellCount);
                dateJalali = (TextView) itemView.findViewById(R.id.itemEntranceCreateI_dateJalali);
                entranceLogo = (ImageView) itemView.findViewById(R.id.itemEntranceCreateI_entranceLogo);

                concourText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceType.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceSetGroup.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                additionalData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                dateYear.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateMonth.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(ConcoughActivityStruct concoughActivityStruct) {

                concoughActivityStructLocal = concoughActivityStruct;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int dateNumber = concoughActivityStruct.getTarget().getAsJsonObject().get("year").getAsInt();
                        int monthNumber = concoughActivityStruct.getTarget().getAsJsonObject().get("month").getAsInt();

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

//                try {
//                    georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
//                    persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                dateJalali.setText(persianDateString.trim());

                        try {
                            georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
                            String timeAgo = timeAgoSinceDate(georgianDate, "fa", false);
                            dateJalali.setText(timeAgo);
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }

                        dateYear.setText(FormatterSingleton.getInstance().getNumberFormatter().format(dateNumber));
                        dateMonth.setText(monthToString(monthNumber));

                        JsonArray stats = concoughActivityStruct.getTarget().getAsJsonObject().get("stats").getAsJsonArray();
                        Integer sellCountInt = 0;
                        if (stats.size() > 0) {
                            sellCountInt = stats.get(0).getAsJsonObject().get("purchased").getAsInt();
                        }
                        sellCount.setText(FormatterSingleton.getInstance().getNumberFormatter().format(sellCountInt));

                        sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                        entranceType.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString().trim());
                        entranceSetGroup.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString().trim() + " (" + concoughActivityStruct.getTarget().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString().trim() + ")");


//                entranceLogo.setImageResource(R.drawable.no_image);


//
//                String s;
//                s = concoughActivityStruct.getTarget().getAsJsonObject().get("extra_data").getAsString();
//                extraData = new JsonParser().parse(s).getAsJsonObject();
//
//                String extra = "";
//                ArrayList<String> extraArray = new ArrayList<>();
//
//                for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
//                    extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
//                }
//
//                extra = TextUtils.join(" - ", extraArray);
                        additionalData.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString().trim());

                    }
                });
            }

            public void removeImageHandler() {
                entranceLogo.setImageResource(R.drawable.no_image);

            }

            public void downloadImageHandler() {
                int imageId = concoughActivityStructLocal.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
                downloadImage(imageId);
            }

            private void downloadImage(final int imageId) {
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
                byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                if (data != null) {

                    Glide.with(HomeActivity.this)
                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(entranceLogo)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(HomeActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                            if (httpErrorType != HTTPErrorType.Success) {
                                Log.d(TAG, "run: ");
                                if (httpErrorType == HTTPErrorType.Refresh) {
                                    downloadImage(imageId);
                                } else {
//                                    entranceLogo.setImageResource(R.drawable.no_image);
                                }
                            } else {
                                MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);

                                Glide.with(getApplicationContext())

                                        .load(data)
                                        //.crossFade()
                                        .dontAnimate()
                                        .into(entranceLogo)
                                        .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));

                            }
//                                }
//                            });
                            return null;
                        }
                    }, new Function1<NetworkErrorType, Unit>() {
                        @Override
                        public Unit invoke(NetworkErrorType networkErrorType) {
                            return null;
                        }
                    });
                }
            }

        }

        public class ItemMultiHolder extends RecyclerView.ViewHolder {
            private ImageView entranceLogo;
            private TextView concourText;
            private TextView entranceType;
            private TextView entranceSetGroup;
            private TextView additionalData;
            private TextView sellCount;
            private TextView dateJalali;
            private TextView itemCountsTextView;
            private TextView firstYearTextView;
            private TextView lastYearTextView;
            private TextView firstMonthTextView;
            private TextView lastMonthTextView;
            private ImageView countCarouselImageView;

            private JsonObject extraData;
            ConcoughActivityStruct concoughActivityStructLocal;

            public ItemMultiHolder(View itemView) {
                super(itemView);

                concourText = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_concourText);
                entranceType = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_entranceType);
                entranceSetGroup = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_entranceSetGroup);
                additionalData = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_additionalData);
                sellCount = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_sellCount);
                dateJalali = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_dateJalali);
                entranceLogo = (ImageView) itemView.findViewById(R.id.itemEntranceMultiI_entranceLogo);
                itemCountsTextView = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_count);
                firstYearTextView = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_firstYear);
                lastYearTextView = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_lastYear);
                firstMonthTextView = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_firstMonth);
                lastMonthTextView = (TextView) itemView.findViewById(R.id.itemEntranceMultiI_lastMonth);
                countCarouselImageView = (ImageView) itemView.findViewById(R.id.itemEntranceMultiI_carousal);

                concourText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceType.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceSetGroup.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                additionalData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                itemCountsTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                firstYearTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                lastYearTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                firstMonthTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                lastMonthTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

                countCarouselImageView.setColorFilter(ContextCompat.getColor(HomeActivity.this, R.color.colorConcoughGreen));
            }

            public void setupHolder(ConcoughActivityStruct concoughActivityStruct) {
                concoughActivityStructLocal = concoughActivityStruct;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int yearNumber1 = concoughActivityStruct.getTarget().getAsJsonObject().get("entrances").getAsJsonArray().get(0).getAsJsonObject().get("year").getAsInt();
                        int monthNumber1 = concoughActivityStruct.getTarget().getAsJsonObject().get("entrances").getAsJsonArray().get(0).getAsJsonObject().get("month").getAsInt();

                        int arrayCount = concoughActivityStruct.getTarget().getAsJsonObject().get("entrances").getAsJsonArray().size();

                        int yearNumber2 = concoughActivityStruct.getTarget().getAsJsonObject().get("entrances").getAsJsonArray().get(arrayCount - 1).getAsJsonObject().get("year").getAsInt();
                        int monthNumber2 = concoughActivityStruct.getTarget().getAsJsonObject().get("entrances").getAsJsonArray().get(arrayCount - 1).getAsJsonObject().get("month").getAsInt();

                        String currentDateString = concoughActivityStruct.getTarget().getAsJsonObject().get("updated").getAsString();

                        Date georgianDate = null;
//                try {
//                    georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
//                    persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                dateJalali.setText(persianDateString.trim());

                        try {
                            georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
                            String timeAgo = timeAgoSinceDate(georgianDate, "fa", false);
                            dateJalali.setText(timeAgo);
                        } catch (Exception e) {

                        }

                        firstYearTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(yearNumber1));
                        firstMonthTextView.setText(monthToString(monthNumber1));
                        lastYearTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(yearNumber2));
                        lastMonthTextView.setText(monthToString(monthNumber2));

                        itemCountsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(arrayCount));

//                JsonArray stats = concoughActivityStruct.getTarget().getAsJsonObject().get("stats").getAsJsonArray();
//                Integer sellCountInt = 0;
//                if (stats.size() > 0) {
//                    sellCountInt = stats.get(0).getAsJsonObject().get("purchased").getAsInt();
//                }
//                sellCount.setText(FormatterSingleton.getInstance().getNumberFormatter().format(sellCountInt));
//                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

                        entranceType.setText(concoughActivityStruct.getTarget().getAsJsonObject()
                                .get("entrances").getAsJsonArray().get(0).getAsJsonObject()
                                .get("entrance_type").getAsJsonObject().get("title").getAsString().trim());
                        entranceSetGroup.setText(concoughActivityStruct.getTarget().getAsJsonObject()
                                .get("entrances").getAsJsonArray().get(0).getAsJsonObject()
                                .get("entrance_set").getAsJsonObject().get("title").getAsString().trim() +
                                " (" +
                                concoughActivityStruct.getTarget().getAsJsonObject()
                                        .get("entrances").getAsJsonArray().get(0).getAsJsonObject()
                                        .get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString().trim() + ")");

                        additionalData.setText(concoughActivityStruct.getTarget().getAsJsonObject()
                                .get("entrances").getAsJsonArray().get(0).getAsJsonObject()
                                .get("organization").getAsJsonObject().get("title").getAsString().trim());

                    }
                });
            }

            public void removeImageHandler() {
                entranceLogo.setImageResource(R.drawable.no_image);

            }

            public void downloadImageHandler() {
                int imageId = concoughActivityStructLocal.getTarget().getAsJsonObject()
                        .get("entrances").getAsJsonArray().get(0).getAsJsonObject()
                        .get("entrance_set").getAsJsonObject().get("id").getAsInt();
                downloadImage(imageId);
            }

            private void downloadImage(final int imageId) {
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
                byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                if (data != null) {

                    Glide.with(HomeActivity.this)
                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(entranceLogo)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(HomeActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                            if (httpErrorType != HTTPErrorType.Success) {
                                Log.d(TAG, "run: ");
                                if (httpErrorType == HTTPErrorType.Refresh) {
                                    downloadImage(imageId);
                                } else {
//                                    entranceLogo.setImageResource(R.drawable.no_image);
                                }
                            } else {
                                MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);

                                Glide.with(getApplicationContext())

                                        .load(data)
                                        //.crossFade()
                                        .dontAnimate()
                                        .into(entranceLogo)
                                        .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));

                            }
//                                }
//                            });
                            return null;
                        }
                    }, new Function1<NetworkErrorType, Unit>() {
                        @Override
                        public Unit invoke(NetworkErrorType networkErrorType) {
                            return null;
                        }
                    });
                }
            }

        }

        private class EntranceUpdateHolder extends RecyclerView.ViewHolder {
            private de.hdodenhof.circleimageview.CircleImageView entranceLogo;
            private TextView dateYear;
            private TextView dateMonth;
            private TextView entranceType;
            private TextView entranceSetGroup;
            private TextView additionalData;
            private TextView sellCount;
            private TextView dateJalali;

            private JsonObject extraData;


            public EntranceUpdateHolder(View itemView) {
                super(itemView);

                dateYear = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_dateYear);
                dateMonth = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_dateMonth);
                entranceType = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceType);
                entranceSetGroup = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceSetGroup);
                additionalData = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_additionalData);
                sellCount = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_sellCount);
                dateJalali = (TextView) itemView.findViewById(R.id.itemEntranceUpdateI_dateJalali);
                entranceLogo = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.itemEntranceUpdateI_entranceLogo);

                entranceType.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceSetGroup.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                additionalData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                dateYear.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                dateMonth.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(ConcoughActivityStruct concoughActivityStruct) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String datePublishString = concoughActivityStruct.getTarget().getAsJsonObject().get("last_published").getAsString();
                        String lastUpdateString = concoughActivityStruct.getTarget().getAsJsonObject().get("last_update").getAsString();

                        int dateNumber = concoughActivityStruct.getTarget().getAsJsonObject().get("year").getAsInt();
                        int monthNumber = concoughActivityStruct.getTarget().getAsJsonObject().get("month").getAsInt();

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
                            String timeAgo = timeAgoSinceDate(georgianDate, "fa", false);
                            dateJalali.setText(timeAgo);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        dateYear.setText(FormatterSingleton.getInstance().getNumberFormatter().format(dateNumber));
                        dateMonth.setText(monthToString(monthNumber));

                        JsonArray stats = concoughActivityStruct.getTarget().getAsJsonObject().get("stats").getAsJsonArray();
                        Integer sellCountInt = 0;
                        if (stats.size() > 0) {
                            sellCountInt = stats.get(0).getAsJsonObject().get("purchased").getAsInt();
                        }
                        sellCount.setText(FormatterSingleton.getInstance().getNumberFormatter().format(sellCountInt));
                        sellCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


                        entranceType.setText("آزمون " + concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString().trim());
                        entranceSetGroup.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString().trim() + " (" + concoughActivityStruct.getTarget().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString().trim() + ")");

                        int imageId = concoughActivityStruct.getTarget().getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
                        downloadImage(imageId);

//                String s;
//                s = concoughActivityStruct.getTarget().getAsJsonObject().get("extra_data").getAsString();
//                extraData = new JsonParser().parse(s).getAsJsonObject();
//
//                String extra = "";
//                ArrayList<String> extraArray = new ArrayList<>();
//
//                for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
//                    extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
//                }
//
//                extra = TextUtils.join(" - ", extraArray);
                        additionalData.setText(concoughActivityStruct.getTarget().getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString().trim());
                    }
                });
            }

            private void downloadImage(final int imageId) {
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
                final byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                if (data != null) {

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
                    Glide.with(HomeActivity.this)

                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(entranceLogo)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));

//                        }
//                    });


                } else {
                    MediaRestAPIClass.downloadEsetImage(HomeActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                            if (httpErrorType != HTTPErrorType.Success) {
                                Log.d(TAG, "run: ");
                                if (httpErrorType == HTTPErrorType.Refresh) {
                                    downloadImage(imageId);
                                } else {
                                    entranceLogo.setImageResource(R.drawable.no_image);
                                }
                            } else {
                                MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);

                                Glide.with(HomeActivity.this)

                                        .load(data)
                                        //.crossFade()
                                        .dontAnimate()
                                        .into(entranceLogo)
                                        .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));

                            }
//                                }
//                            });
                            return null;
                        }
                    }, new Function1<NetworkErrorType, Unit>() {
                        @Override
                        public Unit invoke(NetworkErrorType networkErrorType) {
                            return null;
                        }
                    });
                }
            }

        }


        private class ItemEmptyHolder extends RecyclerView.ViewHolder {
            private TextView emptyText;
            private ImageView emptyImage;
            private ViewGroup linearLayout;

            public ItemEmptyHolder(View itemView) {
                super(itemView);

                emptyImage = (ImageView) itemView.findViewById(R.id.noItemL_image);
                emptyText = (TextView) itemView.findViewById(R.id.noItemL_text);
                linearLayout = (ViewGroup) itemView.findViewById(R.id.container);

                emptyText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
            }


            public void setupHolder() {
                emptyImage.setImageResource(R.drawable.refresh_empty);
                emptyText.setText("داده ای موجود نیست");

                emptyImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HomeActivity.this.isRefresh = true;
                        homeActivity(null);
                    }
                });
            }

        }

        private class LoadingHolder extends RecyclerView.ViewHolder {
            private ProgressBar progressBar;

            public LoadingHolder(View itemView) {
                super(itemView);

                progressBar = (ProgressBar)itemView.findViewById(R.id.loadingMoreProgressBar);
                progressBar.getIndeterminateDrawable().setColorFilter(
                        ContextCompat.getColor(HomeActivity.this, R.color.colorConcoughGray),
                        PorterDuff.Mode.SRC_IN);
            }

            public void setupHolder() {
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ConcoughActivityType.EMPTY_HOLDER.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_recycle_not_item, parent, false);
                return new ItemEmptyHolder(view);

            } else if (viewType == ConcoughActivityType.ENTRANCE_UPDATE.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_entrance_update, parent, false);
                return new EntranceUpdateHolder(view);
            } else if (viewType == ConcoughActivityType.LOADING_HOLDER.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_recycle_loading, parent, false);
                return new LoadingHolder(view);
            } else if (viewType == ConcoughActivityType.ENTRANCE_MULTI.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_entrance_multi, parent, false);
                return new ItemMultiHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_entrance_create, parent, false);
                return new ItemHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            if (holder.getClass() == ItemEmptyHolder.class) {
                ItemEmptyHolder itemEmptyHolder = (ItemEmptyHolder) holder;
                itemEmptyHolder.setupHolder();
            } else if (holder.getClass() == LoadingHolder.class) {
                LoadingHolder lholder = (LoadingHolder) holder;
                lholder.setupHolder();
            } else {
                final ConcoughActivityStruct oneItem = this.concoughActivityStructList.get(position);
                switch (oneItem.getActivityType()) {
                    case "ENTRANCE_UPDATE": {
                        EntranceUpdateHolder itemHolder = (EntranceUpdateHolder) holder;
                        itemHolder.setupHolder(oneItem);
                        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = EntranceDetailActivity.newIntent(HomeActivity.this,
                                        oneItem.getTarget().getAsJsonObject().get("unique_key").getAsString(),
                                        "Home");
                                startActivity(i);
                            }
                        });
                        break;
                    }
                    case "ENTRANCE_MULTI": {
                        ItemMultiHolder itemHolder = (ItemMultiHolder) holder;
                        itemHolder.setupHolder(oneItem);
                        ImageView img = (ImageView) itemHolder.itemView.findViewById(R.id.itemEntranceMultiI_entranceLogo);
                        img.setImageResource(R.drawable.no_image);

                        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = EntranceMultiDetailActivity.newIntent(HomeActivity.this,
                                        oneItem.getTarget().getAsJsonObject().get("unique_key").getAsString(),
                                        oneItem.getActivityType(),
                                        oneItem.getTarget().toString());
                                startActivity(i);
                            }
                        });
                        break;
                    }
                    default: {
                        ItemHolder itemHolder2 = (ItemHolder) holder;

                        ImageView img = (ImageView) itemHolder2.itemView.findViewById(R.id.itemEntranceCreateI_entranceLogo);
                        img.setImageResource(R.drawable.no_image);

                        itemHolder2.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = EntranceDetailActivity.newIntent(HomeActivity.this, oneItem.getTarget().getAsJsonObject().get("unique_key").getAsString(), "Home");
                                startActivity(i);
                            }
                        });
                        itemHolder2.setupHolder(oneItem);
                    }
                }
            }

        }


        @Override
        public int getItemCount() {
            if (concoughActivityStructList.size() == 0) {
                return 1;
            } else {
                if (moreFeedExist) {
                    return concoughActivityStructList.size() + 1;
                } else {
                    return concoughActivityStructList.size();
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            Log.d(TAG, "position: " + position);
            if (concoughActivityStructList.size() == 0) {
                if (moreFeedExist) {
                    return ConcoughActivityType.LOADING_HOLDER.getValue();
                }
                return ConcoughActivityType.EMPTY_HOLDER.getValue();
            } else {
                if (position == this.concoughActivityStructList.size()) {
                    return ConcoughActivityType.LOADING_HOLDER.getValue();
                } else {
                    switch (concoughActivityStructList.get(position).getActivityType()) {
                        case "ENTRANCE_UPDATE":
                            return ConcoughActivityType.ENTRANCE_UPDATE.getValue();
                        case "ENTRANCE_MULTI":
                            return ConcoughActivityType.ENTRANCE_MULTI.getValue();
                        default:
                            return ConcoughActivityType.ENTRANCE_CREATE.getValue();
                    }
                }
            }
        }
    }
}





