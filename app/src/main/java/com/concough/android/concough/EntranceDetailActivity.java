package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.concough.android.downloader.EntrancePackageDownloader;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.PurchasedModel;
import com.concough.android.models.PurchasedModelHandler;
import com.concough.android.models.UserLogModelHandler;
import com.concough.android.rest.EntranceRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.rest.ProductRestAPIClass;
import com.concough.android.rest.PurchasedRestAPIClass;
import com.concough.android.singletons.BasketSingleton;
import com.concough.android.singletons.DownloaderSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.EntrancePurchasedStruct;
import com.concough.android.structures.EntranceSaleStruct;
import com.concough.android.structures.EntranceStatStruct;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.EntranceVCStateEnum;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.LogTypeEnum;
import com.concough.android.structures.NetworkErrorType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class EntranceDetailActivity extends BottomNavigationActivity implements Handler.Callback {

    private HandlerThread handlerThread = null;
    private Handler handler = null;
    private Handler uiHandler = null;

    private static final String TAG = "EntranceDetailActivity";
    private static final String CONTEXT_WHO_KEY = "CONTEXT_WHO";
    private static final String ENTRANCE_UNIQUE_ID_KEY = "ENTRANCE_UNIQUE_ID";
    private static final String HANDLE_THREAD_NAME = "Concough-EntranceDetailActivity";

    private static final int DOWNLOAD_ENTRANCE = 0;
    private static final int DOWNLOAD_USER_PURCHASE_DATA = 1;
    private static final int DOWNLOAD_ENTRANCE_STAT = 2;
    private static final int DOWNLOAD_ENTRANCE_SALE = 3;
    private static final int UPDATE_USER_PURCHASE_DATE = 4;

    private String entranceUniqueId = "fc4419fc2ceb4aaca787a25ff6c00117";
    private String contextFromWho = "";
    private Boolean selfBasketAdd = false;
    private EntranceVCStateEnum state = null;

    private EntranceStruct entrance = null;
    private EntranceStatStruct entranceStat = null;
    private EntranceSaleStruct entranceSale = null;
    private EntrancePurchasedStruct entrancePurchase = null;

    private PullRefreshLayout pullRefreshLayout = null;
    private RecyclerView recycleView;
    private EntranceDetailAdapter entranceDetailAdapter;

    public static Intent newIntent(Context packageContext, String entranceUniqueId, String who) {
        Intent i = new Intent(packageContext, EntranceDetailActivity.class);
        i.putExtra(ENTRANCE_UNIQUE_ID_KEY, entranceUniqueId);
        i.putExtra(CONTEXT_WHO_KEY, who);
        return i;
    }

    protected int getLayoutResourceId() {
        return R.layout.activity_entrance_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.contextFromWho = getIntent().getStringExtra(CONTEXT_WHO_KEY);
        this.entranceUniqueId = getIntent().getStringExtra(ENTRANCE_UNIQUE_ID_KEY);


        if ("Home".equals(this.contextFromWho)) {
            this.setMenuSelectedIndex(0);
        } else if ("Archive".equals(this.contextFromWho)) {
            this.setMenuSelectedIndex(1);
        }
//        this.setMenuSelectedIndex(1);
        super.onCreate(savedInstanceState);


        this.handlerThread = new HandlerThread(HANDLE_THREAD_NAME);
        if (this.handlerThread != null) {
            this.handlerThread.start();
        }

        Looper looper = this.handlerThread.getLooper();
        if (looper != null) {
            this.handler = new Handler(looper, this);
        }

        this.uiHandler = new Handler();

        recycleView = (RecyclerView) findViewById(R.id.entranceDetaiA_recycle);
        entranceDetailAdapter = new EntranceDetailAdapter(this);

        recycleView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupView();
        BasketSingleton.getInstance().loadBasketItems(this);

    }

    private void setupView() {
        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.entranceDetailA_swipeRefreshLayout);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                EntranceDetailActivity.this.resetView();
                EntranceDetailActivity.this.stateMachine();

            }
        });

        BasketSingleton.getInstance().setListener(new BasketSingleton.BasketSingletonListener() {
            @Override
            public void onLoadItemCompleted(int count) {
                EntranceDetailActivity.this.recycleView.setAdapter(entranceDetailAdapter);
                EntranceDetailActivity.this.resetView();
                EntranceDetailActivity.this.stateMachine();
            }

            @Override
            public void onCreateCompleted() {
                Integer id = BasketSingleton.getInstance().findSaleByTargetId(EntranceDetailActivity.this.entranceUniqueId, "Entrance");
                if (id != null && id > 0) {
                    BasketSingleton.getInstance().removeSaleById(EntranceDetailActivity.this, id, 0);
                } else {
                    BasketSingleton.getInstance().addSale(EntranceDetailActivity.this, EntranceDetailActivity.this.entrance, "Entrance");
                }
            }

            @Override
            public void onAddCompleted(int count) {
                EntranceDetailActivity.this.selfBasketAdd = !EntranceDetailActivity.this.selfBasketAdd;
                EntranceDetailActivity.this.updateBasketBadge(count);

                EntranceDetailActivity.this.entranceDetailAdapter.notifyDataSetChanged();
                EntranceDetailActivity.this.recycleView.smoothScrollToPosition(EntranceDetailActivity.this.entranceDetailAdapter.getItemCount());
            }

            @Override
            public void onRemoveCompleted(int count, int position) {
                EntranceDetailActivity.this.selfBasketAdd = !EntranceDetailActivity.this.selfBasketAdd;
                EntranceDetailActivity.this.updateBasketBadge(count);

                EntranceDetailActivity.this.entranceDetailAdapter.notifyDataSetChanged();
                EntranceDetailActivity.this.recycleView.smoothScrollToPosition(EntranceDetailActivity.this.entranceDetailAdapter.getItemCount());
            }

            @Override
            public void onCheckout(int count, HashMap<Integer, BasketSingleton.PurchasedItem> purchased) {

            }
        });

    }

    public void stopHandler() {
        if (this.handlerThread != null)
            this.handlerThread.quit();
    }

    @Override
    protected void onDestroy() {
        stopHandler();
        super.onDestroy();
    }

    private void resetView() {
        this.setupBarButton();

        this.state = EntranceVCStateEnum.Initialize;
        this.selfBasketAdd = false;

        this.entrance = null;
        this.entranceSale = null;
        this.entranceStat = null;
        this.entrancePurchase = null;

    }

    private void setupBarButton() {
        if (BasketSingleton.getInstance().getSalesCount() > 0) {
            // TODO: Setup basket badge
        }
    }

    private void updateBasketBadge(int count) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case DOWNLOAD_ENTRANCE:
                    EntranceDetailActivity.this.handleDownloadEntrance(msg);
                    break;
                case DOWNLOAD_USER_PURCHASE_DATA:
                    EntranceDetailActivity.this.handleDownloadUserPurchaseData(msg);
                    break;
                case DOWNLOAD_ENTRANCE_STAT:
                    EntranceDetailActivity.this.handleDownloadEntranceStat(msg);
                    break;
                case DOWNLOAD_ENTRANCE_SALE:
                    EntranceDetailActivity.this.handleDownloadEntranceSale(msg);
                    break;
                case UPDATE_USER_PURCHASE_DATE:
                    EntranceDetailActivity.this.handleUpdateUserPurchaseData(msg);
                    break;
            }
        }
        return true;
    }

    private void stateMachine() {
        String username;
        if (this.state != null) {
            switch (this.state) {
                case Initialize:
                    username = UserDefaultsSingleton.getInstance(getApplicationContext())
                            .getUsername(getApplicationContext());

                    if (username != null && EntranceModelHandler.existById(getApplicationContext(), EntranceDetailActivity.this.entranceUniqueId, username)) {
                        pullRefreshLayout.setRefreshing(false);
                        this.localEntrance();
                    } else {
                        Integer index = BasketSingleton.getInstance().findSaleByTargetId(this.entranceUniqueId, "Entrance");
                        if (index != null) {
                            pullRefreshLayout.setRefreshing(false);

                            this.entrance = (EntranceStruct) BasketSingleton.getInstance().getSaleById(index);

                            EntranceDetailActivity.this.state = EntranceVCStateEnum.EntranceComplete;
                            EntranceDetailActivity.this.selfBasketAdd = true;
                            EntranceDetailActivity.this.stateMachine();
                            return;
                        } else {
                            if (this.handler != null) {
                                Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_ENTRANCE);
                                msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                EntranceDetailActivity.this.handler.sendMessage(msg);
                            }
                        }
                    }
                    break;
                case EntranceComplete:
                    username = UserDefaultsSingleton.getInstance(getApplicationContext())
                            .getUsername(getApplicationContext());

                    if (username != null) {
                        PurchasedModel purchased = PurchasedModelHandler.getByProductId(getApplicationContext(), username, "Entrance", EntranceDetailActivity.this.entranceUniqueId);
                        if (purchased != null) {
                            EntrancePurchasedStruct ep = new EntrancePurchasedStruct();
                            ep.id = purchased.id;
                            ep.created = purchased.created;
                            ep.amount = 0;
                            ep.downloaded = purchased.downloadTimes;
                            ep.isDownloaded = purchased.isDownloaded;
                            ep.isDataDownloaded = purchased.isLocalDBCreated;
                            ep.isImagesDownloaded = purchased.isImageDownloaded;

                            EntranceDetailActivity.this.entrancePurchase = ep;
                            EntranceDetailActivity.this.state = EntranceVCStateEnum.Purchased;
                            EntranceDetailActivity.this.stateMachine();
                            return;

                        } else {
                            if (this.handler != null) {
                                Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_USER_PURCHASE_DATA);
                                msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                EntranceDetailActivity.this.handler.sendMessage(msg);
                            }
                        }
                    }
                    break;
                case NotPurchased:
                    if (this.handler != null) {
                        Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_ENTRANCE_STAT);
                        msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                        EntranceDetailActivity.this.handler.sendMessage(msg);
                    }
                    break;
                case Purchased:
                    EntranceDetailActivity.this.entranceDetailAdapter.notifyDataSetChanged();

                    username = UserDefaultsSingleton.getInstance(getApplicationContext())
                            .getUsername(getApplicationContext());

                    if (username != null) {
                        PurchasedModel localPurchased = PurchasedModelHandler.getByProductId(EntranceDetailActivity.this, username, "Entrance", EntranceDetailActivity.this.entranceUniqueId);
                        if (localPurchased != null) {
                            if (localPurchased.isDownloaded) {
                                EntranceDetailActivity.this.state = EntranceVCStateEnum.Downloaded;
                                EntranceDetailActivity.this.stateMachine();
                                return;
                            } else {
                                DownloaderSingleton.DownloaderState st = DownloaderSingleton.getInstance().getDownloaderState(entranceUniqueId);
                                if (st != null) {
                                    if (st == DownloaderSingleton.DownloaderState.Started) {
                                        DownloaderSingleton.getInstance().setListener(new DownloaderSingleton.DownloaderSingletonListener() {
                                            @Override
                                            public void onDownloadergetReady(Object downloader, int index) {
//                                                ((EntrancePackageDownloader) downloader).registerActivity(EntranceDetailActivity.this, "ED", 0);
//                                                uiHandler.post(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        EntranceDetailActivity.this.state = EntranceVCStateEnum.DownloadStarted;
//                                                        EntranceDetailActivity.this.stateMachine();
//                                                        return;
//                                                    }
//                                                });
                                            }
                                        });

                                        try {
                                            DownloaderSingleton.getInstance().getMeDownloader(EntranceDetailActivity.this, "Entrance", entranceUniqueId, 0, new Function2<Object, Integer, Unit>() {
                                                @Override
                                                public Unit invoke(Object o, Integer integer) {
                                                    ((EntrancePackageDownloader) o).registerActivity(EntranceDetailActivity.this, "ED", 0);
                                                    uiHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            EntranceDetailActivity.this.state = EntranceVCStateEnum.DownloadStarted;
                                                            EntranceDetailActivity.this.stateMachine();
                                                        }
                                                    });

                                                    return null;
                                                }
                                            });
                                        } catch (Exception exc) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;

                case ShowSaleInfo:
                    EntranceDetailActivity.this.entranceDetailAdapter.notifyDataSetChanged();
                    break;

                case DownloadStarted:
                    try {
                        EntranceDetailAdapter.EDPurchasedSectionViewHolder holder = (EntranceDetailAdapter.EDPurchasedSectionViewHolder) EntranceDetailActivity.this.recycleView.findViewHolderForAdapterPosition(3);
                        if (holder != null) {
                            holder.changeToDownloadStartedState();
                        }
                    } catch (Exception exc) {

                    }
                    break;

                case Downloaded:
                    EntranceDetailActivity.this.entranceDetailAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    private void createLog(String logType, JsonObject extraData) {
        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername(getApplicationContext());
        if (username != null) {
            String uniqueId = UUID.randomUUID().toString();
            Date created = new Date();

            try {
                UserLogModelHandler.add(getApplicationContext(), username, uniqueId, created, logType, extraData);
            } catch (Exception exc) {
            }
        }
    }

    private void localEntrance() {
        String username = UserDefaultsSingleton.getInstance(getApplicationContext())
                .getUsername(getApplicationContext());

        if (username != null && EntranceModelHandler.existById(getApplicationContext(), EntranceDetailActivity.this.entranceUniqueId, username)) {
            EntranceModel localEntrance = EntranceModelHandler.getByUsernameAndId(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, username);
            if (localEntrance != null) {
                String s = localEntrance.extraData;
                JsonElement extraData = new JsonParser().parse(s);

                EntranceStruct myLocalEntrance = new EntranceStruct();
                myLocalEntrance.setEntranceBookletCounts(localEntrance.bookletsCount);
                myLocalEntrance.setEntranceDuration(localEntrance.duration);
                myLocalEntrance.setEntranceExtraData(extraData);
                myLocalEntrance.setEntranceGroupTitle(localEntrance.group);
                myLocalEntrance.setEntranceLastPublished(localEntrance.lastPublished);
                myLocalEntrance.setEntranceOrgTitle(localEntrance.organization);
                myLocalEntrance.setEntranceSetId(localEntrance.setId);
                myLocalEntrance.setEntranceSetTitle(localEntrance.set);
                myLocalEntrance.setEntranceTypeTitle(localEntrance.type);
                myLocalEntrance.setEntranceUniqueId(localEntrance.uniqueId);
                myLocalEntrance.setEntranceYear(localEntrance.year);

                EntranceDetailActivity.this.entrance = myLocalEntrance;
                EntranceDetailActivity.this.state = EntranceVCStateEnum.EntranceComplete;
                EntranceDetailActivity.this.entranceDetailAdapter.notifyDataSetChanged();

                EntranceDetailActivity.this.stateMachine();
            }
        }
    }

    private void refreshUserPurchaseData() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: Show loading dialog
            }
        });

        PurchasedRestAPIClass.getEntrancePurchasedData(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, new Function2<JsonElement, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);

                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (EntranceDetailActivity.this.handler != null) {
                                    Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_USER_PURCHASE_DATA);
                                    msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                    EntranceDetailActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                // Show top message with "HTTPError" and type = error
                            }
                        } else {
                            if (jsonElement != null) {
                                String status = jsonElement.getAsJsonObject().get("status").getAsString();
                                switch (status) {
                                    case "OK":
                                        try {
                                            JsonObject purchaseData = jsonElement.getAsJsonObject().get("purchase").getAsJsonObject();
                                            Boolean purchaseStatus = purchaseData.get("status").getAsBoolean();
                                            if (!purchaseStatus) {
                                                EntranceDetailActivity.this.state = EntranceVCStateEnum.NotPurchased;
                                            } else {
                                                JsonObject purchaseRecord = purchaseData.get("purchase_record").getAsJsonObject();
                                                if (purchaseRecord != null) {
                                                    int id = purchaseRecord.get("id").getAsInt();
                                                    int amount = purchaseRecord.get("payed_amount").getAsInt();
                                                    int downloaded = purchaseRecord.get("downloaded").getAsInt();

                                                    String username = UserDefaultsSingleton.getInstance(getApplicationContext())
                                                            .getUsername(getApplicationContext());

                                                    EntranceDetailActivity.this.entrancePurchase.downloaded = downloaded;

                                                    if (username != null)
                                                        PurchasedModelHandler.updateDownloadTimes(getApplicationContext(), username, id, downloaded);

                                                    EntranceDetailAdapter.EDPurchasedSectionViewHolder holder = (EntranceDetailAdapter.EDPurchasedSectionViewHolder)
                                                            EntranceDetailActivity.this.recycleView.findViewHolderForAdapterPosition(3);
                                                    if (holder != null) {
                                                        holder.updateDownloadedLabel(downloaded);
                                                        holder.showLoading(false);
                                                    }

                                                }
                                            }
                                        } catch (Exception exc) {
                                        }
                                        break;
                                    case "Error":
                                        String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EntranceNotExist":
                                            case "EmptyArray":
                                                // TODO: Show alert message EntranceResult with subType = "EntranceNotExist"
                                                break;
                                            default:
                                                // TODO: Show alert message ErrorResult
                                                break;
                                        }
                                        break;
                                }
                            }
                        }
                    }
                });
                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);
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

    }

    private void handleDownloadEntranceStat(@Nullable Message msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: Show loading dialog
            }
        });

        ProductRestAPIClass.getEntranceStatData(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, new Function2<JsonElement, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog

                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);

                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (EntranceDetailActivity.this.handler != null) {
                                    Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_ENTRANCE_STAT);
                                    msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                    EntranceDetailActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                // Show top message with "HTTPError" and type = error
                            }
                        } else {
                            if (jsonElement != null) {
                                String status = jsonElement.getAsJsonObject().get("status").getAsString();
                                switch (status) {
                                    case "OK":
                                        try {
                                            JsonObject statData = jsonElement.getAsJsonObject().get("stat_data").getAsJsonObject();
                                            if (statData != null) {
                                                int purchased = statData.get("purchased").getAsInt();
                                                String updatedStr = statData.get("updated").getAsString();
                                                Date updated = FormatterSingleton.getInstance().getUTCDateFormatter().parse(updatedStr);

                                                EntranceStatStruct stat = new EntranceStatStruct();
                                                stat.purchased = purchased;
                                                stat.updated = updated;

                                                EntranceDetailActivity.this.entranceStat = stat;
                                            }
                                        } catch (Exception exc) {
                                        }
                                        break;
                                    case "Error":
                                        String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EntranceNotExist":
                                            case "EmptyArray":
                                                // TODO: Show alert message EntranceResult with subType = "EntranceNotExist"
                                                break;
                                            default:
                                                // TODO: Show alert message ErrorResult
                                                break;
                                        }
                                        break;
                                }
                            }
                        }

                        // download sale data
                        if (EntranceDetailActivity.this.handler != null) {
                            Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_ENTRANCE_SALE);
                            msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                            EntranceDetailActivity.this.handler.sendMessage(msg);
                        }

                    }
                });
                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);
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
    }

    private void handleDownloadEntranceSale(@Nullable Message msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: Show loading dialog
            }
        });

        ProductRestAPIClass.getEntranceSaleData(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, new Function2<JsonElement, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog

                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);

                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (EntranceDetailActivity.this.handler != null) {
                                    Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_ENTRANCE_SALE);
                                    msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                    EntranceDetailActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                // Show top message with "HTTPError" and type = error
                            }
                        } else {
                            if (jsonElement != null) {
                                String status = jsonElement.getAsJsonObject().get("status").getAsString();
                                switch (status) {
                                    case "OK":
                                        try {
                                            JsonObject saleData = jsonElement.getAsJsonObject().get("sale_data").getAsJsonObject();
                                            if (saleData != null) {
                                                int discount = saleData.get("discount").getAsInt();
                                                int cost = saleData.get("sale_record").getAsJsonObject().get("cost").getAsInt();

                                                EntranceSaleStruct es = new EntranceSaleStruct();
                                                es.cost = cost;
                                                es.discount = discount;
                                                EntranceDetailActivity.this.entranceSale = es;

                                                EntranceDetailActivity.this.state = EntranceVCStateEnum.ShowSaleInfo;
                                                EntranceDetailActivity.this.stateMachine();
                                                return;

                                            }
                                        } catch (Exception exc) {
                                        }
                                        break;
                                    case "Error":
                                        String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EntranceNotExist":
                                            case "EmptyArray":
                                                // TODO: Show alert message EntranceResult with subType = "EntranceNotExist"
                                                break;
                                            default:
                                                // TODO: Show alert message ErrorResult
                                                break;
                                        }
                                        break;
                                }
                            }
                        }

//                        // download sale data
//                        if (EntranceDetailActivity.this.handler != null) {
//                            Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_ENTRANCE_STAT);
//                            msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));
//
//                            EntranceDetailActivity.this.handler.sendMessage(msg);
//                        }

                    }
                });
                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);
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
    }

    private void handleDownloadUserPurchaseData(@Nullable Message msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: Show loading dialog
            }
        });

        PurchasedRestAPIClass.getEntrancePurchasedData(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, new Function2<JsonElement, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);

                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (EntranceDetailActivity.this.handler != null) {
                                    Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_USER_PURCHASE_DATA);
                                    msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                    EntranceDetailActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                // Show top message with "HTTPError" and type = error
                            }
                        } else {
                            if (jsonElement != null) {
                                String status = jsonElement.getAsJsonObject().get("status").getAsString();
                                switch (status) {
                                    case "OK":
                                        try {
                                            JsonObject purchaseData = jsonElement.getAsJsonObject().get("purchase").getAsJsonObject();
                                            Boolean purchaseStatus = purchaseData.get("status").getAsBoolean();
                                            if (!purchaseStatus) {
                                                EntranceDetailActivity.this.state = EntranceVCStateEnum.NotPurchased;
                                            } else {
                                                JsonObject purchaseRecord = purchaseData.get("purchase_record").getAsJsonObject();
                                                if (purchaseRecord != null) {
                                                    int id = purchaseRecord.get("id").getAsInt();
                                                    int amount = purchaseRecord.get("payed_amount").getAsInt();
                                                    int downloaded = purchaseRecord.get("downloaded").getAsInt();

                                                    String createdStr = purchaseRecord.get("created").getAsString();
                                                    Date created = FormatterSingleton.getInstance().getUTCDateFormatter().parse(createdStr);

                                                    EntrancePurchasedStruct ep = new EntrancePurchasedStruct();
                                                    ep.id = id;
                                                    ep.created = created;
                                                    ep.amount = amount;
                                                    ep.downloaded = downloaded;
                                                    ep.isDownloaded = false;
                                                    ep.isDataDownloaded = false;
                                                    ep.isImagesDownloaded = false;

                                                    EntranceDetailActivity.this.entrancePurchase = ep;

                                                    String username = UserDefaultsSingleton.getInstance(getApplicationContext())
                                                            .getUsername(getApplicationContext());

                                                    if (EntranceModelHandler.add(getApplicationContext(), username, EntranceDetailActivity.this.entrance)) {
                                                        if (!PurchasedModelHandler.add(getApplicationContext(), id, username, false, downloaded, false, "Entrance", EntranceDetailActivity.this.entranceUniqueId, created)) {
                                                            EntranceModelHandler.removeById(getApplicationContext(), username, EntranceDetailActivity.this.entranceUniqueId);
                                                        }
                                                    }

                                                }
                                                EntranceDetailActivity.this.state = EntranceVCStateEnum.Purchased;

                                            }

                                            EntranceDetailActivity.this.stateMachine();
                                            return;

                                        } catch (Exception exc) {
                                        }
                                        break;
                                    case "Error":
                                        String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EntranceNotExist":
                                            case "EmptyArray":
                                                // TODO: Show alert message EntranceResult with subType = "EntranceNotExist"
                                                break;
                                            default:
                                                // TODO: Show alert message ErrorResult
                                                break;
                                        }
                                        break;
                                }
                            }
                        }
                    }
                });
                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);
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
    }

    private void handleDownloadEntrance(@Nullable Message msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: Show loading dialog

            }
        });

        EntranceRestAPIClass.getEntranceWithBuyInfo(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, new Function2<JsonElement, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);

                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (EntranceDetailActivity.this.handler != null) {
                                    Message msg = EntranceDetailActivity.this.handler.obtainMessage(DOWNLOAD_ENTRANCE);
                                    msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                    EntranceDetailActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                // Show top message with "HTTPError" and type = error
                            }
                        } else {
                            if (jsonElement != null) {
                                String status = jsonElement.getAsJsonObject().get("status").getAsString();
                                switch (status) {
                                    case "OK":
                                        try {

                                            JsonObject record = jsonElement.getAsJsonObject().get("records").getAsJsonObject();

                                            if (record != null) {
                                                String organization = record.get("organization").getAsJsonObject().get("title").getAsString();
                                                String entrance_type = record.get("entrance_type").getAsJsonObject().get("title").getAsString();
                                                String entrance_set = record.get("entrance_set").getAsJsonObject().get("title").getAsString();
                                                int entrance_set_id = record.get("entrance_set").getAsJsonObject().get("id").getAsInt();
                                                String entrance_group = record.get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString();
                                                JsonElement extraData = new JsonParser().parse(record.get("extra_data").getAsString());
                                                int bookletCount = record.get("booklets_count").getAsInt();
                                                int duration = record.get("duration").getAsInt();
                                                int year = record.get("year").getAsInt();
                                                String lastPublishedStr = record.get("last_published").getAsString();
                                                Date lastPublished = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublishedStr);

                                                EntranceStruct myLocalEntrance = new EntranceStruct();
                                                myLocalEntrance.setEntranceBookletCounts(bookletCount);
                                                myLocalEntrance.setEntranceDuration(duration);
                                                myLocalEntrance.setEntranceExtraData(extraData);
                                                myLocalEntrance.setEntranceGroupTitle(entrance_group);
                                                myLocalEntrance.setEntranceLastPublished(lastPublished);
                                                myLocalEntrance.setEntranceOrgTitle(organization);
                                                myLocalEntrance.setEntranceSetId(entrance_set_id);
                                                myLocalEntrance.setEntranceSetTitle(entrance_set);
                                                myLocalEntrance.setEntranceTypeTitle(entrance_type);
                                                myLocalEntrance.setEntranceUniqueId(entranceUniqueId);
                                                myLocalEntrance.setEntranceYear(year);

                                                EntranceDetailActivity.this.entrance = myLocalEntrance;
                                                EntranceDetailActivity.this.state = EntranceVCStateEnum.EntranceComplete;

                                                EntranceDetailActivity.this.entranceDetailAdapter.notifyDataSetChanged();
                                                EntranceDetailActivity.this.stateMachine();
                                                return;
                                            }
                                        } catch (Exception exc) {
                                        }
                                        break;
                                    case "Error":
                                        String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EntranceNotExist":
                                            case "EmptyArray":
                                                // TODO: Show alert message EntranceResult with subType = "EntranceNotExist"
                                                break;
                                            default:
                                                // TODO: Show alert message ErrorResult
                                                break;
                                        }
                                        break;
                                }
                            }
                        }
                    }
                });

                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType) {
                uiHandler.post(new Runnable() {
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
    }

    private void handleUpdateUserPurchaseData(@Nullable Message msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: Show loading dialog

            }
        });

        PurchasedRestAPIClass.putEntrancePurchasedDownload(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, new Function2<JsonElement, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO: hide loading dialog
                        EntranceDetailActivity.this.pullRefreshLayout.setRefreshing(false);

                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (EntranceDetailActivity.this.handler != null) {
                                    Message msg = EntranceDetailActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATE);
                                    msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                    EntranceDetailActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                // Show top message with "HTTPError" and type = error
                            }
                        } else {
                            if (jsonElement != null) {
                                String status = jsonElement.getAsJsonObject().get("status").getAsString();
                                switch (status) {
                                    case "OK":
                                        try {
                                            JsonElement purchase = jsonElement.getAsJsonObject().get("purchase");
                                            if (purchase != null) {
                                                if (purchase.getAsJsonObject().has("purchase_record")) {
                                                    JsonObject purchase_record = purchase.getAsJsonObject().get("purchase_record").getAsJsonObject();
                                                    int id = purchase_record.get("id").getAsInt();
                                                    int downloaded = purchase_record.get("downloaded").getAsInt();

                                                    EntranceDetailActivity.this.entrancePurchase.downloaded = downloaded;

                                                    String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername(getApplicationContext());
                                                    if (username != null) {
                                                        PurchasedModelHandler.updateDownloadTimes(getApplicationContext(), username, id, downloaded);

                                                        EntranceDetailAdapter.EDPurchasedSectionViewHolder holder = (EntranceDetailAdapter.EDPurchasedSectionViewHolder) EntranceDetailActivity.this.recycleView.findViewHolderForAdapterPosition(3);
                                                        holder.updateDownloadedLabel(downloaded);
                                                        holder.showLoading(false);
                                                    }
                                                }
                                            }

                                        } catch (Exception exc) {
                                        }
                                        break;
                                    case "Error":
                                        String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EntranceNotExist":
                                            case "EmptyArray":
                                                // TODO: Show alert message EntranceResult with subType = "EntranceNotExist"
                                                break;
                                            default:
                                                // TODO: Show alert message ErrorResult
                                                break;
                                        }
                                        break;
                                }
                            }
                        }
                    }
                });

                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType) {
                uiHandler.post(new Runnable() {
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
    }

    private enum EDViewHolderType {
        INITIAL_SECTION(1),
        HEADER_SECTION(2),
        INFORMATION_SECTION(3),
        SALE_SECTION(4),
        PURCHASED_SECTION(5),
        DOWNLOADED_SECTION(6);

        private final int value;

        private EDViewHolderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    private class EntranceDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;

        public EntranceDetailAdapter(Context context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (EDViewHolderType.INITIAL_SECTION.getValue() == viewType) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_ed_initial_section, parent, false);
                return new EDInitialSectionViewHolder(view);
            } else if (EDViewHolderType.HEADER_SECTION.getValue() == viewType) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_ed_header_section, parent, false);
                return new EDHeaderSectionViewHolder(view);
            } else if (EDViewHolderType.INFORMATION_SECTION.getValue() == viewType) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_ed_information_section, parent, false);
                return new EDInformationSectionViewHolder(view);
            } else if (EDViewHolderType.SALE_SECTION.getValue() == viewType) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_ed_sale_section, parent, false);
                return new EDSaleSectionViewHolder(view);
            } else if (EDViewHolderType.PURCHASED_SECTION.getValue() == viewType) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_ed_purchased_section, parent, false);
                return new EDPurchasedSectionViewHolder(view);
            } else if (EDViewHolderType.DOWNLOADED_SECTION.getValue() == viewType) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_ed_downloaded_section, parent, false);
                return new EDDownloadedSectionViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getClass() == EDInitialSectionViewHolder.class) {
                ((EDInitialSectionViewHolder) holder).setupHolder(EntranceDetailActivity.this.entrance);
            } else if (holder.getClass() == EDHeaderSectionViewHolder.class) {
                ((EDHeaderSectionViewHolder) holder).setupHolder(EntranceDetailActivity.this.entrance);
            } else if (holder.getClass() == EDInformationSectionViewHolder.class) {
                ((EDInformationSectionViewHolder) holder).setupHolder(EntranceDetailActivity.this.entrance);
            } else if (holder.getClass() == EDSaleSectionViewHolder.class) {
                ((EDSaleSectionViewHolder) holder).setupHolder(EntranceDetailActivity.this.entranceStat, EntranceDetailActivity.this.entranceSale, EntranceDetailActivity.this.selfBasketAdd, BasketSingleton.getInstance().getSalesCount());
            } else if (holder.getClass() == EDPurchasedSectionViewHolder.class) {
                ((EDPurchasedSectionViewHolder) holder).setupHolder(EntranceDetailActivity.this.entrancePurchase);
            } else if (holder.getClass() == EDDownloadedSectionViewHolder.class) {
                ((EDDownloadedSectionViewHolder) holder).setupHolder();
            }
        }

        @Override
        public int getItemCount() {
            switch (EntranceDetailActivity.this.state) {
                case EntranceComplete:
                    return 3;
                case ShowSaleInfo:
                case Purchased:
                case Downloaded:
                case DownloadStarted:
                    return 4;
                default:
                    return 0;
            }
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                    return EDViewHolderType.INITIAL_SECTION.getValue();
                case 1:
                    return EDViewHolderType.HEADER_SECTION.getValue();
                case 2:
                    return EDViewHolderType.INFORMATION_SECTION.getValue();
                case 3:
                    if (EntranceDetailActivity.this.state == EntranceVCStateEnum.ShowSaleInfo) {
                        return EDViewHolderType.SALE_SECTION.getValue();
                    } else if (EntranceDetailActivity.this.state == EntranceVCStateEnum.Purchased) {
                        return EDViewHolderType.PURCHASED_SECTION.getValue();
                    } else if (EntranceDetailActivity.this.state == EntranceVCStateEnum.DownloadStarted) {
                        return EDViewHolderType.PURCHASED_SECTION.getValue();
                    } else if (EntranceDetailActivity.this.state == EntranceVCStateEnum.Downloaded) {
                        return EDViewHolderType.DOWNLOADED_SECTION.getValue();
                    }
            }
            return 0;
        }

        // MARK: ViewHolders
        private class EDInitialSectionViewHolder extends RecyclerView.ViewHolder {

            private ImageView esetImageView;
            private TextView entranceTypeTextView;
            private TextView entranceSetTextView;

            public EDInitialSectionViewHolder(View itemView) {
                super(itemView);

                esetImageView = (ImageView) itemView.findViewById(R.id.EDItem_initial_section_eset_image);
                entranceTypeTextView = (TextView) itemView.findViewById(R.id.EDItem_initial_section_type);
                entranceSetTextView = (TextView) itemView.findViewById(R.id.EDItem_initial_section_set);

                entranceTypeTextView.setTypeface(FontCacheSingleton.getInstance(context.getApplicationContext()).getRegular());
                entranceSetTextView.setTypeface(FontCacheSingleton.getInstance(context.getApplicationContext()).getBold());
            }

            public void setupHolder(EntranceStruct es) {
                entranceSetTextView.setText(es.getEntranceSetTitle() + " (" + es.getEntranceGroupTitle() + ")");
                entranceTypeTextView.setText(es.getEntranceTypeTitle() + " " + es.getEntranceOrgTitle());

                if (es.getEntranceSetId() != null) {
                    downloadImage(es.getEntranceSetId());

                }
            }

            private void downloadImage(final int imageId) {
                MediaRestAPIClass.downloadEsetImage(EntranceDetailActivity.this, imageId, esetImageView, new Function2<JsonObject, HTTPErrorType, Unit>() {
                    @Override
                    public Unit invoke(JsonObject jsonObject, HTTPErrorType httpErrorType) {
                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                downloadImage(imageId);
                            } else {
                                // TODO: Set to default
                            }
                        }
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

        private class EDHeaderSectionViewHolder extends RecyclerView.ViewHolder {

            private TextView entranceInfoTextView;
            private TextView entranceExtraDataTextView;

            public EDHeaderSectionViewHolder(View itemView) {
                super(itemView);

                entranceInfoTextView = (TextView) itemView.findViewById(R.id.EDItem_header_section_info);
                entranceExtraDataTextView = (TextView) itemView.findViewById(R.id.EDItem_header_section_extradata);

                entranceInfoTextView.setTypeface(FontCacheSingleton.getInstance(context.getApplicationContext()).getRegular());
                entranceExtraDataTextView.setTypeface(FontCacheSingleton.getInstance(context.getApplicationContext()).getLight());
            }

            public void setupHolder(EntranceStruct es) {
                entranceInfoTextView.setText("اطلاعات آزمون");

                try {
                    JsonObject extraData = es.getEntranceExtraData().getAsJsonObject();

                    if (extraData != null) {
                        String extra = "";
                        ArrayList<String> extraArray = new ArrayList<>();

                        for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
                            extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
                        }

                        extra = TextUtils.join(" - ", extraArray);

                        entranceExtraDataTextView.setText(extra);

                    }
                } catch (Exception exc) {
                }
            }
        }

        private class EDInformationSectionViewHolder extends RecyclerView.ViewHolder {

            private TextView entranceDurationTextView;
            private TextView entranceBookletCountsTextView;
            private TextView entranceYearTextView;

            public EDInformationSectionViewHolder(View itemView) {
                super(itemView);

                entranceDurationTextView = (TextView) itemView.findViewById(R.id.EDItem_information_section_duration);
                entranceBookletCountsTextView = (TextView) itemView.findViewById(R.id.EDItem_information_section_booklet_counts);
                entranceYearTextView = (TextView) itemView.findViewById(R.id.EDItem_information_section_year);

                entranceDurationTextView.setTypeface(FontCacheSingleton.getInstance(context.getApplicationContext()).getRegular());
                entranceBookletCountsTextView.setTypeface(FontCacheSingleton.getInstance(context.getApplicationContext()).getRegular());
                entranceYearTextView.setTypeface(FontCacheSingleton.getInstance(context.getApplicationContext()).getRegular());
            }

            public void setupHolder(EntranceStruct es) {
                try {

                    entranceYearTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(es.getEntranceYear()));
                    entranceBookletCountsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(es.getEntranceBookletCounts()) + " دفترچه");
                    entranceDurationTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(es.getEntranceDuration()) + " دقیقه");

                } catch (Exception exc) {
                }
            }
        }

        private class EDSaleSectionViewHolder extends RecyclerView.ViewHolder {

            private TextView entranceCostTextView;
            private TextView entranceCostLabelTextView;
            private TextView entranceBuyCountTextView;
            private TextView entranceCheckoutSummeryTextView;

            private Button entranceBuyButton;
            private Button entranceCheckoutButton;

            private ConstraintLayout checkoutSection;

            public EDSaleSectionViewHolder(View itemView) {
                super(itemView);

                entranceCostLabelTextView = (TextView) itemView.findViewById(R.id.EDItem_sale_section_cost_label);
                entranceCostTextView = (TextView) itemView.findViewById(R.id.EDItem_sale_section_cost);
                entranceBuyCountTextView = (TextView) itemView.findViewById(R.id.EDItem_sale_section_buy_count);
                entranceCheckoutSummeryTextView = (TextView) itemView.findViewById(R.id.EDItem_sale_section_checkout_summery);

                entranceBuyButton = (Button) itemView.findViewById(R.id.EDItem_sale_section_buy_button);
                entranceCheckoutButton = (Button) itemView.findViewById(R.id.EDItem_sale_section_checkout_button);

                entranceCostTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceCostLabelTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceBuyButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                entranceCheckoutButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceCheckoutSummeryTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceBuyCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

                checkoutSection = (ConstraintLayout) itemView.findViewById(R.id.EDItem_sale_section_checkout);
                checkoutSection.setVisibility(View.GONE);

                entranceCostLabelTextView.setText("قیمت:");
                entranceCheckoutButton.setText("خرید خود را نهایی کنید");

                entranceBuyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (BasketSingleton.getInstance().getBasketId() == null) {
                            BasketSingleton.getInstance().createBasket(EntranceDetailActivity.this);
                        } else {
                            Integer id = BasketSingleton.getInstance().findSaleByTargetId(EntranceDetailActivity.this.entranceUniqueId, "Entrance");
                            if (id != null && id > 0) {
                                BasketSingleton.getInstance().removeSaleById(EntranceDetailActivity.this, id, 0);
                            } else {
                                BasketSingleton.getInstance().addSale(EntranceDetailActivity.this, EntranceDetailActivity.this.entrance, "Entrance");
                            }
                        }
                    }
                });

                entranceCheckoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = BasketCheckoutActivity.newIntent(EntranceDetailActivity.this, "EntranceDetail");
                        EntranceDetailActivity.this.startActivity(i);
//
                    }
                });

            }


            public void setupHolder(EntranceStatStruct statStruct, EntranceSaleStruct saleStruct, Boolean buttonState, Integer basketCount) {
                try {
                    entranceBuyCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(statStruct.purchased) + " خرید");
                    entranceCostTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(saleStruct.cost) + " تومان");
                    if (buttonState) {
                        entranceBuyButton.setText("-  سبد خرید");
                        entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_red_style));
                        entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughRed));
                        checkoutSection.setVisibility(View.VISIBLE);

                    } else {
                        entranceBuyButton.setText("+  سبد خرید");
                        entranceBuyButton.setBackground(getResources().getDrawable(R.drawable.concough_border_radius_style));
                        entranceBuyButton.setTextColor(getResources().getColor(R.color.colorConcoughBlue));
                        checkoutSection.setVisibility(View.GONE);
                    }

                    entranceCheckoutSummeryTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(basketCount) + " قلم در سبد کالا موجود است.");
                } catch (Exception exc) {
                }
            }
        }

        private class EDPurchasedSectionViewHolder extends RecyclerView.ViewHolder {

            private float totalCount = 0.0f;
            private EntrancePackageDownloader downloader = null;

            private LinearLayout downloadingLayout;
            private ImageView refreshImageView;
            private ProgressBar refreshProgressBar;
            private TextView downloadedTextView;
            private TextView downloadingTextView;
            private ProgressBar downloadingProgressBar;
            private ProgressBar downloadProgressBar;
            private Button downloadButton;
            private TextView saleLabelTextView;
            private TextView saleDateTextView;

            public EDPurchasedSectionViewHolder(View itemView) {
                super(itemView);

                downloadingLayout = (LinearLayout) itemView.findViewById(R.id.EDItem_purchased_section_downloading);

                refreshImageView = (ImageView) itemView.findViewById(R.id.EDItem_purchased_section_refresh_img);
                refreshProgressBar = (ProgressBar) itemView.findViewById(R.id.EDItem_purchased_section_download_progress_refresh);
                downloadedTextView = (TextView) itemView.findViewById(R.id.EDItem_purchased_section_downloaded_TextView);
                downloadingTextView = (TextView) itemView.findViewById(R.id.EDItem_purchased_section_downloading_text);
                downloadingProgressBar = (ProgressBar) itemView.findViewById(R.id.EDItem_purchased_section_downloading_progress);
                downloadProgressBar = (ProgressBar) itemView.findViewById(R.id.EDItem_purchased_section_download_progress);
                downloadButton = (Button) itemView.findViewById(R.id.EDItem_purchased_section_download_button);
                saleLabelTextView = (TextView) itemView.findViewById(R.id.EDItem_purchased_section_sale_label);
                saleDateTextView = (TextView) itemView.findViewById(R.id.EDItem_purchased_section_sale_date);

                downloadingTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                downloadedTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                saleLabelTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                saleDateTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

                downloadButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

                refreshImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (EntranceDetailActivity.this.state == EntranceVCStateEnum.Purchased) {
                            showLoading(true);
                        }
                        EntranceDetailActivity.this.refreshUserPurchaseData();
                    }
                });
                DownloaderSingleton.getInstance().setListener(new DownloaderSingleton.DownloaderSingletonListener() {
                    @Override
                    public void onDownloadergetReady(Object downloader, int index) {
//                        EDPurchasedSectionViewHolder.this.downloader = (EntrancePackageDownloader) downloader;
//                        EDPurchasedSectionViewHolder.this.downloader.setListener(new EntrancePackageDownloader.EntrancePackageDownloaderListener() {
//                            @Override
//                            public void onDownloadImagesFinishedForViewHolder(boolean result, int index) {
//
//                            }
//
//                            @Override
//                            public void onDownloadImagesFinished(boolean result) {
//                                if (result) {
//                                    uiHandler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            DownloaderSingleton.getInstance().removeDownloader(entranceUniqueId);
//
//                                            if (EntranceDetailActivity.this.handler != null) {
//                                                Message msg = EntranceDetailActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATE);
//                                                msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));
//
//                                                EntranceDetailActivity.this.handler.sendMessage(msg);
//                                            }
//
//
//                                            JsonObject eData = new JsonObject();
//                                            eData.addProperty("uniqueId", entranceUniqueId);
//                                            EntranceDetailActivity.this.createLog(LogTypeEnum.EntranceDownload.getTitle(), eData);
//
//                                            // TODO: show top message "ActionResult", and message subType = "DownloadSuccess" and type = "success"
//
//                                            EntranceDetailActivity.this.state = EntranceVCStateEnum.Downloaded;
//                                            EntranceDetailActivity.this.stateMachine();
//                                            return;
//                                        }
//                                    });
//                                } else {
//                                    uiHandler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            DownloaderSingleton.getInstance().removeDownloader(entranceUniqueId);
//
//                                            // TODO: show top message "ActionResult", and message subType = "DownloadFailed" and type = "error"
//
//                                            EntranceDetailActivity.this.state = EntranceVCStateEnum.Purchased;
//                                            EntranceDetailActivity.this.stateMachine();
//                                            return;
//
//                                        }
//                                    });
//                                }
//                            }
//
//                            @Override
//                            public void onDownloadProgress(final int count) {
//                                uiHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        changeProgressValue(count);
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onDownloadprogressForViewHolder(int count, int totalCount, int index) {
//
//                            }
//
//                            @Override
//                            public void onDownloadPaused() {
//                                DownloaderSingleton.getInstance().removeDownloader(entranceUniqueId);
//                                uiHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        EntranceDetailActivity.this.state = EntranceVCStateEnum.Purchased;
//                                        EntranceDetailActivity.this.stateMachine();
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onDownloadPausedForViewHolder(int index) {
//
//                            }
//
//                            @Override
//                            public void onDismissActivity(boolean b) {
//                                EntranceDetailActivity.this.finish();
//                            }
//                        });
                    }
                });
            }

            public void setupHolder(EntrancePurchasedStruct entrancePurchased) {
                downloadedTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrancePurchased.downloaded) + " دانلود");
                saleDateTextView.setText(FormatterSingleton.getInstance().getPersianDateString(entrancePurchased.created));

                downloadProgressBar.setVisibility(View.GONE);
                refreshProgressBar.setVisibility(View.GONE);
                downloadButton.setVisibility(View.VISIBLE);

                DownloaderSingleton.getInstance().getMeDownloader(EntranceDetailActivity.this, "Entrance", EntranceDetailActivity.this.entranceUniqueId, 0, new Function2<Object, Integer, Unit>() {
                    @Override
                    public Unit invoke(Object o, Integer integer) {
                        EDPurchasedSectionViewHolder.this.downloader = (EntrancePackageDownloader) o;
                        EDPurchasedSectionViewHolder.this.downloader.setListener(new EntrancePackageDownloader.EntrancePackageDownloaderListener() {
                            @Override
                            public void onDownloadImagesFinishedForViewHolder(boolean result, int index) {

                            }

                            @Override
                            public void onDownloadImagesFinished(boolean result) {
                                if (result) {
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            DownloaderSingleton.getInstance().removeDownloader(entranceUniqueId);

                                            if (EntranceDetailActivity.this.handler != null) {
                                                Message msg = EntranceDetailActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATE);
                                                msg.setTarget(new Handler(EntranceDetailActivity.this.getMainLooper()));

                                                EntranceDetailActivity.this.handler.sendMessage(msg);
                                            }


                                            JsonObject eData = new JsonObject();
                                            eData.addProperty("uniqueId", entranceUniqueId);
                                            EntranceDetailActivity.this.createLog(LogTypeEnum.EntranceDownload.getTitle(), eData);

                                            // TODO: show top message "ActionResult", and message subType = "DownloadSuccess" and type = "success"

                                            EntranceDetailActivity.this.state = EntranceVCStateEnum.Downloaded;
                                            EntranceDetailActivity.this.stateMachine();
                                            return;
                                        }
                                    });
                                } else {
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            DownloaderSingleton.getInstance().removeDownloader(entranceUniqueId);

                                            // TODO: show top message "ActionResult", and message subType = "DownloadFailed" and type = "error"

                                            EntranceDetailActivity.this.state = EntranceVCStateEnum.Purchased;
                                            EntranceDetailActivity.this.stateMachine();
                                            return;

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onDownloadProgress(final int count) {
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeProgressValue(count);
                                    }
                                });
                            }

                            @Override
                            public void onDownloadprogressForViewHolder(int count, int totalCount, int index) {

                            }

                            @Override
                            public void onDownloadPaused() {
                                DownloaderSingleton.getInstance().removeDownloader(entranceUniqueId);
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        EntranceDetailActivity.this.state = EntranceVCStateEnum.Purchased;
                                        EntranceDetailActivity.this.stateMachine();
                                    }
                                });
                            }

                            @Override
                            public void onDownloadPausedForViewHolder(int index) {

                            }

                            @Override
                            public void onDismissActivity(boolean b) {
                                EntranceDetailActivity.this.finish();
                            }
                        });

                        return null;
                    }
                });

                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: show top message msgType="ActionResult" with subMsg = DownloadStarted and type="warning"

                        final String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername(getApplicationContext());
                        if (username != null) {
                            Boolean isDownloaded = PurchasedModelHandler.isInitialDataDownloaded(getApplicationContext(), username, entranceUniqueId, "Entrance");
                            if (isDownloaded) {
                                if (downloader != null) {
                                    downloader.initialize(EntranceDetailActivity.this, entranceUniqueId, "ED", username, 0);
                                    if (downloader.fillImageArray()) {
                                        String newDir = entranceUniqueId;
                                        File f = new File(context.getFilesDir(), newDir);
                                        boolean d = f.mkdir();

                                        Integer count = (Integer) downloader.getDownloadCount();

                                        changeToDownloadState(count);
                                        DownloaderSingleton.getInstance().setDownloaderStarted(entranceUniqueId);
                                        downloader.downloadPackageImages(f);
                                    }
                                }
                            } else {
                                if (downloader != null) {
                                    downloader.initialize(EntranceDetailActivity.this, entranceUniqueId, "ED", username, 0);
                                    downloader.downloadInitialData(new Function2<Boolean, Integer, Unit>() {
                                        @Override
                                        public Unit invoke(Boolean aBoolean, Integer integer) {
                                            if (aBoolean) {
                                                boolean valid2 = PurchasedModelHandler.setIsLocalDBCreatedTrue(getApplicationContext(), username, entranceUniqueId, "Entrance");
                                                if (valid2) {
//                                                    String newDir = context.getFilesDir().getPath().concat(entranceUniqueId);
                                                    String newDir = entranceUniqueId;
                                                    File f = new File(context.getFilesDir(), newDir);
                                                    boolean d = f.mkdir();
                                                    Integer count = (Integer) downloader.getDownloadCount();

                                                    changeToDownloadState(count);
                                                    DownloaderSingleton.getInstance().setDownloaderStarted(entranceUniqueId);
                                                    downloader.downloadPackageImages(f);

                                                }
                                            }
                                            return null;
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

            }

            public void changeToDownloadState(int total) {
                this.downloadButton.setVisibility(View.GONE);
                this.downloadProgressBar.setVisibility(View.VISIBLE);

                this.downloadProgressBar.setProgress(0);
                this.downloadProgressBar.setMax(100);

                this.refreshImageView.setVisibility(View.GONE);
                this.refreshProgressBar.setVisibility(View.GONE);

                this.totalCount = new Float(total);
            }

            public void changeProgressValue(int value) {
                float delta = ((this.totalCount - (float) value) / this.totalCount) * 100;
                this.downloadProgressBar.setProgress((int) delta);
            }

            public void changeToDownloadStartedState() {
                this.downloadButton.setVisibility(View.GONE);
                this.downloadProgressBar.setVisibility(View.GONE);

                this.downloadingLayout.setVisibility(View.VISIBLE);
                this.refreshProgressBar.setVisibility(View.GONE);
                this.refreshImageView.setVisibility(View.GONE);

                this.downloadingProgressBar.setIndeterminate(true);
            }

            public void showLoading(Boolean flag) {
                if (flag) {
                    this.refreshProgressBar.setVisibility(View.VISIBLE);
                    this.refreshProgressBar.setIndeterminate(true);
                    this.refreshImageView.setVisibility(View.GONE);
                } else {
                    this.refreshProgressBar.setVisibility(View.GONE);
                    this.refreshImageView.setVisibility(View.VISIBLE);
                }
            }

            public void updateDownloadedLabel(int count) {
                this.downloadedTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(count) + " دستگاه");
            }


        }

        private class EDDownloadedSectionViewHolder extends RecyclerView.ViewHolder {

            private TextView label;
            private Button showButton;

            public EDDownloadedSectionViewHolder(View itemView) {
                super(itemView);

                label = (TextView) itemView.findViewById(R.id.EDItem_downloaded_section_label);
                showButton = (Button) itemView.findViewById(R.id.EDItem_downloaded_section_show_button);

                label.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                showButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder() {
                showButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = EntranceShowActivity.newIntent(EntranceDetailActivity.this, EntranceDetailActivity.this.entranceUniqueId, "Show");
                        startActivity(i);
                    }
                });
            }
        }
    }
}