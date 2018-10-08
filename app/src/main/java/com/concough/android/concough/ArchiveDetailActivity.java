package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.concough.android.general.AlertClass;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.rest.ArchiveRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.BasketSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.MediaCacheSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.ArchiveEsetDetailStruct;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.settings.ConstantsKt.getCONNECTION_MAX_RETRY;

public class ArchiveDetailActivity extends BottomNavigationActivity {
    private static String TAG = "ArchiveDetailActivity";

    //private ArrayList<MyStruct> listDetail;
    private RecyclerView recyclerView;

    private TextView setNameText;

    private GetEntranceAdapter adapter;
    private ArrayList<JsonElement> setsList;

    private final static String Detail_Struct = "Detail_Struct";
    private String username = "";
    private Integer retryCounter = 0;

    private KProgressHUD loadingProgress;
    private ArchiveEsetDetailStruct mArchiveEsetDetailStruct;

    public static Intent newIntent(Context packageContext, @Nullable ArchiveEsetDetailStruct detailStruct) {
        Log.d(TAG, "newIntent: ");
        Intent i = new Intent(packageContext, ArchiveDetailActivity.class);
        i.putExtra(Detail_Struct, detailStruct);
        return i;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_archive_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setMenuSelectedIndex(1);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_archive_detail);


        mArchiveEsetDetailStruct = (ArchiveEsetDetailStruct) getIntent().getSerializableExtra(Detail_Struct);

        adapter = new GetEntranceAdapter(this, new ArrayList<JsonElement>());

        recyclerView = (RecyclerView) findViewById(R.id.archiveDetailA_recycle);
        RecyclerView.LayoutManager layoutManagerDetails = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManagerDetails);
        recyclerView.setAdapter(adapter);


        final Integer setId = mArchiveEsetDetailStruct.esetStruct.id;
        getSets(setId);


        final PullRefreshLayout layout = (PullRefreshLayout) findViewById(R.id.homeA_swipeRefreshLayout);
        layout.setColorSchemeColors(Color.TRANSPARENT, Color.GRAY, Color.GRAY, Color.GRAY);
        layout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSets(setId);
                layout.setRefreshing(false);
            }
        });

        username = UserDefaultsSingleton.getInstance(ArchiveDetailActivity.this).getUsername();


    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarSet();
        adapter.notifyDataSetChanged();


        BasketSingleton.getInstance().setListener(new BasketSingleton.BasketSingletonListener() {
            @Override
            public void onRemoveFailed(int position) {
                RecyclerView.ViewHolder holder = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(position);
                if (holder.getClass() == GetEntranceAdapter.ItemsHolder.class) {
                    ((GetEntranceAdapter.ItemsHolder)holder).changedBuyState();
                    ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
                }

            }

            @Override
            public void onAddFailed(int position) {
                RecyclerView.ViewHolder holder = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(position);
                if (holder.getClass() == GetEntranceAdapter.ItemsHolder.class) {
                    ((GetEntranceAdapter.ItemsHolder)holder).changedBuyState();
                    ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onAddCompleted(final int count, final int position) {
                if (count == 1) {
                    ArchiveDetailActivity.this.actionBarSet();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
                        ArchiveDetailActivity.super.updateBadge(0, count);

                    }
                });

            }

            @Override
            public void onCreateFailed(@org.jetbrains.annotations.Nullable Integer position) {
                RecyclerView.ViewHolder holder = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(position);
                if (holder.getClass() == GetEntranceAdapter.ItemsHolder.class) {
                    ((GetEntranceAdapter.ItemsHolder)holder).changedBuyState();
                    ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onCheckoutRedirect(String payUrl, String authority) {

            }

            @Override
            public void onLoadItemCompleted(int count) {

            }

            @Override
            public void onCreateCompleted(Integer position) {
                ((GetEntranceAdapter.ItemsHolder) ArchiveDetailActivity.this.recyclerView.findViewHolderForLayoutPosition(position)).addSaleToBasket(position);
            }

            @Override
            public void onRemoveCompleted(final int count, final int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
                        ArchiveDetailActivity.super.updateBadge(0, count);

                    }
                });
            }

            @Override
            public void onCheckout(int count, HashMap<Integer, BasketSingleton.PurchasedItem> purchased) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        if (loadingProgress != null && loadingProgress.isShowing()) {
            AlertClass.hideLoadingMessage(loadingProgress);
        }
        super.onDestroy();
    }

    private void actionBarSet() {

        final ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();

        ButtonDetail buttonDetail;
        buttonDetail = new ButtonDetail();
        if (BasketSingleton.getInstance().getSalesCount() > 0) {
            buttonDetail.hasBadge = true;
            buttonDetail.badgeCount = BasketSingleton.getInstance().getSalesCount();
            buttonDetail.imageSource = R.drawable.buy_icon;
            buttonDetailArrayList.add(buttonDetail);
        }

        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {
                switch (id) {
                    case R.drawable.buy_icon: {
                        Intent i = BasketCheckoutActivity.newIntent(ArchiveDetailActivity.this, "EntranceDetail");
                        ArchiveDetailActivity.this.startActivity(i);
                        break;
                    }
                }
            }

            @Override
            public void OnBackClicked() {
                onBackPressed();
            }
        };

        String title = mArchiveEsetDetailStruct.typeTitle;

        super.createActionBar(title, true, buttonDetailArrayList);
    }

    private void getSets(Integer setId) {
        new GetEntranceSetsTask().execute(setId);
    }

    private class GetEntranceSetsTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(final Integer... params) {

            ArchiveRestAPIClass.getEntrances(getApplicationContext(), params[0], new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (jsonObject != null) {
                                if (httpErrorType == HTTPErrorType.Success) {
                                    ArchiveDetailActivity.this.retryCounter = 0;
                                    String status = jsonObject.get("status").getAsString();

                                    switch (status) {
                                        case "OK": {
                                            JsonObject json = jsonObject.getAsJsonObject();
                                            JsonArray leaders = json.getAsJsonArray("record");


                                            ArrayList<JsonElement> localList = new ArrayList<JsonElement>();
                                            if (leaders != null) {
                                                for (JsonElement je : leaders) {
                                                    //je = je.getAsJsonObject().get("organization");
                                                    if (je != null) {
                                                        //String j = je.getAsJsonObject().get("title").getAsString();
                                                        localList.add(je);
                                                    }
                                                }
                                            }


                                            ArchiveDetailActivity.this.adapter.setItems(localList);
                                            ArchiveDetailActivity.this.adapter.notifyDataSetChanged();
                                            break;
                                        }
                                        case "Error": {
                                            String errorType = jsonObject.get("error_type").getAsString();

                                            switch (errorType) {
                                                case "EmptyArray": {

                                                    ArchiveDetailActivity.this.adapter.setItems(new ArrayList<JsonElement>());
                                                    ArchiveDetailActivity.this.adapter.notifyDataSetChanged();

                                                    break;
                                                }
                                            }
                                            break;
                                        }

                                    }


//                                    }
                                } else {
                                    if (httpErrorType == HTTPErrorType.Refresh) {
                                        getSets(params[0]);
                                    } else {
                                        if (ArchiveDetailActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                            ArchiveDetailActivity.this.retryCounter += 1;
                                            getSets(params[0]);
                                        } else {
                                            ArchiveDetailActivity.this.retryCounter = 0;
                                            AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                        }
                                    }
                                }
                            }
                        }
                    });
                    return null;
                }
            }, new Function1<NetworkErrorType, Unit>()

            {
                @Override
                public Unit invoke(final NetworkErrorType networkErrorType) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (ArchiveDetailActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                ArchiveDetailActivity.this.retryCounter += 1;
                                getSets(params[0]);
                            } else {
                                ArchiveDetailActivity.this.retryCounter = 0;
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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
                    loadingProgress = AlertClass.showLoadingMessage(ArchiveDetailActivity.this);
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


    private class GetEntranceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private Context context;
        private ArrayList<JsonElement> mArrayList = new ArrayList<>();

        public GetEntranceAdapter(Context context, ArrayList<JsonElement> m) {
            this.context = context;
            this.mArrayList = m;
        }


        public void setItems(ArrayList<JsonElement> arrayList) {
            this.mArrayList = arrayList;
        }

        public void addItem(ArrayList<JsonElement> arrayList) {
            this.mArrayList.addAll(arrayList);
        }


        private class TopItemHolder extends RecyclerView.ViewHolder {
            private TextView setName;
            private TextView code;
            private TextView count;

            private ImageView logoImage;

            private JsonObject extraData;


            public TopItemHolder(View itemView) {
                super(itemView);

                setName = (TextView) itemView.findViewById(R.id.archiveDetailHolder1L_EntranceName);
                code = (TextView) itemView.findViewById(R.id.archiveDetailHolder1L_code);
                count = (TextView) itemView.findViewById(R.id.archiveDetailHolder1L_count);
                logoImage = (ImageView) itemView.findViewById(R.id.settingUserInfoL_userImage);

                setName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                code.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                count.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

            }

            public void setupHolder() {
                String t1 = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.title + " (" + ArchiveDetailActivity.this.mArchiveEsetDetailStruct.groupTitle + ")";
                setName.setText(t1);

                int codeInteger = Integer.valueOf(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.code);

                if (codeInteger == 0) {
                    code.setText("کد: " + "ندارد");
                } else {
                    code.setText("کد: " + FormatterSingleton.getInstance().getNumberFormatter().format(codeInteger));
                }


                Integer entranceCountInteger =  ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.entrance_count;
                String t3 = "آزمون منتشر شده: " + FormatterSingleton.getInstance().getNumberFormatter().format(entranceCountInteger) ;
                count.setText(t3);


                Integer imageId = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.id;

                downloadImage(imageId);

            }


            private void downloadImage(final int imageId) {
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
                byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                if (data != null) {

                    Glide.with(ArchiveDetailActivity.this)

                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(logoImage)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(ArchiveDetailActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
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
                                    logoImage.setImageResource(R.drawable.no_image);
                                }
                            } else {
                                MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);

                                Glide.with(ArchiveDetailActivity.this)

                                        .load(data)
                                        //.crossFade()
                                        .dontAnimate()
                                        .into(logoImage)
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


        private class ItemsHolder extends RecyclerView.ViewHolder {
            private TextView extraDataText;
            private TextView countText;
            private TextView dateJalali;
            //            private TextView typeText;
            private TextView yearText;

            private ImageView doubleCheck;

            private Button entranceBuyButton;

//            private ImageView logoImage;

            private JsonElement extraData;
            private JsonElement jsonElement;

            public ItemsHolder(View itemView) {
                super(itemView);

                extraDataText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_extraDataText);
                countText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_countText);
                dateJalali = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_dateJalali);
//                typeText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_constrantRight);
                yearText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_yearText);
                entranceBuyButton = (Button) itemView.findViewById(R.id.archiveDetailHolder2L_BuyButton);
                doubleCheck = (ImageView) itemView.findViewById(R.id.archiveDetailHolder2L_DoubleCheck);
//                logoImage = (ImageView) itemView.findViewById(R.id.archiveDetailHolder2L_imageRight);


                extraDataText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                countText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
//                typeText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                yearText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceBuyButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

            }

            public void setupHolder(final JsonElement je, final Integer position) {
//                String t1 = jsonElement.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();
//                typeText.setText(t1);

                jsonElement = je;

                doubleCheck.setVisibility(View.GONE);
                entranceBuyButton.setVisibility(View.GONE);

                Integer t2 = jsonElement.getAsJsonObject().get("year").getAsInt();
                yearText.setText(FormatterSingleton.getInstance().getNumberFormatter().format(t2).toString());


                String currentDateString = jsonElement.getAsJsonObject().get("last_published").getAsString();
                Date georgianDate = null;
                String persianDateString = "";

                try {
                    georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
                    persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                dateJalali.setText(persianDateString);

                String t3 = FormatterSingleton.getInstance().getNumberFormatter().format(jsonElement.getAsJsonObject().get("stats").getAsJsonArray().get(0).getAsJsonObject().get("purchased").getAsInt()) + " خرید";
                countText.setText(t3);

//                String s;
//                s = jsonElement.getAsJsonObject().get("extra_data").getAsString();
//                extraData = new JsonParser().parse(s).getAsJsonObject();

                final String sorganizationTitle;
                sorganizationTitle = jsonElement.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();


                final String lastPublished;
                Date lastPublishedDate = new Date();
                lastPublished = jsonElement.getAsJsonObject().get("last_published").getAsString();
                try {
                    lastPublishedDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublished);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

//                String s;
//                s = jsonElement.getAsJsonObject().get("extra_data").getAsString();
//                try {
//                    extraData = new JsonParser().parse(s).getAsJsonObject();
//                } catch (Exception exc) {
//
//                }

                String extraStr = jsonElement.getAsJsonObject().get("extra_data").getAsString();
                extraData = null;
                if (extraStr != null && !"".equals(extraStr)) {
                    try {
                        extraData = new JsonParser().parse(extraStr);
                    } catch (Exception exc) {
                        extraData = new JsonParser().parse("[]");
                    }
                }


//                String extra = "";
//                ArrayList<String> extraArray = new ArrayList<>();
//
//                for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
//                    extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
//                }

//                extra = TextUtils.join(" - ", extraArray);
                extraDataText.setText(sorganizationTitle);


                final String uniqId = jsonElement.getAsJsonObject().get("unique_key").getAsString();

                //final Integer statIndex = BasketSingleton.getInstance().findSaleByTargetId(uniqId, "Entrance");

                if (EntranceModelHandler.existById(ArchiveDetailActivity.this, username, uniqId)) {
                    doubleCheck.setVisibility(View.VISIBLE);

                } else {
                    entranceBuyButton.setVisibility(View.VISIBLE);
                    changedBuyState();
                }
                entranceBuyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        disableBuyButton();
//                        Integer statIndex = BasketSingleton.getInstance().findSaleByTargetId(uniqId, "Entrance");
                        //removeButtonEvent();
                        if (BasketSingleton.getInstance().getBasketId() == null) {
                            BasketSingleton.getInstance().createBasket(getApplicationContext(), position);
                        } else {
                            addSaleToBasket(position);
                        }
                    }
                });

                Integer imageId = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.id;

//                downloadImage(imageId);
            }

            public void addButtonEvent() {
                entranceBuyButton.setEnabled(true);
            }

            public void removeButtonEvent() {
                entranceBuyButton.setEnabled(false);
            }

            public void addSaleToBasket(int position) {

                final String sorganizationTitle;
                sorganizationTitle = jsonElement.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();

                final String uniqId = jsonElement.getAsJsonObject().get("unique_key").getAsString();

                final Integer bookletCount;
                bookletCount = jsonElement.getAsJsonObject().get("booklets_count").getAsInt();

                final Integer entranceYear;
                entranceYear = jsonElement.getAsJsonObject().get("year").getAsInt();

                final Integer entranceDuration;
                entranceDuration = jsonElement.getAsJsonObject().get("duration").getAsInt();

                final String lastPublished;
                Date lastPublishedDate = new Date();
                lastPublished = jsonElement.getAsJsonObject().get("last_published").getAsString();
                try {
                    lastPublishedDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublished);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Integer id = BasketSingleton.getInstance().findSaleByTargetId(uniqId, "Entrance");
                if (id != null && id > 0) {
                    BasketSingleton.getInstance().removeSaleById(ArchiveDetailActivity.this, id, position);
//                    entranceBuyButton.setText("+  سبد خرید");
//                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_style));
//                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughBlue));
                } else {
                    EntranceStruct myLocalEntrance = new EntranceStruct();
                    myLocalEntrance.setEntranceBookletCounts(bookletCount);
                    myLocalEntrance.setEntranceDuration(entranceDuration);
                    myLocalEntrance.setEntranceExtraData(extraData);
                    myLocalEntrance.setEntranceGroupTitle(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.groupTitle);
                    myLocalEntrance.setEntranceLastPublished(lastPublishedDate);
                    myLocalEntrance.setEntranceOrgTitle(sorganizationTitle);
                    myLocalEntrance.setEntranceSetId(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.id);
                    myLocalEntrance.setEntranceSetTitle(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.title);
                    myLocalEntrance.setEntranceTypeTitle(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.typeTitle);
                    myLocalEntrance.setEntranceUniqueId(uniqId);
                    myLocalEntrance.setEntranceYear(entranceYear);

//                    entranceBuyButton.setText("-  سبد خرید");
//                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_red_style));
//                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughRed));

                    BasketSingleton.getInstance().addSale(ArchiveDetailActivity.this, myLocalEntrance, "Entrance", position);
                }

            }

            public void disableBuyButton() {
                entranceBuyButton.setEnabled(false);
                entranceBuyButton.setText("●●●");
                entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_lightgray_style));
                entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughGray));
            }

            public void changedBuyState() {
                final String uniqueId = jsonElement.getAsJsonObject().get("unique_key").getAsString();
                entranceBuyButton.setEnabled(true);
                final Integer statIndex = BasketSingleton.getInstance().findSaleByTargetId(uniqueId, "Entrance");
                if (statIndex != null && statIndex >= 0) {
                    entranceBuyButton.setText("-  سبد خرید");
                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_red_style));
                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughRed));

                } else {
                    entranceBuyButton.setText("+  سبد خرید");
                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_style));
                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughBlue));
                }
            }

//            private void downloadImage(final int imageId) {
//                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
//                byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
//                if (data != null) {
//
//                    Glide.with(ArchiveDetailActivity.this)
//
//                            .load(data)
//                            //.crossFade()
//                            .dontAnimate()
//                            .into(logoImage)
//                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));
//
//
//                } else {
//                    MediaRestAPIClass.downloadEsetImage(ArchiveDetailActivity.this, imageId, logoImage, new Function2<byte[], HTTPErrorType, Unit>() {
//                        @Override
//                        public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
////                            runOnUiThread(new Runnable() {
////                                @Override
////                                public void run() {
//                                    if (httpErrorType != HTTPErrorType.Success) {
//                                        Log.d(TAG, "run: ");
//                                        if (httpErrorType == HTTPErrorType.Refresh) {
//                                            downloadImage(imageId);
//                                        } else {
//                                            logoImage.setImageResource(R.drawable.no_image);
//                                        }
//                                    } else {
//                                        MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);
//
//                                        Glide.with(ArchiveDetailActivity.this)
//
//                                                .load(data)
//                                                //.crossFade()
//                                                .dontAnimate()
//                                                .into(logoImage)
//                                                .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));
//
//                                    }
////                                }
////                            });
//                            return null;
//                        }
//                    }, new Function1<NetworkErrorType, Unit>() {
//                        @Override
//                        public Unit invoke(NetworkErrorType networkErrorType) {
//                            return null;
//                        }
//                    });
//                }
//            }


        }


        @Override
        public int getItemCount() {
            int c = mArrayList.size() + 1;
            return c;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_archivedetail_holder1, parent, false);
                return new TopItemHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_archivedetail_holder2, parent, false);
                return new ItemsHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == 0) {
                TopItemHolder itemHolder = (TopItemHolder) holder;
                itemHolder.setupHolder();
            } else {
                final JsonElement oneItem = mArrayList.get(position - 1);
                ItemsHolder itemHolder = (ItemsHolder) holder;
                itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = EntranceDetailActivity.newIntent(ArchiveDetailActivity.this, oneItem.getAsJsonObject().get("unique_key").getAsString(), "Archive");
                        startActivity(i);
                    }
                });
                itemHolder.setupHolder(oneItem, position);
            }


        }


        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 1;
            } else {
                return 2;
            }
        }

    }


}
