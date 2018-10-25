package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.concough.android.concough.dialogs.EntranceBuyDialog;
import com.concough.android.concough.interfaces.ProductBuyDelegate;
import com.concough.android.general.AlertClass;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.PurchasedModel;
import com.concough.android.models.PurchasedModelHandler;
import com.concough.android.rest.ArchiveRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.rest.WalletRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.MediaCacheSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.ArchiveEntranceStructure;
import com.concough.android.structures.ArchiveEsetDetailStruct;
import com.concough.android.structures.EntranceMultiSaleStruct;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmResults;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.extensions.TypeExtensionsKt.timeAgoSinceDate;
import static com.concough.android.settings.ConstantsKt.getCONNECTION_MAX_RETRY;
import static com.concough.android.utils.DataConvertorsKt.monthToString;

public class ArchiveDetailActivity extends BottomNavigationActivity implements ProductBuyDelegate, Handler.Callback {
    private static String TAG = "ArchiveDetailActivity";
    private final static String DETAIL_STRUCT = "Detail_Struct";
    private final static String HANDLE_THREAD_NAME = "Concough-ArchiveDetailActivity";

    private final static int CREATE_WALLET = 1;

    private RecyclerView recyclerView;

    private GetEntranceAdapter adapter;
    private String username = "";
    private Integer retryCounter = 0;

    private HandlerThread handlerThread = null;
    private Handler handler = null;

    private KProgressHUD loadingProgress;
    private ArchiveEsetDetailStruct mArchiveEsetDetailStruct;
    private ArrayList<EntranceMultiSaleStruct> entranceSaleData = new ArrayList<>();

    public static Intent newIntent(Context packageContext, @Nullable ArchiveEsetDetailStruct detailStruct) {
        Intent i = new Intent(packageContext, ArchiveDetailActivity.class);
        i.putExtra(DETAIL_STRUCT, detailStruct);
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

        mArchiveEsetDetailStruct = (ArchiveEsetDetailStruct) getIntent().getSerializableExtra(DETAIL_STRUCT);

        adapter = new GetEntranceAdapter(this, new ArrayList<>());

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

        this.username = UserDefaultsSingleton.getInstance(ArchiveDetailActivity.this).getUsername();

        // setup handler thread
        this.handlerThread = new HandlerThread(HANDLE_THREAD_NAME);
        this.handlerThread.start();

        Looper looper = this.handlerThread.getLooper();
        if (looper != null) {
            this.handler = new Handler(looper, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarSet();
        adapter.notifyDataSetChanged();

//        BasketSingleton.getInstance().setListener(new BasketSingleton.BasketSingletonListener() {
//            @Override
//            public void onRemoveFailed(int position) {
//                RecyclerView.ViewHolder holder = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(position);
//                if (holder.getClass() == GetEntranceAdapter.ItemsHolder.class) {
//                    ((GetEntranceAdapter.ItemsHolder)holder).changedBuyState();
//                    ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
//                }
//
//            }
//
//            @Override
//            public void onAddFailed(int position) {
//                RecyclerView.ViewHolder holder = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(position);
//                if (holder.getClass() == GetEntranceAdapter.ItemsHolder.class) {
//                    ((GetEntranceAdapter.ItemsHolder)holder).changedBuyState();
//                    ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
//                }
//            }
//
//            @Override
//            public void onAddCompleted(final int count, final int position) {
//                if (count == 1) {
//                    ArchiveDetailActivity.this.actionBarSet();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
//                        ArchiveDetailActivity.super.updateBadge(0, count);
//
//                    }
//                });
//
//            }
//
//            @Override
//            public void onCreateFailed(@org.jetbrains.annotations.Nullable Integer position) {
//                RecyclerView.ViewHolder holder = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(position);
//                if (holder.getClass() == GetEntranceAdapter.ItemsHolder.class) {
//                    ((GetEntranceAdapter.ItemsHolder)holder).changedBuyState();
//                    ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
//                }
//            }
//
//            @Override
//            public void onCheckoutRedirect(String payUrl, String authority) {
//
//            }
//
//            @Override
//            public void onLoadItemCompleted(int count) {
//
//            }
//
//            @Override
//            public void onCreateCompleted(Integer position) {
//                ((GetEntranceAdapter.ItemsHolder) ArchiveDetailActivity.this.recyclerView.findViewHolderForLayoutPosition(position)).addSaleToBasket(position);
//            }
//
//            @Override
//            public void onRemoveCompleted(final int count, final int position) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ArchiveDetailActivity.this.adapter.notifyItemChanged(position);
//                        ArchiveDetailActivity.super.updateBadge(0, count);
//
//                    }
//                });
//            }
//
//            @Override
//            public void onCheckout(int count, HashMap<Integer, BasketSingleton.PurchasedItem> purchased) {
//
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        if (loadingProgress != null && loadingProgress.isShowing()) {
            AlertClass.hideLoadingMessage(loadingProgress);
        }
        this.handlerThread.quit();
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            switch (message.what) {
                case CREATE_WALLET: {
                    this.handleCreateWallet(message);
                    break;
                }
            }
        }
        return true;
    }

    private void actionBarSet() {

        final ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();

//        ButtonDetail buttonDetail;
//        buttonDetail = new ButtonDetail();
//        if (BasketSingleton.getInstance().getSalesCount() > 0) {
//            buttonDetail.hasBadge = true;
//            buttonDetail.badgeCount = BasketSingleton.getInstance().getSalesCount();
//            buttonDetail.imageSource = R.drawable.buy_icon;
//            buttonDetailArrayList.add(buttonDetail);
//        }

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

            @Override
            public void OnTitleClicked() {
                if (ArchiveDetailActivity.this.recyclerView != null) {
                    ArchiveDetailActivity.this.recyclerView.smoothScrollToPosition(0);
                }
            }
        };

        String title = mArchiveEsetDetailStruct.typeTitle;
        super.createActionBar(title, true, buttonDetailArrayList);
    }

    private void handleCreateWallet(Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });

        WalletRestAPIClass.info(ArchiveDetailActivity.this, new Function2<JsonObject, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertClass.hideLoadingMessage(loadingProgress);

                        if (httpErrorType == HTTPErrorType.Success) {
                            AlertClass.hideLoadingMessage(loadingProgress);
                            ArchiveDetailActivity.this.retryCounter = 0;

                            RecyclerView.ViewHolder h = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(message.getData().getInt("POSITION"));
                            if (h.getClass() == GetEntranceAdapter.ItemsHolder.class) {
                                ((GetEntranceAdapter.ItemsHolder) h).changedBuyButtonState(false);
                            }

                            String status = jsonObject.get("status").getAsString();

                            switch (status) {
                                case "OK": {
                                    JsonObject walletRecord = jsonObject.getAsJsonObject("record");
                                    int cash = walletRecord.get("cash").getAsInt();
                                    String updatedStr = walletRecord.get("updated").getAsString();

                                    UserDefaultsSingleton.getInstance(ArchiveDetailActivity.this.getApplicationContext()).setWalletInfo(
                                            cash, updatedStr);

                                    if (UserDefaultsSingleton.getInstance(ArchiveDetailActivity.this.getApplicationContext()).hasWallet()) {
                                        ArchiveDetailActivity.this.adapter.showBuyDialog(message.getData().getInt("POSITION"));
                                    }
                                }
                            }
                        } else {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (ArchiveDetailActivity.this.handler != null) {
                                    ArchiveDetailActivity.this.handler.sendMessage(message);
                                } else {
                                    AlertClass.hideLoadingMessage(loadingProgress);
                                }

                            } else {
                                if (ArchiveDetailActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                    ArchiveDetailActivity.this.retryCounter += 1;

                                    if (ArchiveDetailActivity.this.handler != null) {
                                        ArchiveDetailActivity.this.handler.sendMessage(message);
                                    } else {
                                        AlertClass.hideLoadingMessage(loadingProgress);
                                    }
                                } else {
                                    AlertClass.hideLoadingMessage(loadingProgress);

                                    ArchiveDetailActivity.this.retryCounter = 0;
                                    AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);

                                    RecyclerView.ViewHolder h = ArchiveDetailActivity.this.recyclerView.findViewHolderForAdapterPosition(message.getData().getInt("POSITION"));
                                    if (h.getClass() == GetEntranceAdapter.ItemsHolder.class) {
                                        ((GetEntranceAdapter.ItemsHolder) h).changedBuyButtonState(false);
                                    }
                                }
                            }

                        }
                    }
                });
                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType){
                if (ArchiveDetailActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                    ArchiveDetailActivity.this.retryCounter += 1;

                    if (ArchiveDetailActivity.this.handler != null) {
                        ArchiveDetailActivity.this.handler.sendMessage(message);
                    } else {
                        AlertClass.hideLoadingMessage(loadingProgress);
                    }

                } else {
                    ArchiveDetailActivity.this.retryCounter = 0;
                    AlertClass.hideLoadingMessage(loadingProgress);

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
                return null;
            }
        });
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

//                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (jsonObject != null) {
                                if (httpErrorType == HTTPErrorType.Success) {
                                    ArchiveDetailActivity.this.retryCounter = 0;
                                    String status = jsonObject.get("status").getAsString();

                                    switch (status) {
                                        case "OK": {
                                            JsonObject json = jsonObject.getAsJsonObject();
                                            JsonArray leaders = json.getAsJsonArray("record");
                                            JsonObject type_record = json.getAsJsonObject("entrance_type");

                                            int typeId = type_record.get("id").getAsInt();
                                            for(JsonElement item: type_record.getAsJsonArray("sale_data")) {
                                                int year = item.getAsJsonObject().get("year").getAsInt();
                                                int month = item.getAsJsonObject().get("month").getAsInt();
                                                int cost = item.getAsJsonObject().get("cost").getAsInt();
                                                int costBon = item.getAsJsonObject().get("cost_bon").getAsInt();

                                                EntranceMultiSaleStruct ems = new EntranceMultiSaleStruct(typeId, year,
                                                        month, cost, costBon);
                                                ArchiveDetailActivity.this.entranceSaleData.add(ems);
                                            }


                                            ArrayList<ArchiveEntranceStructure> localList = new ArrayList<>();
                                            if (leaders != null) {
                                                for (JsonElement je : leaders) {
                                                    String organizationTitle = je.getAsJsonObject().get("organization")
                                                            .getAsJsonObject().get("title").getAsString();
                                                    int entranceYear = je.getAsJsonObject().get("year").getAsInt();
                                                    int entranceMonth = je.getAsJsonObject().get("month").getAsInt();
                                                    String lastPublishedStr = je.getAsJsonObject().get("last_published").getAsString();
                                                    String uniqueId = je.getAsJsonObject().get("unique_key").getAsString();

                                                    String extraStr = je.getAsJsonObject().get("extra_data").getAsString();
                                                    JsonElement extraData = null;
                                                    if (extraStr != null && !"".equals(extraStr)) {
                                                        try {
                                                            extraData = new JsonParser().parse(extraStr);
                                                        } catch (Exception exc) {
                                                            extraData = new JsonParser().parse("[]");
                                                        }
                                                    }

                                                    int duration = je.getAsJsonObject().get("duration").getAsInt();
                                                    int bookletCount = je.getAsJsonObject().get("booklets_count").getAsInt();
                                                    int entranceTypeId = je.getAsJsonObject().get("entrance_type").getAsJsonObject().get("id").getAsInt();
                                                    int buyCount = je.getAsJsonObject().get("stats").getAsJsonArray().get(0).getAsJsonObject().get("purchased").getAsInt();

                                                    int index = -1;
                                                    for (int j = 0; j < ArchiveDetailActivity.this.entranceSaleData.size(); j++) {
                                                        EntranceMultiSaleStruct es =  ArchiveDetailActivity.this.entranceSaleData.get(j);
                                                        if (entranceMonth == es.getEntranceMonth() && entranceYear == es.getEntranceYear() &&
                                                                entranceTypeId == es.getEntranceType()) {
                                                            index = j;
                                                            break;
                                                        }
                                                    }

                                                    if (index >= 0) {
                                                        int costBon = ArchiveDetailActivity.this.entranceSaleData.get(index).getCostBon();
                                                        Date lastPublished = null;
                                                        try {
                                                            lastPublished = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublishedStr);
                                                        } catch (ParseException e) {
                                                            lastPublished = new Date();
                                                        }

                                                        ArchiveEntranceStructure entrance = new ArchiveEntranceStructure(organizationTitle,
                                                                entranceYear, entranceMonth, extraData, buyCount, lastPublished, uniqueId,
                                                                bookletCount, duration, costBon);

                                                        localList.add(entrance);
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

                                                    ArchiveDetailActivity.this.adapter.setItems(new ArrayList<>());
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

                                            ArchiveDetailActivity.this.adapter.changeLoadingState(false);
                                            ArchiveDetailActivity.this.adapter.notifyDataSetChanged();
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

//                            AlertClass.hideLoadingMessage(loadingProgress);
                            ArchiveDetailActivity.this.adapter.changeLoadingState(false);
                            ArchiveDetailActivity.this.adapter.notifyDataSetChanged();

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
//            if (!isFinishing()) {
//                if (loadingProgress == null) {
//                    loadingProgress = AlertClass.showLoadingMessage(ArchiveDetailActivity.this);
//                    loadingProgress.show();
//                } else {
//                    if (!loadingProgress.isShowing()) {
//                        //loadingProgress = AlertClass.showLoadingMessage(HomeActivity.this);
//                        loadingProgress.show();
//                    }
//                }
//            }

            ArchiveDetailActivity.this.adapter.changeLoadingState(false);
            ArchiveDetailActivity.this.adapter.notifyDataSetChanged();
        }
    }

    private enum EntranceAdapterHolderType {
        ENTRANCE_SET(2), HEADER_HOLDER(1), LOADING_HOLDER(51);
        private final int value;

        EntranceAdapterHolderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private class GetEntranceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;
        private ArrayList<ArchiveEntranceStructure> mArrayList = new ArrayList<>();
        private boolean isLoaded = false;

        public GetEntranceAdapter(Context context, ArrayList<ArchiveEntranceStructure> m) {
            this.context = context;
            this.mArrayList = m;
        }


        public void setItems(ArrayList<ArchiveEntranceStructure> arrayList) {
            this.mArrayList = arrayList;
            this.isLoaded = true;
        }

        public ArrayList<ArchiveEntranceStructure> getItems() {
            return this.mArrayList;
        }

        public void changeLoadingState(boolean state) {
            this.isLoaded = state;
        }

        public void showBuyDialog(int position) {
            final ArchiveEntranceStructure struct = mArrayList.get(position - 1);

            UserDefaultsSingleton.WalletStruct ws = UserDefaultsSingleton.getInstance(getApplicationContext()).getWalletInfo();
            int cost = struct.getCostBon();

            boolean canBuy = true;
            if (cost > (ws != null ? ws.getCash() : 0)) {
                canBuy = false;
            }

            String subTitle = struct.getOrganization() + " | " + monthToString(struct.getMonth()) +
                    " " + FormatterSingleton.getInstance().getNumberFormatter().format(struct.getYear());

            EntranceBuyDialog dialog = new EntranceBuyDialog(ArchiveDetailActivity.this);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setListener(ArchiveDetailActivity.this);
            dialog.show();
            dialog.setupDialog("Entrance", struct.getUniqueId(),
                    canBuy, cost, (ws != null ? ws.getCash() : 0), subTitle);

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
                    code.setVisibility(View.GONE);
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
            private TextView yearText;
            private TextView monthText;
            private ImageView doubleCheck;
            private Button entranceBuyButton;

            private LinearLayout costContainer;
            private LinearLayout buyedTimeContainer;

            private JsonElement extraData;
            private ArchiveEntranceStructure entrance;

            public ItemsHolder(View itemView) {
                super(itemView);

                extraDataText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_extraDataText);
                countText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_countText);
                dateJalali = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_dateJalali);
                yearText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_yearText);
                monthText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_monthText);
                entranceBuyButton = (Button) itemView.findViewById(R.id.archiveDetailHolder2L_BuyButton);
                doubleCheck = (ImageView) itemView.findViewById(R.id.archiveDetailHolder2L_DoubleCheck);
                costContainer = (LinearLayout) itemView.findViewById(R.id.archiveDetailHolder2L_costContainer);
                buyedTimeContainer = (LinearLayout) itemView.findViewById(R.id.archiveDetailHolder2L_countTextLinear);

                extraDataText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                countText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                yearText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                monthText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceBuyButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(final ArchiveEntranceStructure struct, final boolean buyed, final Date buyedTime, final Integer position) {
                this.entrance = struct;

                doubleCheck.setVisibility(View.GONE);
                entranceBuyButton.setVisibility(View.GONE);

                yearText.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getYear()));
                if (entrance.getMonth() > 0) {
                    monthText.setText(monthToString(entrance.getMonth()));
                    monthText.setVisibility(View.VISIBLE);
                } else {
                    monthText.setText("");
                    monthText.setVisibility(View.GONE);
                }

                if (entrance.getCostBon() > 0) {
                    this.countText.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getCostBon()));
                    this.countText.setTextColor(ContextCompat.getColor(ArchiveDetailActivity.this, R.color.colorConcoughGray2));
                } else {
                    this.countText.setText("رایگان");
                    this.countText.setTextColor(ContextCompat.getColor(ArchiveDetailActivity.this, R.color.colorConcoughRed));
                }

                if (buyed) {
                    if (buyedTime != null) {
                        try {
                            String timeAgo = timeAgoSinceDate(entrance.getLastPublished(), "fa", false);
                            dateJalali.setText("خرید: " + timeAgo);
                        } catch (Exception e) {

                        }
                    }
                }

                extraDataText.setText(this.entrance.getOrganization());

                entranceBuyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        disableBuyButton();
//                        Integer statIndex = BasketSingleton.getInstance().findSaleByTargetId(uniqId, "Entrance");
//                        removeButtonEvent();
//                        if (BasketSingleton.getInstance().getBasketId() == null) {
//                            BasketSingleton.getInstance().createBasket(getApplicationContext(), position);
//                        } else {
//                            addSaleToBasket(position);
//                        }

                        if (UserDefaultsSingleton.getInstance(getApplicationContext()).hasWallet()) {
                            UserDefaultsSingleton.WalletStruct ws = UserDefaultsSingleton.getInstance(getApplicationContext()).getWalletInfo();
                            int cost = struct.getCostBon();

                            boolean canBuy = true;
                            if (cost > (ws != null ? ws.getCash() : 0)) {
                                canBuy = false;
                            }

                            String subTitle = struct.getOrganization() + " | " + monthToString(struct.getMonth()) +
                                    " " + FormatterSingleton.getInstance().getNumberFormatter().format(struct.getYear());

                            EntranceBuyDialog dialog = new EntranceBuyDialog(ArchiveDetailActivity.this);
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.setListener(ArchiveDetailActivity.this);
                            dialog.show();
                            dialog.setupDialog("Entrance", struct.getUniqueId(),
                                    canBuy, cost, (ws != null ? ws.getCash() : 0), subTitle);

                        } else {
                            ItemsHolder.this.disableBuyButton();
                            if (ArchiveDetailActivity.this.handler != null) {
                                Message msg = ArchiveDetailActivity.this.handler.obtainMessage(CREATE_WALLET);

                                Bundle bundle = new Bundle();
                                bundle.putInt("POSITION", position);
                                msg.setData(bundle);

                                ArchiveDetailActivity.this.handler.sendMessage(msg);
                            }
                        }
                    }
                });

//                Integer imageId = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.id;
//                downloadImage(imageId);

                this.changeButtonsState(buyed);
            }

            public void changeButtonsState(boolean buyed) {
                if (buyed) {
                    this.entranceBuyButton.setVisibility(View.GONE);
                    this.doubleCheck.setVisibility(View.VISIBLE);
                    this.costContainer.setVisibility(View.GONE);
                    this.buyedTimeContainer.setVisibility(View.VISIBLE);

                } else {
                    this.entranceBuyButton.setVisibility(View.VISIBLE);
                    this.doubleCheck.setVisibility(View.GONE);
                    this.costContainer.setVisibility(View.VISIBLE);
                    this.buyedTimeContainer.setVisibility(View.GONE);

                    this.changedBuyButtonState(false);
                }
            }

//            public void addSaleToBasket(int position) {
//
//                final String sorganizationTitle;
//                sorganizationTitle = jsonElement.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();
//
//                final String uniqId = jsonElement.getAsJsonObject().get("unique_key").getAsString();
//
//                final Integer bookletCount;
//                bookletCount = jsonElement.getAsJsonObject().get("booklets_count").getAsInt();
//
//                final Integer entranceYear;
//                entranceYear = jsonElement.getAsJsonObject().get("year").getAsInt();
//
//                final Integer entranceMonth;
//                entranceMonth = jsonElement.getAsJsonObject().get("month").getAsInt();
//
//                final Integer entranceDuration;
//                entranceDuration = jsonElement.getAsJsonObject().get("duration").getAsInt();
//
//                final String lastPublished;
//                Date lastPublishedDate = new Date();
//                lastPublished = jsonElement.getAsJsonObject().get("last_published").getAsString();
//                try {
//                    lastPublishedDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublished);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//                Integer id = BasketSingleton.getInstance().findSaleByTargetId(uniqId, "Entrance");
//                if (id != null && id > 0) {
//                    BasketSingleton.getInstance().removeSaleById(ArchiveDetailActivity.this, id, position);
//                } else {
//                    EntranceStruct myLocalEntrance = new EntranceStruct();
//                    myLocalEntrance.setEntranceBookletCounts(bookletCount);
//                    myLocalEntrance.setEntranceDuration(entranceDuration);
//                    myLocalEntrance.setEntranceExtraData(extraData);
//                    myLocalEntrance.setEntranceGroupTitle(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.groupTitle);
//                    myLocalEntrance.setEntranceLastPublished(lastPublishedDate);
//                    myLocalEntrance.setEntranceOrgTitle(sorganizationTitle);
//                    myLocalEntrance.setEntranceSetId(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.id);
//                    myLocalEntrance.setEntranceSetTitle(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.title);
//                    myLocalEntrance.setEntranceTypeTitle(ArchiveDetailActivity.this.mArchiveEsetDetailStruct.typeTitle);
//                    myLocalEntrance.setEntranceUniqueId(uniqId);
//                    myLocalEntrance.setEntranceYear(entranceYear);
//                    myLocalEntrance.setEntranceMonth(entranceMonth);
//
//                    BasketSingleton.getInstance().addSale(ArchiveDetailActivity.this, myLocalEntrance, "Entrance", position);
//                }
//            }

            public void disableBuyButton() {
                entranceBuyButton.setEnabled(false);
                entranceBuyButton.setText("●●●");
                entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_lightgray_style));
                entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughGray));
            }

            public void changedBuyButtonState(boolean state) {
                entranceBuyButton.setEnabled(true);
//                final Integer statIndex = BasketSingleton.getInstance().findSaleByTargetId(uniqueId, "Entrance");
//                if (statIndex != null && statIndex >= 0) {
//                    entranceBuyButton.setText("-  سبد خرید");
//                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_red_style));
//                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughRed));
//
//                } else {
//                    entranceBuyButton.setText("+  سبد خرید");
//                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_style));
//                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughBlue));
//                }

                if (!state) {
                    entranceBuyButton.setText("خرید آزمون");
                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_style));
                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughBlue));
                } else {
                    entranceBuyButton.setText("-  سبد خرید");
                    entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_red_style));
                    entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughRed));
                }
            }
        }

        private class LoadingHolder extends RecyclerView.ViewHolder {
            private ProgressBar progressBar;

            public LoadingHolder(View itemView) {
                super(itemView);

                progressBar = (ProgressBar)itemView.findViewById(R.id.loadingMoreProgressBar);
                progressBar.getIndeterminateDrawable().setColorFilter(
                        ContextCompat.getColor(ArchiveDetailActivity.this, R.color.colorConcoughGray),
                        PorterDuff.Mode.SRC_IN);
            }

            public void setupHolder() {
            }
        }


        @Override
        public int getItemCount() {
            if (this.isLoaded) {
                return mArrayList.size() + 1;
            } else {
                return 2;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == EntranceAdapterHolderType.HEADER_HOLDER.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_archivedetail_holder1, parent, false);
                return new TopItemHolder(view);
            } else if (viewType == EntranceAdapterHolderType.LOADING_HOLDER.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_recycle_loading, parent, false);
                return new LoadingHolder(view);
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
                if (this.isLoaded) {
                    final ArchiveEntranceStructure oneItem = mArrayList.get(position - 1);
                    ItemsHolder itemHolder = (ItemsHolder) holder;
                    itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = EntranceDetailActivity.newIntent(ArchiveDetailActivity.this, oneItem.getUniqueId(), "Archive");
                            startActivity(i);
                        }
                    });

                    boolean buyed = false;
                    Date buyedTime = null;

                    if (EntranceModelHandler.existById(ArchiveDetailActivity.this,
                            username, oneItem.getUniqueId())) {
                        buyed = true;

                        PurchasedModel pm = PurchasedModelHandler.getByProductId(ArchiveDetailActivity.this,
                                username,
                                "Entrance",
                                oneItem.getUniqueId());
                        if (pm != null) {
                            buyedTime = pm.created;
                        }
                    }

                    itemHolder.setupHolder(oneItem, buyed, buyedTime, position);
                } else {
                    LoadingHolder h = (LoadingHolder) holder;
                    h.setupHolder();
                }
            }


        }


        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return EntranceAdapterHolderType.HEADER_HOLDER.getValue();
            } else {
                if (this.isLoaded) {
                    return EntranceAdapterHolderType.ENTRANCE_SET.getValue();
                } else {
                    return EntranceAdapterHolderType.LOADING_HOLDER.getValue();
                }
            }
        }

    }

    @Override
    public void productBuyResult(@NotNull JsonObject data, @NotNull String productId, @NotNull String productType) {
        try {
            int cash = data.get("wallet_cash").getAsInt();
            String updated = data.get("wallet_updated").getAsString();

            UserDefaultsSingleton.getInstance(this.getApplicationContext()).setWalletInfo(cash, updated);

            int index = -1;
            ArrayList<ArchiveEntranceStructure> array = this.adapter.getItems();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).getUniqueId() == productId) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                ArchiveEntranceStructure item = array.get(index);

                EntranceStruct entranceStruct = new EntranceStruct();
                entranceStruct.setEntranceTypeTitle(this.mArchiveEsetDetailStruct.typeTitle);
                entranceStruct.setEntranceOrgTitle(item.getOrganization());
                entranceStruct.setEntranceGroupTitle(this.mArchiveEsetDetailStruct.groupTitle);
                entranceStruct.setEntranceSetTitle(this.mArchiveEsetDetailStruct.esetStruct.title);
                entranceStruct.setEntranceSetId(this.mArchiveEsetDetailStruct.esetStruct.id);
                entranceStruct.setEntranceExtraData(item.getExtraData());
                entranceStruct.setEntranceBookletCounts(item.getBookletCount());
                entranceStruct.setEntranceYear(item.getYear());
                entranceStruct.setEntranceMonth(item.getMonth());
                entranceStruct.setEntranceDuration(item.getEntranceDuration());
                entranceStruct.setEntranceUniqueId(item.getUniqueId());
                entranceStruct.setEntranceLastPublished(item.getLastPublished());

                ArrayList<Integer> purchasedTemp = new ArrayList<>();
                JsonArray purchased = null;
                if (data.has("purchased")) {
                    if (data.get("purchased").isJsonArray()) {
                        purchased = data.get("purchased").getAsJsonArray();
                    }
                }

                if (purchased != null) {
                    for (JsonElement element : purchased) {
                        int purchaseId = element.getAsJsonObject().get("purchase_id").getAsInt();
                        int downloaded = element.getAsJsonObject().get("downloaded").getAsInt();

                        String purchasedTimeStr = element.getAsJsonObject().get("purchase_time").getAsString();
                        Date purchasedTime = new Date();
                        try {
                            purchasedTime = FormatterSingleton.getInstance().getUTCDateFormatter().parse(purchasedTimeStr);
                        } catch (Exception exc) {}

                        if (EntranceModelHandler.add(this.getApplicationContext(), this.username, entranceStruct)) {
                            if (PurchasedModelHandler.add(this.getApplicationContext(), purchaseId,
                                    this.username, false, downloaded, false,
                                    productType, entranceStruct.getEntranceUniqueId(), purchasedTime)) {

                                purchasedTemp.add(purchaseId);
                            } else {
                                EntranceModelHandler.removeById(this, username, entranceStruct.getEntranceUniqueId());
                            }
                        }
                    }
                }

                this.adapter.notifyDataSetChanged();
                this.downloadImages(purchasedTemp);

                AlertClass.showAlertMessage(this, "ActionResult",
                        "PurchasedSuccess",
                        "success", null);

                this.setMenuItemColor(1, R.color.colorConcoughRedLight);
            }
        } catch (Exception exc) {
        }
    }

    private void downloadImages(ArrayList<Integer> ids) {
        RealmResults<PurchasedModel> purchased = PurchasedModelHandler.getAllPurchasedIn(this.getApplicationContext(),
                this.username, (Integer[]) ids.toArray());
        if (purchased != null) {
            for (PurchasedModel pm : purchased) {
                if (pm.productType == "Entrance") {
                    EntranceModel em = EntranceModelHandler.getByUsernameAndId(this.getApplicationContext(),
                            this.username, pm.productUniqueId);
                    if (em != null) {
                        this.downloadEsetImage(em.setId);
                    }
                }
            }
        }
    }

    private void downloadEsetImage(final int imageId) {
        final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);

        if (url != null) {
            byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
            if (data != null) {
                saveToFile(data, imageId);
            } else {
                MediaRestAPIClass.downloadEsetImage(this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
                    @Override
                    public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (httpErrorType != HTTPErrorType.Success) {
                                    if (httpErrorType == HTTPErrorType.Refresh) {
                                        downloadEsetImage(imageId);
                                    }
                                } else {
                                    MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);
                                    saveToFile(data, imageId);
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

            }
        }
    }

    private void saveToFile(byte[] data, int imageId) {
        File folder = new File(getApplicationContext().getFilesDir(), "images");
        File folder2 = new File(getApplicationContext().getFilesDir() + "/images", "eset");
        if (!folder.exists()) {
            folder.mkdir();
            folder2.mkdir();
        }

        File photo = new File(getApplicationContext().getFilesDir() + "/images/eset", String.valueOf(imageId));
        if (photo.exists()) {
            photo.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());

            fos.write(data);
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }

}
