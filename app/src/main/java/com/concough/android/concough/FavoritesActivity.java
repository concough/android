package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.concough.android.models.EntranceLastVisitInfoModelHandler;
import com.concough.android.models.EntranceLessonExamModelHandler;
import com.concough.android.models.EntranceQuestionCommentModelHandler;
import com.concough.android.models.EntranceQuestionExamStatModelHandler;
import com.concough.android.services.EntrancePackageDownloader;
import com.concough.android.general.AlertClass;
import com.concough.android.models.EntranceLessonModel;
import com.concough.android.models.EntranceLessonModelHandler;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.EntranceOpenedCountModelHandler;
import com.concough.android.models.EntrancePackageHandler;
import com.concough.android.models.EntranceQuestionModelHandler;
import com.concough.android.models.EntranceQuestionStarredModelHandler;
import com.concough.android.models.PurchasedModel;
import com.concough.android.models.PurchasedModelHandler;
import com.concough.android.models.UserLogModelHandler;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.rest.PurchasedRestAPIClass;
import com.concough.android.singletons.DownloaderSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.MediaCacheSingleton;
import com.concough.android.singletons.NotificationSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.EntrancePurchasedStruct;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.LogTypeEnum;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.MemoryUtilities;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import io.realm.RealmResults;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.concough.android.settings.ConstantsKt.getCONNECTION_MAX_RETRY;
import static com.concough.android.utils.DataConvertorsKt.monthToString;
import static com.concough.android.utils.UtilitiesKt.convertFileToByteArray;

public class FavoritesActivity extends BottomNavigationActivity implements Handler.Callback {
    private class FavoriteItem {
        public String uniqueId;
        public String type;
        public Object object;
        public Object purchased;
        public int starred;
        public int opened;
        public long questionCount;
    }

    private PullRefreshLayout pullRefreshLayout = null;
    private RecyclerView recycleView;
    private FavoritesAdapter favAdapter;

    private HandlerThread handlerThread = null;
    private Handler handler = null;
    private Handler uiHandler = null;

    private static final String TAG = "FavoritesActivity";
    private static final String HANDLE_THREAD_NAME = "Concough-FavoritesActivity";

    private static final int SYNC_WITH_SERVER = 0;
    private static final int UPDATE_USER_PURCHASE_DATA = 1;

    private String showType = "Normal";
    private String selectedShowType = "Show";
    private String entranceUniqueId = "";
    private String username;
    private Integer retryCounter = 0;

    private KProgressHUD loadingProgress;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_favorites;
    }

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, FavoritesActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setMenuSelectedIndex(2);
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

        recycleView = (RecyclerView) findViewById(R.id.favoritesA_recycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);

        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.favoritesA_swipeRefreshLayout);
        pullRefreshLayout.setColorSchemeColors(Color.TRANSPARENT, Color.GRAY, Color.GRAY, Color.GRAY);
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                FavoritesActivity.this.reloadData();
            }
        });

        username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
        actionBarSet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FavoritesActivity.this.entranceUniqueId.equals("") == false) {
            DownloaderSingleton.getInstance().unbind(FavoritesActivity.this, entranceUniqueId);
        }

    }


    private void actionBarSet() {
        final ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();


        ButtonDetail buttonDetail = new ButtonDetail();
        buttonDetail.imageSource = R.drawable.edit;
        buttonDetailArrayList.add(buttonDetail);

//        buttonDetail = new ButtonDetail();
//        buttonDetail.imageSource = R.drawable.download_cloud;
//        buttonDetailArrayList.add(buttonDetail);

        super.createActionBar("کتابخانه من", false, buttonDetailArrayList);


        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {
                switch (id) {
                    case R.drawable.edit: {
                        showType = "Edit";
                        favAdapter.notifyDataSetChanged();

                        buttonDetailArrayList.clear();
                        ButtonDetail buttonDetail = new ButtonDetail();
                        buttonDetail.imageSource = R.drawable.checkmark;
                        buttonDetailArrayList.add(buttonDetail);

                        FavoritesActivity.super.createActionBar("ویرایش کتابخانه", false, buttonDetailArrayList);
                        break;
                    }

                    case R.drawable.checkmark: {
                        showType = "Normal";
                        loadData();
                        actionBarSet();
                        break;
                    }

//                    case R.drawable.download_cloud: {
//                        if (FavoritesActivity.this.handler != null) {
//                            Message msg = FavoritesActivity.this.handler.obtainMessage(SYNC_WITH_SERVER);
//                            msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));
//
//                            FavoritesActivity.this.handler.sendMessage(msg);
//                        }
//                        break;
//                    }
                }
            }

            @Override
            public void OnBackClicked() {
                if (FavoritesActivity.this.recycleView != null) {
                    FavoritesActivity.this.recycleView.smoothScrollToPosition(0);
                }
            }

            @Override
            public void OnTitleClicked() {

            }
        };
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.favorite_edit) {
//            if ("Normal".equals(showType)) {
//                showType = "Edit";
//                favAdapter.notifyDataSetChanged();
//            } else if ("Edit".equals(showType)) {
//                showType = "Normal";
//                loadData();
//            }
//
//            invalidateOptionsMenu();
//        } else if (item.getItemId() == R.id.favorite_sync) {
//            if (FavoritesActivity.this.handler != null) {
//                Message msg = FavoritesActivity.this.handler.obtainMessage(SYNC_WITH_SERVER);
//                msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));
//
//                FavoritesActivity.this.handler.sendMessage(msg);
//            }
//        }
//        return super.onOptionsItemSelected(item);
//    }


    @Override
    protected void onResume() {
        super.onResume();

        favAdapter = new FavoritesAdapter(this, new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        recycleView.setAdapter(favAdapter);

        this.loadData();
        //this.loadLessonData();
    }

    public void reloadData() {
        this.loadData();
        //this.loadLessonData();
    }

    private void loadData() {
        try {
            ArrayList<FavoriteItem> localPurchased = new ArrayList<>();
            ArrayList<FavoriteItem> localNotDownloaded = new ArrayList<>();
            ArrayList<Integer> localDownloadCount = new ArrayList<>();

            String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
            if (username != null) {
                RealmResults<PurchasedModel> items = PurchasedModelHandler.getAllPurchased(getApplicationContext(), username);
                if (items != null && !items.isEmpty()) {
                    for (int i = 0; i < items.size(); i++) {
                        PurchasedModel item = items.get(i);
                        if ("Entrance".equals(item.productType)) {
                            EntrancePurchasedStruct purchased = new EntrancePurchasedStruct();
                            purchased.id = item.id;
                            purchased.amount = 0;
                            purchased.downloaded = item.downloadTimes;
                            purchased.isDataDownloaded = item.isLocalDBCreated;
                            purchased.isImagesDownloaded = item.isImageDownloaded;
                            purchased.isDownloaded = item.isDownloaded;

                            EntranceModel entrance = EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, item.productUniqueId);
                            if (entrance != null) {
                                JsonElement extraData = new JsonParser().parse(entrance.extraData);

                                EntranceStruct entranceS = new EntranceStruct();
                                entranceS.setEntranceSetId(entrance.setId);
                                entranceS.setEntranceYear(entrance.year);
                                entranceS.setEntranceMonth(entrance.month);
                                entranceS.setEntranceUniqueId(entrance.uniqueId);
                                entranceS.setEntranceTypeTitle(entrance.type);
                                entranceS.setEntranceBookletCounts(entrance.bookletsCount);
                                entranceS.setEntranceDuration(entrance.duration);
                                entranceS.setEntranceExtraData(extraData);
                                entranceS.setEntranceGroupTitle(entrance.group);
                                entranceS.setEntranceLastPublished(entrance.lastPublished);
                                entranceS.setEntranceOrgTitle(entrance.organization);
                                entranceS.setEntranceSetTitle(entrance.set);

                                Log.d(TAG, "loadData: "+entrance.pUniqueId);

                                int starCount = EntranceQuestionStarredModelHandler.countByEntranceId(getApplicationContext(), username, item.productUniqueId);
                                int openedCount = EntranceOpenedCountModelHandler.countByEntranceId(getApplicationContext(), username, item.productUniqueId);
                                long qCount = EntranceQuestionModelHandler.countQuestions(getApplicationContext(), username, item.productUniqueId);

                                FavoriteItem fav = new FavoriteItem();
                                fav.uniqueId = entranceS.getEntranceUniqueId();
                                fav.type = "Entrance";
                                fav.object = entranceS;
                                fav.purchased = purchased;
                                fav.starred = starCount;
                                fav.opened = openedCount;
                                fav.questionCount = qCount;

                                if (purchased.isDownloaded) {
                                    localPurchased.add(fav);
                                    localDownloadCount.add(localPurchased.size() - 1);
                                } else {
                                    localNotDownloaded.add(fav);
                                }
                            }
                        }
                    }
                }

                pullRefreshLayout.setRefreshing(false);
                favAdapter.setItems(localPurchased, localNotDownloaded, localDownloadCount);
                favAdapter.notifyDataSetChanged();
            }

        } catch (Exception exc) {
            Log.d(TAG, "loadData: " + exc.getMessage());
        }
    }

    private void loadLessonData() {
        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
        ArrayList<EntranceLessonModel> items = EntranceLessonModelHandler.getAllLessons(getApplicationContext(),
                username);

        HashMap<String, Integer> result = new HashMap<>();
        for(EntranceLessonModel item: items) {
            if (result.containsKey(item.fullTitle)) {
                result.put(item.fullTitle, result.get(item.fullTitle) + item.qCount);
            } else {
                result.put(item.fullTitle, item.qCount);
            }
        }
    }

    private void syncWithServer(@Nullable Message msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    if (loadingProgress == null) {
                        loadingProgress = AlertClass.showLoadingMessage(FavoritesActivity.this);
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

        PurchasedRestAPIClass.getPurchasedList(getApplicationContext(), new Function2<JsonElement, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertClass.hideLoadingMessage(loadingProgress);

                        if (httpErrorType != HTTPErrorType.Success) {
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                if (FavoritesActivity.this.handler != null) {
                                    Message msg = FavoritesActivity.this.handler.obtainMessage(SYNC_WITH_SERVER);
                                    msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));

                                    FavoritesActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                if (FavoritesActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                    FavoritesActivity.this.retryCounter += 1;

                                    if (FavoritesActivity.this.handler != null) {
                                        Message msg = FavoritesActivity.this.handler.obtainMessage(SYNC_WITH_SERVER);
                                        msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));

                                        FavoritesActivity.this.handler.sendMessage(msg);
                                    }
                                } else {
                                    FavoritesActivity.this.retryCounter = 0;
                                    AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                }
                            }
                        } else {
                            FavoritesActivity.this.retryCounter = 0;
                            if (jsonElement != null) {
                                String status = jsonElement.getAsJsonObject().get("status").getAsString();
                                switch (status) {
                                    case "OK":
                                        try {
                                            ArrayList<Integer> purchasedId = new ArrayList<Integer>();
                                            JsonArray records = jsonElement.getAsJsonObject().get("records").getAsJsonArray();
                                            String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                            if (username != null) {
                                                for (JsonElement record : records) {
                                                    int id = record.getAsJsonObject().get("id").getAsInt();
                                                    int downloaded = record.getAsJsonObject().get("downloaded").getAsInt();
                                                    String createdStr = record.getAsJsonObject().get("created").getAsString();
                                                    Date created = FormatterSingleton.getInstance().getUTCDateFormatter().parse(createdStr);

                                                    JsonElement target = record.getAsJsonObject().get("target");
                                                    String targetType = target.getAsJsonObject().get("product_type").getAsString();

                                                    if (PurchasedModelHandler.getByUsernameAndId(getApplicationContext(), username, id) != null) {
                                                        PurchasedModelHandler.updateDownloadTimes(getApplicationContext(), username, id, downloaded);

                                                        if ("Entrance".equals(targetType)) {
                                                            String uniqueId = target.getAsJsonObject().get("unique_key").getAsString();
                                                            if (EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, uniqueId) == null) {
                                                                String org = target.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();
                                                                String type = target.getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString();
                                                                String setName = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString();
                                                                String group = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString();
                                                                int setId = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
                                                                int bookletsCount = target.getAsJsonObject().get("booklets_count").getAsInt();
                                                                int duration = target.getAsJsonObject().get("duration").getAsInt();
                                                                int year = target.getAsJsonObject().get("year").getAsInt();
                                                                int month = target.getAsJsonObject().get("month").getAsInt();

                                                                String extraStr = target.getAsJsonObject().get("extra_data").getAsString();
                                                                JsonElement extraData = null;
                                                                if (extraStr != null && !"".equals(extraStr)) {
                                                                    try {
                                                                        extraData = new JsonParser().parse(extraStr);
                                                                    } catch (Exception exc) {
                                                                        extraData = new JsonParser().parse("[]");
                                                                    }
                                                                }

                                                                String lastPublishedStr = target.getAsJsonObject().get("last_published").getAsString();
                                                                Date lastPublished = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublishedStr);

                                                                    EntranceStruct entrance = new EntranceStruct();
                                                                    entrance.setEntranceSetId(setId);
                                                                    entrance.setEntranceSetTitle(setName);
                                                                    entrance.setEntranceOrgTitle(org);
                                                                    entrance.setEntranceLastPublished(lastPublished);
                                                                    entrance.setEntranceBookletCounts(bookletsCount);
                                                                    entrance.setEntranceDuration(duration);
                                                                    entrance.setEntranceExtraData(extraData);
                                                                    entrance.setEntranceGroupTitle(group);
                                                                    entrance.setEntranceTypeTitle(type);
                                                                    entrance.setEntranceUniqueId(uniqueId);
                                                                    entrance.setEntranceYear(year);
                                                                    entrance.setEntranceMonth(month);

                                                                    EntranceModelHandler.add(getApplicationContext(), username, entrance);
                                                            }
                                                        }
                                                    } else {

                                                        if ("Entrance".equals(targetType)) {
                                                            String uniqueId = target.getAsJsonObject().get("unique_key").getAsString();

                                                            if (PurchasedModelHandler.add(getApplicationContext(), id, username, false, downloaded, false, targetType, uniqueId, created)) {
                                                                String org = target.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();
                                                                String type = target.getAsJsonObject().get("entrance_type").getAsJsonObject().get("title").getAsString();
                                                                String setName = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("title").getAsString();
                                                                String group = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("group").getAsJsonObject().get("title").getAsString();
                                                                int setId = target.getAsJsonObject().get("entrance_set").getAsJsonObject().get("id").getAsInt();
                                                                int bookletsCount = target.getAsJsonObject().get("booklets_count").getAsInt();
                                                                int duration = target.getAsJsonObject().get("duration").getAsInt();
                                                                int year = target.getAsJsonObject().get("year").getAsInt();
                                                                int month = target.getAsJsonObject().get("month").getAsInt();

                                                                String extraStr = target.getAsJsonObject().get("extra_data").getAsString();
                                                                JsonElement extraData = null;
                                                                if (extraStr != null &&  !"".equals(extraStr)) {
                                                                    try {
                                                                        extraData = new JsonParser().parse(extraStr);
                                                                    } catch (Exception exc) {
                                                                        extraData = new JsonParser().parse("[]");
                                                                    }
                                                                }

                                                                String lastPublishedStr = target.getAsJsonObject().get("last_published").getAsString();
                                                                Date lastPublished = FormatterSingleton.getInstance().getUTCDateFormatter().parse(lastPublishedStr);

                                                                if (EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, uniqueId) == null) {
                                                                    EntranceStruct entrance = new EntranceStruct();
                                                                    entrance.setEntranceSetId(setId);
                                                                    entrance.setEntranceSetTitle(setName);
                                                                    entrance.setEntranceOrgTitle(org);
                                                                    entrance.setEntranceLastPublished(lastPublished);
                                                                    entrance.setEntranceBookletCounts(bookletsCount);
                                                                    entrance.setEntranceDuration(duration);
                                                                    entrance.setEntranceExtraData(extraData);
                                                                    entrance.setEntranceGroupTitle(group);
                                                                    entrance.setEntranceTypeTitle(type);
                                                                    entrance.setEntranceUniqueId(uniqueId);
                                                                    entrance.setEntranceYear(year);
                                                                    entrance.setEntranceMonth(month);

                                                                    EntranceModelHandler.add(getApplicationContext(), username, entrance);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    purchasedId.add(id);
                                                }

                                                Integer[] dat = new Integer[purchasedId.size()];
                                                for (int i = 0; i < purchasedId.size(); i++) {
                                                    dat[i] = purchasedId.get(i);
                                                }

                                                RealmResults<PurchasedModel> deletedItems = PurchasedModelHandler.getAllPurchasedNotIn(getApplicationContext(), username, dat);
                                                if (deletedItems.size() > 0) {
                                                    for (PurchasedModel pm : deletedItems) {
                                                        FavoritesActivity.this.deletePurchaseData(pm.productUniqueId, username);

                                                        if ("Entrance".equals(pm.productType)) {
                                                            if (EntranceModelHandler.removeById(getApplicationContext(), username, pm.productUniqueId)) {
                                                                EntranceOpenedCountModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
                                                                EntranceQuestionStarredModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
                                                                PurchasedModelHandler.removeById(getApplicationContext(), username, pm.id);
                                                            }
                                                        }
                                                    }
                                                }

                                                purchasedIds(dat);
                                                FavoritesActivity.this.loadData();
                                            }
                                        } catch (Exception exc) {
                                            //Log.d(TAG, exc.getLocalizedMessage());
                                        }
                                        break;
                                    case "Error":
                                        String errorType = jsonElement.getAsJsonObject().get("error_type").getAsString();
                                        switch (errorType) {
                                            case "EmptyArray":
                                                String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                                if (username != null) {
                                                    RealmResults<PurchasedModel> items = PurchasedModelHandler.getAllPurchased(getApplicationContext(), username);

                                                    for (PurchasedModel pm : items) {
                                                        FavoritesActivity.this.deletePurchaseData(pm.productUniqueId, username);

                                                        if ("Entrance".equals(pm.productType)) {
                                                            if (EntranceModelHandler.removeById(getApplicationContext(), username, pm.productUniqueId)) {
                                                                EntranceOpenedCountModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
                                                                EntranceQuestionStarredModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
                                                                PurchasedModelHandler.removeById(getApplicationContext(), username, pm.id);
                                                            }
                                                        }
                                                    }

                                                    FavoritesActivity.this.loadData();

                                                }

                                                break;
                                            default:
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
                        AlertClass.hideLoadingMessage(loadingProgress);

                        if (FavoritesActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                            FavoritesActivity.this.retryCounter += 1;

                            if (FavoritesActivity.this.handler != null) {
                                Message msg = FavoritesActivity.this.handler.obtainMessage(SYNC_WITH_SERVER);
                                msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));

                                FavoritesActivity.this.handler.sendMessage(msg);
                            }
                        } else {
                            FavoritesActivity.this.retryCounter = 0;
                            if (networkErrorType != null) {
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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
    }

    private void purchasedIds(Integer[] ids) {

        RealmResults<PurchasedModel> purchasedIn = PurchasedModelHandler.getAllPurchasedIn(getApplicationContext(), username, ids);
        if (purchasedIn != null) {
            for (PurchasedModel purchasedModel : purchasedIn) {
                if (purchasedModel.productType.equals("Entrance")) {
                    EntranceModel entranceModel = EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, purchasedModel.productUniqueId);
                    if (entranceModel != null) {
                        downloadImage(entranceModel.setId);
                    }
                }
            }
        }
    }


    private void downloadImage(final int imageId) {
        final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);

        if (url != null) {
            byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
            if (data != null) {
                saveToFile(data, imageId);
            } else {
                MediaRestAPIClass.downloadEsetImage(FavoritesActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
                    @Override
                    public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (httpErrorType != HTTPErrorType.Success) {
                                    Log.d(TAG, "run: ");
                                    if (httpErrorType == HTTPErrorType.Refresh) {
                                        downloadImage(imageId);
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


//    private void downloadImage(final int imageId) {
//        final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
//
//        if (url != null) {
//            byte[]  data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
//            if (data != null) {
//
//                File folder = new File(getApplicationContext().getFilesDir(),"images");
//                File folder2 = new File(getApplicationContext().getFilesDir()+"/images","eset");
//                if (!folder.exists()) {
//                    folder.mkdir();
//                    folder2.mkdir();
//                }
//
//                File photo=new File(getApplicationContext().getFilesDir()+"/images/eset", String.valueOf(imageId));
//                if (photo.exists()) {
//                    photo.delete();
//                }
//
//                try {
//                    FileOutputStream fos=new FileOutputStream(photo.getPath());
//
//                    fos.write(data);
//                    fos.close();
//                }
//                catch (java.io.IOException e) {
//                    Log.e("PictureDemo", "Exception in photoCallback", e);
//                }
//            }
//        }
//
//    }



    private void deletePurchaseData(String path, String username) {
        String finalPath = username + "_" + path;

        File f = new File(FavoritesActivity.this.getFilesDir(), finalPath);
        if (!(f.exists() && f.isDirectory())) {
            f = new File(FavoritesActivity.this.getFilesDir(), path);
        }

        if (f.exists() && f.isDirectory()) {
            for (File fc : f.listFiles()) {
                fc.delete();
            }
            boolean rd = f.delete();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case SYNC_WITH_SERVER:
                    FavoritesActivity.this.syncWithServer(msg);
                    break;
                case UPDATE_USER_PURCHASE_DATA:
                    FavoritesActivity.this.handleUpdateUserPurchaseData(msg);
                    break;
            }
        }
        return true;
    }

    private enum FavViewHolderType {
        ENTRANCE_NOT_DOWNLOADED(1),
        ENTRANCE_DOWNLOADED(2),
        ENTRANCE_DELETE(3),
        FAVORITE_HEADER(4),
        EMPTY_RECYCLE(50);

        private final int value;

        private FavViewHolderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private void createLog(String logType, JsonObject extraData) {
        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
        if (username != null) {
            String uniqueId = UUID.randomUUID().toString();
            Date created = new Date();

            try {
                UserLogModelHandler.add(getApplicationContext(), username, uniqueId, created, logType, extraData);
            } catch (Exception exc) {
            }
        }
    }

    private void handleUpdateUserPurchaseData(@Nullable Message msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    if (loadingProgress == null) {
                        loadingProgress = AlertClass.showLoadingMessage(FavoritesActivity.this);
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

        Bundle bundle = msg.getData();
        if (bundle == null)
            return;
        final String productId = bundle.getString("PRODUCT_ID");
        final String productType = bundle.getString("PRODUCT_TYPE");

        if ("Entrance".equals(productType)) {
            PurchasedRestAPIClass.putEntrancePurchasedDownload(FavoritesActivity.this, productId, new Function2<JsonElement, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonElement jsonElement, final HTTPErrorType httpErrorType) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertClass.hideLoadingMessage(loadingProgress);
                            if (httpErrorType != HTTPErrorType.Success) {
                                if (httpErrorType == HTTPErrorType.Refresh) {
                                    if (FavoritesActivity.this.handler != null) {
                                        Message msg = FavoritesActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATA);
                                        msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));

                                        Bundle bundle = new Bundle();
                                        bundle.putString("PRODUCT_ID", productId);
                                        bundle.putString("PRODUCT_TYPE", productType);
                                        msg.setData(bundle);

                                        FavoritesActivity.this.handler.sendMessage(msg);
                                    }
                                } else {
                                    if (FavoritesActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                        FavoritesActivity.this.retryCounter += 1;

                                        if (FavoritesActivity.this.handler != null) {
                                            Message msg = FavoritesActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATA);
                                            msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));

                                            Bundle bundle = new Bundle();
                                            bundle.putString("PRODUCT_ID", productId);
                                            bundle.putString("PRODUCT_TYPE", productType);
                                            msg.setData(bundle);

                                            FavoritesActivity.this.handler.sendMessage(msg);
                                        }
                                    } else {
                                        FavoritesActivity.this.retryCounter = 0;
                                        AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                    }
                                }
                            } else {
                                FavoritesActivity.this.retryCounter = 0;
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

                                                        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                                        if (username != null) {
                                                            PurchasedModelHandler.updateDownloadTimes(getApplicationContext(), username, id, downloaded);
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
                                                    break;
                                                default:
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
                            AlertClass.hideLoadingMessage(loadingProgress);

                            if (FavoritesActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                FavoritesActivity.this.retryCounter += 1;

                                if (FavoritesActivity.this.handler != null) {
                                    Message msg = FavoritesActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATA);
                                    msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));

                                    Bundle bundle = new Bundle();
                                    bundle.putString("PRODUCT_ID", productId);
                                    bundle.putString("PRODUCT_TYPE", productType);
                                    msg.setData(bundle);

                                    FavoritesActivity.this.handler.sendMessage(msg);
                                }
                            } else {
                                FavoritesActivity.this.retryCounter = 0;
                                if (networkErrorType != null) {
                                    switch (networkErrorType) {
                                        case NoInternetAccess:
                                        case HostUnreachable: {
                                            AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                            break;
                                        }
                                        default: {
                                            AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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
        }
    }

    private class FavoritesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private ArrayList<FavoriteItem> purchased;
        private ArrayList<FavoriteItem> notDownloaded;
        private ArrayList<Integer> DownloadCount;

        public FavoritesAdapter(Context context,
                                ArrayList<FavoriteItem> items,
                                ArrayList<FavoriteItem> nitems,
                                ArrayList<Integer> dCounts) {
            this.context = context;
            this.purchased = items;
            this.notDownloaded = nitems;
            this.DownloadCount = dCounts;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == FavViewHolderType.EMPTY_RECYCLE.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_recycle_not_item, parent, false);
                return new ItemEmptyHolder(view);
            } else if (viewType == FavViewHolderType.ENTRANCE_NOT_DOWNLOADED.getValue()) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_favorite_entrance_not_downloaded, parent, false);
                return new FEntranceNotDownloadViewHolder(view);
            } else if (viewType == FavViewHolderType.ENTRANCE_DOWNLOADED.getValue()) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_favorite_entrance_downloaded, parent, false);
                return new FEntranceDownloadViewHolder(view);
            } else if (viewType == FavViewHolderType.ENTRANCE_DELETE.getValue()) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_favorite_entrance_delete, parent, false);
                return new FEntranceDeleteViewHolder(view);
            } else if (viewType == FavViewHolderType.FAVORITE_HEADER.getValue()) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.item_favorite_header, parent, false);
                return new FHeaderViewHeader(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getClass() == ItemEmptyHolder.class) {
                ItemEmptyHolder itemEmptyHolder = (ItemEmptyHolder) holder;
                itemEmptyHolder.setupHolder();
            } else if (holder.getClass() == FEntranceNotDownloadViewHolder.class) {
                FavoriteItem item = this.notDownloaded.get(position - this.purchased.size() - 2);
                EntrancePurchasedStruct purchasedData = (EntrancePurchasedStruct) item.purchased;
                ((FEntranceNotDownloadViewHolder) holder).setupHolder((EntranceStruct) item.object, purchasedData, position);
            } else if (holder.getClass() == FEntranceDownloadViewHolder.class) {
                FavoriteItem item = this.purchased.get(position - 1);
                EntrancePurchasedStruct purchasedData = (EntrancePurchasedStruct) item.purchased;
                ((FEntranceDownloadViewHolder) holder).setupHolder((EntranceStruct) item.object, purchasedData, position, item.starred, item.opened, item.questionCount);
            } else if (holder.getClass() == FEntranceDeleteViewHolder.class) {
                Integer i = this.DownloadCount.get(position);
                FavoriteItem item = this.purchased.get(i);
                ((FEntranceDeleteViewHolder) holder).setupHolder(item, position);
            } else if (holder.getClass() == FHeaderViewHeader.class) {
                if (position == 0) {
                    ((FHeaderViewHeader) holder).setupHolder("آزمون ها");
                } else if (position == this.purchased.size() + 1) {
                    ((FHeaderViewHeader) holder).setupHolder("دانلود نشده");
                }
            }
        }

        @Override
        public int getItemCount() {
            switch (FavoritesActivity.this.showType) {
                case "Normal":
                    if (purchased.size() == 0 && notDownloaded.size() == 0) {
                        return 1;
                    } else {
                        return this.purchased.size() + this.notDownloaded.size() + 2;
                    }
                case "Edit":
                    return this.DownloadCount.size();
            }
            return 0;
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            if (holder.getClass() == FEntranceNotDownloadViewHolder.class) {
                ((FEntranceNotDownloadViewHolder) holder).stopDownloader();
            }
        }

        public void setItems(ArrayList<FavoriteItem> items,
                             ArrayList<FavoriteItem> nitems,
                             ArrayList<Integer> dCounts) {
            this.purchased = items;
            this.notDownloaded = nitems;
            this.DownloadCount = dCounts;
        }

        @Override
        public int getItemViewType(int position) {
            if ("Normal".equals(FavoritesActivity.this.showType)) {
                if (this.purchased.size() == 0 && this.notDownloaded.size() == 0) {
                    return FavViewHolderType.EMPTY_RECYCLE.getValue();
                } else {
                    if (position == 0) {
                        return FavViewHolderType.FAVORITE_HEADER.getValue();
                    } else if (position < this.purchased.size() + 1) {
                        FavoriteItem item = this.purchased.get(position - 1);
                        if ("Entrance".equals(item.type)) {
                            return FavViewHolderType.ENTRANCE_DOWNLOADED.getValue();
                        }
                    } else if (position == this.purchased.size() + 1) {
                        return FavViewHolderType.FAVORITE_HEADER.getValue();
                    } else {
                        FavoriteItem item = this.notDownloaded.get(position - this.purchased.size() - 2);
                        if ("Entrance".equals(item.type)) {
                            return FavViewHolderType.ENTRANCE_NOT_DOWNLOADED.getValue();
                        }
                    }
                }
            } else if ("Edit".equals(FavoritesActivity.this.showType)) {
                if (this.purchased.size() != 0) {
                    Integer i = this.DownloadCount.get(position);
                    FavoriteItem item = this.purchased.get(i);
                    if ("Entrance".equals(item.type)) {
                        return FavViewHolderType.ENTRANCE_DELETE.getValue();
                    }
                }
            }
            return 0;
        }


        // MARK: ViewHolders
        class ItemEmptyHolder extends RecyclerView.ViewHolder {

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
                emptyImage.setImageResource(R.drawable.book_shelf);
                emptyText.setText(R.string.favorite_empty_recycle);
            }
        }

        class FEntranceNotDownloadViewHolder extends RecyclerView.ViewHolder {
            private EntrancePackageDownloader downloader = null;

            private EntranceStruct entranceS;
            private EntrancePurchasedStruct purchasedS;

            private TextView entranceOrgTextView;
            private TextView entranceSetTextView;
            private TextView entranceExtraDataTextView;
            private ImageView entranceSetImage;
            private TextView entranceBookletCountTextView;
            private TextView entranceDurationTextView;
            private TextView entranceYearTextView;
            private TextView entranceMonthTextView;
            private Button downloadButton;
            private TextView entranceDownloadCountTextView;
            private LinearLayout entranceDownloadLayout;
            private ProgressBar isDownloadingProgressBar;
            private ConstraintLayout downloadProgress2;
            private LinearLayout downloadProgress2Level;
            private LinearLayout line;

            private int widthI = 0;

            public FEntranceNotDownloadViewHolder(View itemView) {
                super(itemView);

                entranceOrgTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_org);
                entranceSetTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_set);
                entranceExtraDataTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_extra_data);
                entranceSetImage = (ImageView) itemView.findViewById(R.id.FItem_entrance_set_image);
                entranceBookletCountTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_booklets_count);
                entranceDurationTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_duration);
                entranceDownloadCountTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_download_count);
                entranceYearTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_yearTextView);
                entranceMonthTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_monthTextView);
                entranceDownloadLayout = (LinearLayout) itemView.findViewById(R.id.FItem_entrance_downloadContainer);

                downloadProgress2 = (ConstraintLayout) itemView.findViewById(R.id.FItem_entrance_download_progress2);
                downloadProgress2Level = (LinearLayout) itemView.findViewById(R.id.FItem_entrance_download_progress_level);
                isDownloadingProgressBar = (ProgressBar) itemView.findViewById(R.id.isDownloadingProgressBar);

                line = (LinearLayout) itemView.findViewById(R.id.FItem_entrance_line_gray);
                downloadButton = (Button) itemView.findViewById(R.id.FItem_entrance_downloadButton);

                entranceOrgTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceSetTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                entranceExtraDataTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceBookletCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceDurationTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceDownloadCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceYearTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceMonthTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                downloadButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
            }

            public void setupHolder(final EntranceStruct entrance, final EntrancePurchasedStruct purchased, final int index) {
                this.entranceS = entrance;
                this.purchasedS = purchased;

                FEntranceNotDownloadViewHolder.this.line.post(new Runnable() {
                    @Override
                    public void run() {
                        widthI = FEntranceNotDownloadViewHolder.this.line.getWidth();
                        Log.d(TAG, "width " + widthI);
                    }
                });

                downloadProgress2.setVisibility(View.GONE);
                isDownloadingProgressBar.setVisibility(View.GONE);

                FEntranceNotDownloadViewHolder.this.downloader = null;
                FEntranceNotDownloadViewHolder.this.entranceDownloadLayout.setVisibility(View.GONE);

                entranceOrgTextView.setText("آزمون " + entrance.getEntranceTypeTitle().trim());
                entranceYearTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceYear()).trim());

                if (entrance.getEntranceMonth() > 0) {
                    entranceMonthTextView.setText(monthToString(entrance.getEntranceMonth()));
                } else {
                    entranceMonthTextView.setText("");
                    entranceMonthTextView.setVisibility(View.GONE);
                }
                entranceSetTextView.setText(entrance.getEntranceSetTitle().trim() + " (" + entrance.getEntranceGroupTitle().trim() + ")");

                try {
//                    JsonObject extraData = entrance.getEntranceExtraData().getAsJsonObject();
//
//                    if (extraData != null) {
//                        String extra = "";
//                        ArrayList<String> extraArray = new ArrayList<>();
//
//                        for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
//                            extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
//                        }
//
//                        extra = TextUtils.join(" - ", extraArray);

                        entranceExtraDataTextView.setText(entrance.getEntranceOrgTitle().trim());

//                    }
                } catch (Exception exc) {
                }

                entranceBookletCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceBookletCounts()) + " دفترچه");
                entranceDurationTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceDuration()) + " دقیقه");
                entranceDownloadCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(purchased.downloaded) + " بار دانلود شده است");
                if( purchased.isDataDownloaded) {
                    downloadButton.setText("ادامه دانلود");
                }

                downloadImage(entrance.getEntranceSetId());
                checkForState(index);
            }
//            private void setDownloader(Object downloader) {
//
//                this.downloader = (EntrancePackageDownloader) downloader;
//                FEntranceNotDownloadViewHolder.this.downloader.setListener(new EntrancePackageDownloader.EntrancePackageDownloaderListener() {
//                    @Override
//                    public void onDownloadImagesFinishedForViewHolder(boolean result, final int index) {
//                        if (result) {
//                            uiHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    DownloaderSingleton.getInstance().removeDownloader(entranceS.getEntranceUniqueId());
//
//                                    if (FavoritesActivity.this.handler != null) {
//                                        Message msg = FavoritesActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATA);
//                                        msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));
//
//                                        Bundle bundle = new Bundle();
//                                        bundle.putString("PRODUCT_ID", entranceS.getEntranceUniqueId());
//                                        bundle.putString("PRODUCT_TYPE", "Entrance");
//                                        msg.setData(bundle);
//
//                                        FavoritesActivity.this.handler.sendMessage(msg);
//                                    }
//
//
//                                    JsonObject eData = new JsonObject();
//                                    eData.addProperty("uniqueId", entranceS.getEntranceUniqueId());
//                                    FavoritesActivity.this.createLog(LogTypeEnum.EntranceDownload.getTitle(), eData);
//
//                                    String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername(getApplicationContext());
//                                    if (username != null) {
//                                        FavoriteItem item = favAdapter.purchased.get(index);
//                                        PurchasedModel p = PurchasedModelHandler.getByProductId(getApplicationContext(), username, item.type, item.uniqueId);
//                                        if (p != null) {
//                                            EntrancePurchasedStruct ps = new EntrancePurchasedStruct();
//                                            ps.id = p.id;
//                                            ps.created = p.created;
//                                            ps.amount = 0;
//                                            ps.downloaded = p.downloadTimes;
//                                            ps.isDownloaded = p.isDownloaded;
//                                            ps.isDataDownloaded = p.isLocalDBCreated;
//                                            ps.isImagesDownloaded = p.isImageDownloaded;
//
//                                            long qCount = EntranceQuestionModelHandler.countQuestions(getApplicationContext(), username, item.uniqueId);
//
//                                            FavoriteItem fav = new FavoriteItem();
//                                            fav.uniqueId = item.uniqueId;
//                                            fav.type = item.type;
//                                            fav.object = item.object;
//                                            fav.purchased = ps;
//                                            fav.starred = 0;
//                                            fav.opened = 0;
//                                            fav.questionCount = qCount;
//
//                                            favAdapter.purchased.set(index, fav);
//                                            favAdapter.DownloadCount.add(index);
//                                            favAdapter.notifyItemRangeChanged(index, 1);
//
//                                        }
//                                    }
//
//                                }
//                            });
//                        } else {
//                            uiHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    DownloaderSingleton.getInstance().removeDownloader(entranceS.getEntranceUniqueId());
//
//                                }
//                            });
//                        }
//
//                    }
//
//                    @Override
//                    public void onDownloadImagesFinished(boolean result) {
//                    }
//
//                    @Override
//                    public void onDownloadProgress(final int count) {
//                    }
//
//                    @Override
//                    public void onDownloadprogressForViewHolder(final int count, final int totalCount, int index) {
//                        uiHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                changeProgressValue(count, totalCount);
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void onDownloadPaused() {
//                    }
//
//                    @Override
//                    public void onDownloadPausedForViewHolder(int index) {
//                        DownloaderSingleton.getInstance().removeDownloader(entranceS.getEntranceUniqueId());
//                        uiHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onDismissActivity(boolean b) {
////                                FavoritesActivity.this.finish();
//                    }
//                });
//
//                if (DownloaderSingleton.getInstance().getDownloaderState(entranceS.getEntranceUniqueId()) == DownloaderSingleton.DownloaderState.Started) {
//                    changeToDownlaodState(FEntranceNotDownloadViewHolder.this.downloader.getDownloadCount().intValue());
//                }
//
//            }

            private void checkForState(int index) {
                if (DownloaderSingleton.getInstance().getDownloaderState(entranceS.getEntranceUniqueId()) == DownloaderSingleton.DownloaderState.Started) {
                    DownloaderSingleton.getInstance().getMeDownloader(FavoritesActivity.this, "Entrance", entranceS.getEntranceUniqueId(), index, new Function2<Object, Integer, Unit>() {
                        @Override
                        public Unit invoke(final Object o, final Integer integer) {
                            isDownloadingProgressBar.setVisibility(View.VISIBLE);
                            FEntranceNotDownloadViewHolder.this.downloader = (EntrancePackageDownloader) o;

                            setListener(integer);
                            changeToDownlaodState(0);
                            FavoritesActivity.this.entranceUniqueId = entranceS.getEntranceUniqueId();
                            return null;
                        }
                    });
                } else {
                    setOnClickListener(index);
                }

            }

            private void setListener(int index) {

                FEntranceNotDownloadViewHolder.this.downloader.registerActivity(FavoritesActivity.this, "F", index);
                FEntranceNotDownloadViewHolder.this.downloader.setListener(new EntrancePackageDownloader.EntrancePackageDownloaderListener() {
                    @Override
                    public void onDownloadImagesFinishedForViewHolder(boolean result, final int index) {
                        FEntranceNotDownloadViewHolder.this.onDownloadFinished(result, index);
                    }

                    @Override
                    public void onDownloadImagesFinished(boolean result) {
                    }

                    @Override
                    public void onDownloadProgress(final int count) {
                    }

                    @Override
                    public void onDownloadprogressForViewHolder(final int count, final int totalCount, int index) {
                        changeProgressValue(count, totalCount);
                    }

                    @Override
                    public void onDownloadPaused() {
                    }

                    @Override
                    public void onDownloadPausedForViewHolder(int index) {
                        DownloaderSingleton.getInstance().removeDownloader(entranceS.getEntranceUniqueId());
                        FavoritesActivity.this.entranceUniqueId = "";
                        FavoritesAdapter.this.notifyItemChanged(1);
                    }

                    @Override
                    public void onDismissActivity(boolean b) {
//                                FavoritesActivity.this.finish();
                    }
                });

            }

            private void onDownloadFinished(boolean result, final int index) {
                if (result) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DownloaderSingleton.getInstance().removeDownloader(entranceS.getEntranceUniqueId());

                            if (FavoritesActivity.this.handler != null) {
                                Message msg = FavoritesActivity.this.handler.obtainMessage(UPDATE_USER_PURCHASE_DATA);
                                msg.setTarget(new Handler(FavoritesActivity.this.getMainLooper()));

                                Bundle bundle = new Bundle();
                                bundle.putString("PRODUCT_ID", entranceS.getEntranceUniqueId());
                                bundle.putString("PRODUCT_TYPE", "Entrance");
                                msg.setData(bundle);

                                FavoritesActivity.this.handler.sendMessage(msg);
                            }


                            JsonObject eData = new JsonObject();
                            eData.addProperty("uniqueId", entranceS.getEntranceUniqueId());
                            FavoritesActivity.this.createLog(LogTypeEnum.EntranceDownload.getTitle(), eData);

                            String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                            if (username != null) {
                                FavoriteItem item = FavoritesAdapter.this.notDownloaded.get(index - FavoritesAdapter.this.purchased.size() - 2);
                                if (item.type.equals("Entrance")) {
                                    PurchasedModel p = PurchasedModelHandler.getByProductId(getApplicationContext(), username, item.type, item.uniqueId);
                                    if (p != null) {
                                        EntrancePurchasedStruct ps = new EntrancePurchasedStruct();
                                        ps.id = p.id;
                                        ps.created = p.created;
                                        ps.amount = 0;
                                        ps.downloaded = p.downloadTimes;
                                        ps.isDownloaded = p.isDownloaded;
                                        ps.isDataDownloaded = p.isLocalDBCreated;
                                        ps.isImagesDownloaded = p.isImageDownloaded;

                                        long openedCount = EntranceOpenedCountModelHandler.countByEntranceId(getApplicationContext(),
                                                username, item.uniqueId);
                                        long bookmarkedCount = EntranceQuestionStarredModelHandler.countByEntranceId(getApplicationContext(),
                                                username, item.uniqueId);
                                        long qCount = EntranceQuestionModelHandler.countQuestions(getApplicationContext(), username, item.uniqueId);

                                        FavoriteItem fav = new FavoriteItem();
                                        fav.uniqueId = item.uniqueId;
                                        fav.type = item.type;
                                        fav.object = item.object;
                                        fav.purchased = ps;
                                        fav.starred = (int) bookmarkedCount;
                                        fav.opened = (int) openedCount;
                                        fav.questionCount = qCount;

                                        notDownloaded.remove(index - FavoritesAdapter.this.purchased.size() - 2);
                                        notifyItemRemoved(index);

                                        purchased.add(fav);
                                        DownloadCount.add(FavoritesAdapter.this.purchased.size() - 1);
                                        notifyItemInserted(FavoritesAdapter.this.purchased.size());

                                        FavoritesActivity.this.recycleView.smoothScrollToPosition(purchased.size());
                                        FavoritesActivity.this.recycleView.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                RecyclerView.ViewHolder holder = FavoritesActivity.this.recycleView.findViewHolderForAdapterPosition(purchased.size());
                                                if (holder.getClass() == FEntranceDownloadViewHolder.class) {
                                                    ((FEntranceDownloadViewHolder) holder).changeBackground();
                                                }
                                            }
                                        }, 1000);
                                    }
                                }
                            }

                            AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "ActionResult", "DownloadSuccess", "success", null);
                            FavoritesActivity.this.entranceUniqueId = "";
                        }
                    });
                } else {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DownloaderSingleton.getInstance().removeDownloader(entranceS.getEntranceUniqueId());
                            AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "ActionResult", "DownloadFailed", "error", null);
                            FavoritesAdapter.this.notifyItemRangeChanged(index, 1);
                            FavoritesActivity.this.entranceUniqueId = "";
                        }
                    });
                }
            }

            private void setOnClickListener(final int index) {
                entranceDownloadLayout.setVisibility(View.VISIBLE);

                FEntranceNotDownloadViewHolder.this.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MemoryUtilities.getMemorySize() <= 30) {
                            AlertClass.showAlertMessage(FavoritesActivity.this, "SystemError", "LowMemory", "warning", null);
                            return;
                        }

                        if (DownloaderSingleton.getInstance().getIsInDownloadProgress()) {
                            AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "DownloadError", "DownloadInProgress", "warning", null);
                            downloadButton.setClickable(true);
                            return;
                        } else {
                            downloadButton.setClickable(false);
                        }

                        if (!DownloaderSingleton.getInstance().getIsInDownloadProgress()) {
                            AlertClass.showTopMessage(FavoritesActivity.this, findViewById(R.id.container), "ActionResult", "DownloadStarted", "warning", null);
                            DownloaderSingleton.getInstance().getMeDownloader(FavoritesActivity.this, "Entrance", entranceS.getEntranceUniqueId(), index, new Function2<Object, Integer, Unit>() {
                                @Override
                                public Unit invoke(final Object o, final Integer integer) {
                                    uiHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            FEntranceNotDownloadViewHolder.this.downloader = (EntrancePackageDownloader) o;

                                            setListener(integer);

                                            if (DownloaderSingleton.getInstance().getDownloaderState(entranceS.getEntranceUniqueId()) == DownloaderSingleton.DownloaderState.Initialize) {
                                                entranceDownloadLayout.setVisibility(View.VISIBLE);
                                                FavoritesActivity.this.entranceUniqueId = entranceS.getEntranceUniqueId();
                                            }


                                            if (username != null) {
                                                Boolean isDownloaded = PurchasedModelHandler.isInitialDataDownloaded(getApplicationContext(), username, entranceS.getEntranceUniqueId(), "Entrance");
                                                if (isDownloaded) {
                                                    if (FEntranceNotDownloadViewHolder.this.downloader != null) {
                                                        FEntranceNotDownloadViewHolder.this.downloader.initialize(FavoritesActivity.this, entranceS.getEntranceUniqueId(), "F", username, index);
                                                        if (FEntranceNotDownloadViewHolder.this.downloader.fillImageArray()) {
                                                            String newDir = entranceS.getEntranceUniqueId();
                                                            File f = new File(context.getFilesDir(), newDir);

                                                            if (!f.exists()) {
                                                                newDir = username + "_" + newDir;
                                                                f = new File(context.getFilesDir(), newDir);
                                                            }

                                                            boolean d = f.mkdir();

                                                            Integer count = (Integer) FEntranceNotDownloadViewHolder.this.downloader.getDownloadCount();

                                                            changeToDownlaodState(count);
                                                            DownloaderSingleton.getInstance().setDownloaderStarted(entranceS.getEntranceUniqueId());
                                                            FEntranceNotDownloadViewHolder.this.downloader.downloadPackageImages(f);
                                                        } else {
                                                            PurchasedModelHandler.setIsDownloadedTrue(getApplicationContext(),
                                                                    username,
                                                                    entranceS.getEntranceUniqueId(), "Entrance");

                                                            EntranceModel em = EntranceModelHandler.getByUsernameAndId(getApplicationContext(),
                                                                    username,
                                                                    entranceS.getEntranceUniqueId());
                                                            if (em != null) {
                                                                String message = "دانلود آزمون به اتمام رسید";
                                                                String subMassage = entranceS.getEntranceTypeTitle() + " " +
                                                                        monthToString(entranceS.getEntranceMonth()) + " " +
                                                                        FormatterSingleton.getInstance().getNumberFormatter().format(entranceS.getEntranceYear()) + "\n" +
                                                                        entranceS.getEntranceSetTitle() + " (" + entranceS.getEntranceGroupTitle() + ")";

                                                                NotificationSingleton.getInstance(FavoritesActivity.this).simpleNotification(message, subMassage);
                                                            }

                                                            FEntranceNotDownloadViewHolder.this.onDownloadFinished(true, index);
                                                        }
                                                    }
                                                } else {
                                                    if (FEntranceNotDownloadViewHolder.this.downloader != null) {
                                                        FEntranceNotDownloadViewHolder.this.downloader.initialize(FavoritesActivity.this, entranceS.getEntranceUniqueId(), "F", username, index);

                                                        FEntranceNotDownloadViewHolder.this.downloader.downloadInitialData(new Function2<Boolean, Integer, Unit>() {
                                                            @Override
                                                            public Unit invoke(Boolean aBoolean, Integer integer) {
                                                                if (aBoolean) {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            boolean valid2 = PurchasedModelHandler.setIsLocalDBCreatedTrue(getApplicationContext(), username, entranceS.getEntranceUniqueId(), "Entrance");
                                                                            if (valid2) {
//                                                    String newDir = context.getFilesDir().getPath().concat(entranceUniqueId);
                                                                                String newDir = username + "_" + entranceS.getEntranceUniqueId();
                                                                                File f = new File(context.getFilesDir(), newDir);
                                                                                boolean d = f.mkdir();
                                                                                Integer count = (Integer) FEntranceNotDownloadViewHolder.this.downloader.getDownloadCount();

                                                                                changeToDownlaodState(count);
                                                                                isDownloadingProgressBar.setVisibility(View.VISIBLE);
                                                                                DownloaderSingleton.getInstance().setDownloaderStarted(entranceS.getEntranceUniqueId());
                                                                                FEntranceNotDownloadViewHolder.this.downloader.downloadPackageImages(f);

                                                                            }

                                                                        }
                                                                    });
                                                                }
                                                                return null;
                                                            }
                                                        });
                                                    }
                                                }
                                            }


                                        }
                                    }, 50);
                                    return null;
                                }
                            });

                        }


                    }
                });

            }

            private void downloadImage(final int imageId) {
                byte[] data;
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);

                File photo=new File(getApplicationContext().getFilesDir()+"/images/eset", String.valueOf(imageId));
                if (photo.exists()) {
                    data = convertFileToByteArray(photo);
                    //Log.d(TAG, "downloadImage: From File");
                } else {
                      data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                }

                if (data != null) {

                    Glide.with(FavoritesActivity.this)

                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(entranceSetImage)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(FavoritesActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                                    if (httpErrorType != HTTPErrorType.Success) {
                                        //Log.d(TAG, "run: ");
                                        if (httpErrorType == HTTPErrorType.Refresh) {
                                            downloadImage(imageId);
                                        } else {
                                            entranceSetImage.setImageResource(R.drawable.no_image);
                                        }
                                    } else {
                                        if (url != null) {
                                            MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);
                                        }

                                        Glide.with(FavoritesActivity.this)

                                                .load(data)
                                                //.crossFade()
                                                .dontAnimate()
                                                .into(entranceSetImage)
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

            public void changeToDownlaodState(final int total) {
                FEntranceNotDownloadViewHolder.this.entranceDownloadLayout.setVisibility(View.INVISIBLE);
                FEntranceNotDownloadViewHolder.this.downloadProgress2.setVisibility(View.VISIBLE);
//                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) downloadProgress2Level.getLayoutParams();

                ViewGroup.LayoutParams params = downloadProgress2Level.getLayoutParams();
                params.width = 0;
                FEntranceNotDownloadViewHolder.this.downloadProgress2Level.setLayoutParams(params);
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });

//                downloadProgress.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        downloadProgress.invalidate();
//                    }
//                });
            }

            public void stopDownloader() {
                if (downloader != null)
                    downloader.setListener(null);
            }

            public void changeProgressValue(final int value, final int totalC) {
                FEntranceNotDownloadViewHolder.this.downloadProgress2.setVisibility(View.VISIBLE);
                int chunck = (int) (widthI / 100);

                float delta = (((float) totalC - (float) value) / totalC) * 100;
                if (delta < 8) delta = 8;

                ViewGroup.LayoutParams params = FEntranceNotDownloadViewHolder.this.downloadProgress2Level.getLayoutParams();
                params.width = (int) (chunck * delta);
                FEntranceNotDownloadViewHolder.this.downloadProgress2Level.setLayoutParams(params);

            }
        }

        class FEntranceDownloadViewHolder extends RecyclerView.ViewHolder {
            private TextView entranceOrgTextView;
            private TextView entranceSetTextView;
            private TextView entranceExtraDataTextView;
            private ImageView entranceSetImage;
            private TextView entranceYearTextView;
            private TextView entranceMonthTextView;
            private TextView entranceBookletCountTextView;
            private TextView entranceDurationTextView;
            private TextView entranceQuestionCountTextView;
            private TextView entranceOpenedCountTextView;
            private TextView entranceBookmarkedCountTextView;
            private ImageView entranceOpenedCountImageView;
            private ImageView entranceBookmarkedCountImageView;

            private Button entranceOpenButton;
            private Button entranceShowBookmarkedButton;

            private ConstraintLayout container;

            public FEntranceDownloadViewHolder(View itemView) {
                super(itemView);

                container = (ConstraintLayout) itemView.findViewById(R.id.FItem_entrance_downloaded_container);
                entranceYearTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_yearTextView);
                entranceMonthTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_monthTextView);
                entranceOrgTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_org);
                entranceSetTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_set);
                entranceExtraDataTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_extra_data);
                entranceSetImage = (ImageView) itemView.findViewById(R.id.FItem_entrance_set_image);
                entranceBookletCountTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_booklets_count);
                entranceDurationTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_duration);
                entranceQuestionCountTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_question_count);
                entranceOpenedCountTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_open_normal_countTextView);
                entranceBookmarkedCountTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_bookmarkedTextView);
                entranceOpenedCountImageView = (ImageView) itemView.findViewById(R.id.FItem_entrance_open_normal_countImageView);
                entranceBookmarkedCountImageView = (ImageView) itemView.findViewById(R.id.FItem_entrance_bookmarkedImageView);

                entranceOpenButton = (Button) itemView.findViewById(R.id.FItem_entrance_showNormalButton);
                entranceShowBookmarkedButton = (Button) itemView.findViewById(R.id.FItem_entrance_showStarredButton);

                entranceYearTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceMonthTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceOrgTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceSetTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                entranceExtraDataTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceBookletCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceDurationTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceQuestionCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

                entranceOpenButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceShowBookmarkedButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceOpenedCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceBookmarkedCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
            }

            public void setupHolder(final EntranceStruct entrance, EntrancePurchasedStruct purchased, int index, final int starCount, int openedCount, long qCount) {
                entranceOrgTextView.setText("آزمون " + entrance.getEntranceTypeTitle().trim() + " ");
                entranceSetTextView.setText(entrance.getEntranceSetTitle().trim() + " (" + entrance.getEntranceGroupTitle().trim() + ")");

                entranceYearTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceYear()));
                if (entrance.getEntranceMonth() > 0) {
                    entranceMonthTextView.setText(monthToString(entrance.getEntranceMonth()));
                    entranceMonthTextView.setVisibility(View.VISIBLE);
                } else {
                    entranceMonthTextView.setText("");
                    entranceMonthTextView.setVisibility(View.GONE);
                }

                try {
//                    JsonObject extraData = entrance.getEntranceExtraData().getAsJsonObject();
//
//                    if (extraData != null) {
//                        String extra = "";
//                        ArrayList<String> extraArray = new ArrayList<>();
//
//                        for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
//                            extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
//                        }
//
//                        extra = TextUtils.join(" - ", extraArray);

                        entranceExtraDataTextView.setText(entrance.getEntranceOrgTitle());

//                    }
                } catch (Exception exc) {
                }

                entranceBookletCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceBookletCounts()) + " دفترچه");
                entranceDurationTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceDuration()) + " دقیقه");
                entranceQuestionCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(qCount) + " سوال");

                entranceOpenedCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(openedCount));
                entranceBookmarkedCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(starCount));

                downloadImage(entrance.getEntranceSetId());

                if (starCount > 0) {
                    this.entranceBookmarkedCountImageView.setImageResource(R.drawable.bookmark_100_red);
                    this.entranceShowBookmarkedButton.setTextColor(ContextCompat.getColor(FavoritesActivity.this, R.color.colorConcoughBlue));
                    this.entranceShowBookmarkedButton.setBackground(ContextCompat.getDrawable(FavoritesActivity.this, R.drawable.concough_border_radius_style));
                } else {
                    this.entranceBookmarkedCountImageView.setImageResource(R.drawable.bookmark_100_gray);
                    this.entranceShowBookmarkedButton.setTextColor(ContextCompat.getColor(FavoritesActivity.this, R.color.colorConcoughGray));
                    this.entranceShowBookmarkedButton.setBackground(ContextCompat.getDrawable(FavoritesActivity.this, R.drawable.concough_border_radius_lightgray_style));
                }

                entranceOpenButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = EntranceShowActivity.newIntent(FavoritesActivity.this, entrance.getEntranceUniqueId(), "Show");
                        startActivity(i);
                    }
                });

                entranceShowBookmarkedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (starCount == 0) {
                            AlertClass.showAlertMessage(FavoritesActivity.this, "EntranceResult", "EntranceStarredNotExist", "", new Function0<Unit>() {
                                @Override
                                public Unit invoke() {
                                    return null;
                                }
                            });
                        } else {
                            Intent i = EntranceShowActivity.newIntent(FavoritesActivity.this, entrance.getEntranceUniqueId(), "Starred");
                            startActivity(i);
                        }
                    }
                });


            }

            private void downloadImage(final int imageId) {
                byte[] data;
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);

                File photo=new File(getApplicationContext().getFilesDir()+"/images/eset", String.valueOf(imageId));
                if (photo.exists()) {
                    data = convertFileToByteArray(photo);
                    //Log.d(TAG, "downloadImage: From File");
                } else {
                    data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                }
//
//                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
//                byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                if (data != null) {

                    Glide.with(FavoritesActivity.this)

                            .load(data)

                            //.crossFade()
                            .dontAnimate()
                            .into(entranceSetImage)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(FavoritesActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
                                    if (httpErrorType != HTTPErrorType.Success) {
                                        //Log.d(TAG, "run: ");
                                        if (httpErrorType == HTTPErrorType.Refresh) {
                                            downloadImage(imageId);
                                        } else {
                                            entranceSetImage.setImageResource(R.drawable.no_image);
                                        }
                                    } else {
                                        if (url != null) {
                                            MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);
                                        }

                                        Glide.with(FavoritesActivity.this)

                                                .load(data)

                                                //.crossFade()
                                                .dontAnimate()
                                                .into(entranceSetImage)
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

            public void changeBackground() {
                container.setBackground(ContextCompat.getDrawable(FavoritesActivity.this, R.drawable.concough_recycle_box_style_yellow));
                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        container.setBackground(ContextCompat.getDrawable(FavoritesActivity.this, R.drawable.concough_recycle_box_style));
                    }
                }, 2000);
            }
        }

        class FEntranceDeleteViewHolder extends RecyclerView.ViewHolder {

            private TextView entranceOrgTextView;
            private TextView entranceSetTextView;
            private TextView entranceExtraDataTextView;
            private ImageView entranceSetImage;
            private TextView entranceDeleteTextView;
            private LinearLayout entranceDeleteLayout;

            FEntranceDeleteViewHolder(View itemView) {
                super(itemView);

                entranceOrgTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_org);
                entranceSetTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_set);
                entranceExtraDataTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_extra_data);
                entranceSetImage = (ImageView) itemView.findViewById(R.id.FItem_entrance_set_image);
                entranceDeleteTextView = (TextView) itemView.findViewById(R.id.FItem_entrance_delete_text);
                entranceDeleteLayout = (LinearLayout) itemView.findViewById(R.id.FItem_entrance_delete_layout);

                entranceOrgTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                entranceSetTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                entranceExtraDataTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                entranceDeleteTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            void setupHolder(final FavoriteItem favoriteItem, final int index) {
                final EntranceStruct entrance = (EntranceStruct) favoriteItem.object;
                entranceOrgTextView.setText("آزمون " + entrance.getEntranceTypeTitle().trim() + " " +
                        FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceYear()));
                entranceSetTextView.setText(entrance.getEntranceSetTitle().trim() + " (" + entrance.getEntranceGroupTitle().trim() + ")");

                try {
//                    JsonObject extraData = entrance.getEntranceExtraData().getAsJsonObject();
//
//                    if (extraData != null) {
//                        String extra = "";
//                        ArrayList<String> extraArray = new ArrayList<>();
//
//                        for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
//                            extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
//                        }

//                        extra = TextUtils.join(" - ", extraArray);

                        entranceExtraDataTextView.setText(entrance.getEntranceOrgTitle().trim());

//                    }
                } catch (Exception exc) {
                }


                downloadImage(entrance.getEntranceSetId());

                entranceDeleteLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertClass.showAlertMessageCustom(FavoritesActivity.this, "آیا مطمئنید؟", "تنها اطلاعات آزمون حذف خواهد شد و مجددا قابل بارگزاری است", "بله", "خیر", new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                try {
                                    String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
                                    if (username != null) {
                                        String newDir = favoriteItem.uniqueId;
                                        FavoritesActivity.this.deletePurchaseData(newDir, username);

                                        if (PurchasedModelHandler.resetDownloadFlags(getApplicationContext(), username, ((EntrancePurchasedStruct) favoriteItem.purchased).id)) {
                                            EntrancePackageHandler.removePackage(getApplicationContext(), username, favoriteItem.uniqueId);
                                            EntranceQuestionStarredModelHandler.removeByEntranceId(getApplicationContext(), username, favoriteItem.uniqueId);
                                            EntranceOpenedCountModelHandler.removeByEntranceId(getApplicationContext(), username, favoriteItem.uniqueId);
                                            EntranceLastVisitInfoModelHandler.removeByEntranceId(getApplicationContext(), username, favoriteItem.uniqueId);
                                            EntranceQuestionCommentModelHandler.removeAllCommentOfEntrance(getApplicationContext(), username, favoriteItem.uniqueId);
                                            EntranceLessonExamModelHandler.removeAllExamsByEntranceId(getApplicationContext(), username, favoriteItem.uniqueId);
                                            EntranceQuestionExamStatModelHandler.removeAllStatsByEntranceId(getApplicationContext(), username, favoriteItem.uniqueId);

                                            DownloadCount.remove(index);
                                            favAdapter.notifyItemRemoved(index);
                                            //favAdapter.notifyItemRangeChanged(index, getItemCount());
                                        }
                                    }
                                } catch (Exception exc) {
                                }
                                return null;
                            }
                        });

                    }
                });
            }

            private void downloadImage(final int imageId) {
                byte[] data;
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);

                File photo=new File(getApplicationContext().getFilesDir()+"/images/eset", String.valueOf(imageId));
                if (photo.exists()) {
                    data = convertFileToByteArray(photo);
                    Log.d(TAG, "downloadImage: From File");
                } else {
                    data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                }

                if (data != null) {

                    Glide.with(FavoritesActivity.this)

                            .load(data)

                            //.crossFade()
                            .dontAnimate()
                            .into(entranceSetImage)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(FavoritesActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
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
                                    entranceSetImage.setImageResource(R.drawable.no_image);
                                }
                            } else {
                                MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);

                                Glide.with(FavoritesActivity.this)

                                        .load(data)

                                        //.crossFade()
                                        .dontAnimate()
                                        .into(entranceSetImage)
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

        class FHeaderViewHeader extends RecyclerView.ViewHolder {
            private TextView titleTextView;

            public FHeaderViewHeader(View itemView) {
                super(itemView);

                this.titleTextView = (TextView) itemView.findViewById(R.id.itemFEH_titleTextView);
                this.titleTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }

            public void setupHolder(String title) {
                this.titleTextView.setText(title);
            }
        }

    }


}
