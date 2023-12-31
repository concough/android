package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.concough.android.extensions.RotateViewExtensions;
import com.concough.android.general.AlertClass;
import com.concough.android.rest.ArchiveRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.MediaCacheSingleton;
import com.concough.android.structures.ArchiveEsetDetailStruct;
import com.concough.android.structures.ArchiveEsetStructs;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.concough.android.settings.ConstantsKt.getCONNECTION_MAX_RETRY;

public class ArchiveActivity extends BottomNavigationActivity {
    private final String TAG = "ArchiveActivity";

    //    private ArrayAdapter<String> adapter;
    private DialogAdapter adapter;
    private ArrayAdapter<String> adapterTab;
    private ArrayList<String> typeList;

    private ArrayList<JsonElement> dropDownJsonElement;

    private ArrayList<JsonElement> tabbarJsonElement;

    private GetSetsAdapter adapterSet;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DialogPlus dialog;
    private Button texButton;
    private ViewPager mViewPager;
    private RecyclerView recycleView;
    private Button refreshButton;
    private View mCustomView;
    private LinearLayout linearLayout;

//    private KProgressHUD loadingProgress;

    private Integer currentPositionDropDown;
    private Integer currentGroupSelected;
    private Boolean gettingSets = false;

    private CustomTabLayout tabLayout;
    private AppBarLayout appBar;

    private JsonElement cacheTypes;
    private ArrayList<JsonElement> cacheGroups;
    private ArrayList<JsonElement> cacheSets;

    private ArchiveEsetDetailStruct mArchiveEsetDetailStruct;
    private ArchiveEsetStructs mArchiveEsetStructs;

    private Integer retryCounter = 0;

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, ArchiveActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_archive;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setMenuSelectedIndex(1);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_archive);

        mArchiveEsetDetailStruct = new ArchiveEsetDetailStruct();
        mArchiveEsetStructs = new ArchiveEsetStructs();


        typeList = new ArrayList<String>();
        dropDownJsonElement = new ArrayList<JsonElement>();
        tabbarJsonElement = new ArrayList<JsonElement>();
        adapterTab = new ArrayAdapter<String>(this, R.layout.cc_archive_listitem_tabbar);
        linearLayout = (LinearLayout) findViewById(R.id.container);

        recycleView = (RecyclerView) findViewById(R.id.archiveA_recycleDetail);
        adapterSet = new GetSetsAdapter(this, new ArrayList<JsonElement>());
        recycleView.setAdapter(adapterSet);
        recycleView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appBar = (AppBarLayout) findViewById(R.id.appbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.containerView);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Tab
        tabLayout = (CustomTabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        tabLayout.setTabGravity(TabLayout.GRAVITY_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            tabLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
//                int id = ArchiveActivity.this.tabbarJsonElement.get(index).getAsJsonObject().get("id").getAsInt();
                changeGroupIndex(index);

                Log.d(TAG, "onTabSelected: ");
//                getSets(id);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        int toolBarHeight = toolbar.getLayoutParams().height;


        typeList = new ArrayList<>();
        adapter = new DialogAdapter(ArchiveActivity.this, typeList);
        dialog = DialogPlus.newDialog(this)
                .setAdapter(adapter)

                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        currentGroupSelected = -1;
                        ArchiveActivity.this.adapterSet.setItems(new ArrayList<JsonElement>());
                        ArchiveActivity.this.adapterSet.notifyDataSetChanged();
                        ArchiveActivity.this.changeTypeIndex(position);
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogPlus dialog) {
//                        texButton.setText(buttonTextMaker(typeList.get(ArchiveActivity.this.currentPositionDropDown), false));
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .setGravity(Gravity.TOP)
                .setOutMostMargin(0, toolBarHeight, 0, 0)
                .create();


        final ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setElevation(2);
        }

        LayoutInflater mInflater = LayoutInflater.from(this);
        mCustomView = mInflater.inflate(R.layout.cc_archive_actionbar, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            appBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            tabLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        texButton = (Button) mCustomView
                .findViewById(R.id.actionBarL_dropDown);
        texButton.setText("");


        if (mActionBar != null) {
            mActionBar.setCustomView(mCustomView, new ActionBar.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mActionBar.setDisplayShowCustomEnabled(true);
        }


        texButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                } else {
                    dialog.show();
                }

            }

        });

        texButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        refreshButton = (Button) mCustomView.findViewById(R.id.actionBarL_refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(adapter.mArrayList.size()==0) {
//                }
                RotateViewExtensions.buttonRotateStart(refreshButton, getApplicationContext());
//                if (ArchiveActivity.this.adapter.mArrayList.size() == 0) { //avoid duplicate dropdown
                ArchiveActivity.this.texButton.setText("دریافت اطلاعات ...");
                getTypes();
//                }
                //   changeTypeIndex(0);
            }
        });

        final PullRefreshLayout pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.homeA_swipeRefreshLayout);
        pullRefreshLayout.setColorSchemeColors(Color.TRANSPARENT, Color.GRAY, Color.GRAY, Color.GRAY);
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (currentGroupSelected != null && currentGroupSelected > -1) {
                    getSets(currentGroupSelected);
                }
                pullRefreshLayout.setRefreshing(false);
            }
        });

        ArchiveActivity.this.texButton.setText("دریافت اطلاعات ...");
        getTypes();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        AlertClass.hideLoadingMessage(ArchiveActivity.this.loadingProgress);
//        ArchiveActivity.this.loadingProgress = null;
    }

    @Override
    protected void onStop() {
//        AlertClass.hideLoadingMessage(loadingProgress);
//        loadingProgress = null;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarSet();
    }

    private void actionBarSet() {
        TextView badgeCount = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon0Badge);
        LinearLayout badgeLinear = (LinearLayout) mCustomView.findViewById(R.id.badgeLinear);

//        badgeLinear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = BasketCheckoutActivity.newIntent(ArchiveActivity.this, "EntranceDetail");
//                ArchiveActivity.this.startActivity(i);
//            }
//        });
//
//        badgeCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
//        if (BasketSingleton.getInstance().getSalesCount() > 0) {
//            String basketCount = FormatterSingleton.getInstance().getNumberFormatter().format(BasketSingleton.getInstance().getSalesCount());
//            badgeCount.setText(basketCount);
//            badgeLinear.setVisibility(View.VISIBLE);
//        } else {
//            badgeLinear.setVisibility(View.GONE);
//        }
    }


    private String buttonTextMaker(String txt, boolean doOpen) {
        int up = 0x25B2;
        int down = 0x25BC;

        if (doOpen) {
            return new String(Character.toChars(up)) + "   " + txt;
        } else {
            return new String(Character.toChars(down)) + "   " + txt;
        }
    }

    // Custom Functions
    private void changeTypeIndex(int index) {

        ArchiveActivity.this.currentPositionDropDown = index;
        ArchiveActivity.this.texButton.setText(buttonTextMaker((String) ArchiveActivity.this.adapter.getItem(index).toString().trim(), false));
        int id = ArchiveActivity.this.dropDownJsonElement.get(index).getAsJsonObject().get("id").getAsInt();
        getTab(id);
    }

    private void changeGroupIndex(final int index) {
        if (index >= 0) {
            Integer i = ArchiveActivity.this.tabbarJsonElement.get(index).getAsJsonObject().get("id").getAsInt();
//        ArchiveActivity.this.currentPositionDropDown = index;
            getSets(i);
//            getSets(i);
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            ArchiveActivity.this.tabLayout.getTabAt(index).select();
                        }
                    });
        }
    }


    // Asyncs

    private void getTypes() {
        new GetTypesTask().execute();
    }

    private void getTab(int typeId) {
        new GetGroupsTask().execute(typeId);
//        if(ArchiveActivity.this.tabLayout.getTabCount() > 0) {
//            ArchiveActivity.this.tabLayout.getTabAt(0).select();
//        }
    }

    private void getSets(int groupId) {
        new GetSetsTask().execute(groupId);

    }

    // Asyncs

    private class GetTypesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(final Void... params) {

            ArchiveRestAPIClass.getEntranceTypes(getApplicationContext(), new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //AlertClass.hideLoadingMessage(loadingProgress);

                            RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());
                            try {
                                if (httpErrorType == HTTPErrorType.Success) {
                                    ArchiveActivity.this.retryCounter = 0;
                                    if (jsonObject != null) {

                                        String status = jsonObject.get("status").getAsString();
                                        switch (status) {
                                            case "OK": {


                                                HashMap<Integer, String> jsonHashMap = new HashMap<>();
                                                JsonObject json = jsonObject.getAsJsonObject();
                                                JsonArray leaders = json.getAsJsonArray("record");


                                                ArrayList<JsonElement> localListJson = new ArrayList<JsonElement>();
                                                ArrayList<String> localList = new ArrayList<String>();
                                                for (JsonElement je : leaders) {
                                                    String j = je.getAsJsonObject().get("title").getAsString();
                                                    if (!j.equals("")) {
                                                        int i = je.getAsJsonObject().get("id").getAsInt();
                                                        jsonHashMap.put(i, j);
                                                        localListJson.add(je);
                                                        localList.add(j);
                                                    }
                                                }


                                                ArchiveActivity.this.adapter.setItems(localList);
                                                ArchiveActivity.this.dropDownJsonElement.clear();
                                                ArchiveActivity.this.dropDownJsonElement.addAll(localListJson);
                                                ArchiveActivity.this.adapter.notifyDataSetChanged();


                                                ArchiveActivity.this.changeTypeIndex(0);


                                                break;
                                            }
                                            case "Error": {
                                                String errorType = jsonObject.get("error_type").getAsString();
                                                switch (errorType) {
                                                    case "EmptyArray": {
                                                        AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "ErrorResult", errorType, "", null);
                                                        break;
                                                    }
                                                    default:
                                                        break;
                                                }
                                                break;
                                            }
                                        }

                                    } // object is not null if
                                } else if (httpErrorType == HTTPErrorType.Refresh) {
                                    new GetTypesTask().execute(params);
                                } else {
                                    if (ArchiveActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                        ArchiveActivity.this.retryCounter += 1;
                                        new GetTypesTask().execute(params);
                                    } else {
                                        ArchiveActivity.this.retryCounter = 0;
                                        AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                    }
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "run: ");
                                ArchiveActivity.this.gettingSets = false;
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
                            RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());

                            if (ArchiveActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                ArchiveActivity.this.retryCounter += 1;
                                new GetTypesTask().execute(params);
                            } else {
                                ArchiveActivity.this.retryCounter = 0;
                                switch (networkErrorType) {
                                    case NoInternetAccess:
                                    case HostUnreachable: {
                                        AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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
//            loadingProgress = AlertClass.showLoadingMessage(ArchiveActivity.this);
//            loadingProgress.show();
        }

    }

    private class GetGroupsTask extends AsyncTask<Integer, Void, Void> {

        private int firstIndexOfTypes;

        @Override
        protected Void doInBackground(final Integer... params) {

            if (params == null) {
                firstIndexOfTypes = ArchiveActivity.this.dropDownJsonElement.get(0).getAsJsonObject().get("id").getAsInt();
            } else {
                firstIndexOfTypes = params[0];
            }
            ArchiveRestAPIClass.getEntranceGroups(getApplicationContext(), firstIndexOfTypes,
                    new Function2<JsonObject, HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //AlertClass.hideLoadingMessage(loadingProgress);

                                    RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());

                                    try {
                                        if (httpErrorType == HTTPErrorType.Success) {
                                            ArchiveActivity.this.retryCounter = 0;
                                            if (jsonObject != null) {

                                                String status = jsonObject.get("status").getAsString();
                                                switch (status) {
                                                    case "OK": {


                                                        HashMap<Integer, String> jsonHashMap = new HashMap<>();
                                                        JsonObject json = jsonObject.getAsJsonObject();
                                                        JsonArray leaders = json.getAsJsonArray("record");


                                                        ArrayList<JsonElement> localListJson = new ArrayList<JsonElement>();
                                                        ArrayList<String> localList = new ArrayList<String>();
                                                        for (JsonElement je : leaders) {
                                                            String j = je.getAsJsonObject().get("title").getAsString();
                                                            if (!j.equals("")) {
                                                                int i = je.getAsJsonObject().get("id").getAsInt();
                                                                jsonHashMap.put(i, j);
                                                                localListJson.add(je);
                                                                localList.add(j);
                                                            }
                                                        }

                                                        ArchiveActivity.this.adapterTab.clear();
                                                        ArchiveActivity.this.adapterTab.addAll(localList);
                                                        ArchiveActivity.this.adapterTab.notifyDataSetChanged();

                                                        ArchiveActivity.this.tabbarJsonElement.clear();
                                                        ArchiveActivity.this.tabbarJsonElement.addAll(localListJson);

//                                                        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)) {
//                                                            Collections.reverse(ArchiveActivity.this.tabbarJsonElement);
//                                                        }

                                                        ArchiveActivity.this.mSectionsPagerAdapter.notifyDataSetChanged();
                                                        tabLayout.setupWithViewPager(ArchiveActivity.this.mViewPager);

                                                        Handler h = new Handler();
                                                        h.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ArchiveActivity.this.tabLayout.getTabAt(0).select();
                                                            }
                                                        }, 200);

                                                        break;
                                                    }
                                                    case "Error": {
                                                        String errorType = jsonObject.get("error_type").getAsString();

                                                        switch (errorType) {
                                                            case "EmptyArray": {

                                                                ArchiveActivity.this.adapterSet.setItems(new ArrayList<JsonElement>());
                                                                ArchiveActivity.this.adapterSet.notifyDataSetChanged();
                                                                break;
                                                            }
                                                        }


                                                        ArchiveActivity.this.adapterTab.clear();
                                                        ArchiveActivity.this.adapterTab.notifyDataSetChanged();

                                                        ArchiveActivity.this.tabbarJsonElement.clear();
                                                        ArchiveActivity.this.mSectionsPagerAdapter.notifyDataSetChanged();
                                                        tabLayout.setupWithViewPager(ArchiveActivity.this.mViewPager);

                                                        break;
                                                    }
                                                }
                                            } //end null object

                                        } else if (httpErrorType == HTTPErrorType.Refresh) {
                                            new GetGroupsTask().execute(firstIndexOfTypes);
                                        } else {
                                            if (ArchiveActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                                ArchiveActivity.this.retryCounter += 1;
                                                new GetGroupsTask().execute(firstIndexOfTypes);
                                            } else{
                                                ArchiveActivity.this.retryCounter = 0;
                                                AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                            }
                                        }

                                    } catch (Exception e) {
                                        ArchiveActivity.this.tabLayout.getTabAt(0).select();
                                        Log.d(TAG, "run: " + e.getMessage());

                                    }
                                } //end run
                            });
                            return null;
                        }
                    }
                    , new Function1<NetworkErrorType, Unit>() {
                        @Override
                        public Unit invoke(final NetworkErrorType networkErrorType) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (ArchiveActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                        ArchiveActivity.this.retryCounter += 1;
                                        new GetGroupsTask().execute(firstIndexOfTypes);
                                    } else {
                                        ArchiveActivity.this.retryCounter = 0;
                                        RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());
                                        switch (networkErrorType) {
                                            case NoInternetAccess:
                                            case HostUnreachable: {
                                                AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                                break;
                                            }
                                            default: {
                                                AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

//            loadingProgress = AlertClass.showLoadingMessage(ArchiveActivity.this);
//            loadingProgress.show();
        }


    }

    private class GetSetsTask extends AsyncTask<Integer, Void, Void> {
        private Integer firstIndexOfGroups;

        @Override
        protected Void doInBackground(final Integer... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    if (!isFinishing()) {
//                        if (loadingProgress == null) {
//                            loadingProgress = AlertClass.showLoadingMessage(ArchiveActivity.this);
//                            loadingProgress.show();
//                        } else {
//                            if (!loadingProgress.isShowing()) {
//                                //loadingProgress = AlertClass.showLoadingMessage(HomeActivity.this);
//                                loadingProgress.show();
//                            }
//                        }
//                    }

                    ArchiveActivity.this.adapterSet.changeLoadingState(false);
                    ArchiveActivity.this.adapterSet.notifyDataSetChanged();
                }
            });

            if (params == null) {
                firstIndexOfGroups = ArchiveActivity.this.dropDownJsonElement.get(0).getAsJsonObject().get("id").getAsInt();
            } else {
                firstIndexOfGroups = params[0];
            }

            ArchiveRestAPIClass.getEntranceSets(getApplicationContext(), firstIndexOfGroups, new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

//                            AlertClass.hideLoadingMessage(loadingProgress);

                            RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());
                            try {
                                if (httpErrorType == HTTPErrorType.Success) {
                                    ArchiveActivity.this.retryCounter = 0;
                                    if (jsonObject != null) {
                                        String status = jsonObject.get("status").getAsString();

                                        switch (status) {
                                            case "OK": {
                                                HashMap<Integer, String> jsonHashMap = new HashMap<>();
                                                JsonObject json = jsonObject.getAsJsonObject();
                                                JsonArray leaders = json.getAsJsonArray("record");


                                                ArrayList<JsonElement> localListJson = new ArrayList<JsonElement>();
                                                ArrayList<String> localList = new ArrayList<>();
                                                for (JsonElement je : leaders) {
                                                    String j = je.getAsJsonObject().get("title").getAsString();
                                                    if (!j.equals("")) {
                                                        int i = je.getAsJsonObject().get("id").getAsInt();
                                                        jsonHashMap.put(i, j);
                                                        localListJson.add(je);
                                                        localList.add(j);
                                                    }
                                                }

                                                ArchiveActivity.this.adapterSet.setItems(localListJson);
                                                ArchiveActivity.this.adapterSet.notifyDataSetChanged();

                                                RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());
                                                break;
                                            }

                                            case "Error": {
                                                String errorType = jsonObject.get("error_type").getAsString();

                                                switch (errorType) {
                                                    case "EmptyArray": {

                                                        ArchiveActivity.this.adapterSet.setItems(new ArrayList<JsonElement>());
                                                        ArchiveActivity.this.adapterSet.notifyDataSetChanged();
                                                        break;
                                                    }
                                                }

                                                RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());

                                                break;
                                            }

                                        }
                                    }
                                } else if (httpErrorType == HTTPErrorType.Refresh) {
                                    new GetSetsTask().execute(params);
                                } else {
                                    if (ArchiveActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                        ArchiveActivity.this.retryCounter += 1;
                                        new GetSetsTask().execute(params);
                                    } else {
                                        ArchiveActivity.this.retryCounter = 0;

                                        ArchiveActivity.this.adapterSet.changeLoadingState(false);
                                        ArchiveActivity.this.adapterSet.notifyDataSetChanged();

                                        AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null);
                                    }
                                }

                            } catch (Exception exc) {
                                ArchiveActivity.this.adapterSet.setItems(new ArrayList<JsonElement>());
                                ArchiveActivity.this.adapterSet.notifyDataSetChanged();

                                RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());
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
                            if (ArchiveActivity.this.retryCounter < getCONNECTION_MAX_RETRY()) {
                                ArchiveActivity.this.retryCounter += 1;
                                new GetSetsTask().execute(params);
                            } else {
                                ArchiveActivity.this.retryCounter = 0;
//                                AlertClass.hideLoadingMessage(loadingProgress);

                                ArchiveActivity.this.adapterSet.changeLoadingState(false);
                                ArchiveActivity.this.adapterSet.notifyDataSetChanged();

                                RotateViewExtensions.buttonRotateStop(ArchiveActivity.this.refreshButton, getApplicationContext());
                                if (networkErrorType != null) {
                                    switch (networkErrorType) {
                                        case NoInternetAccess:
                                        case HostUnreachable: {
                                            AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "error", null);
                                            break;
                                        }
                                        default: {
                                            AlertClass.showTopMessage(ArchiveActivity.this, findViewById(R.id.container), "NetworkError", networkErrorType.name(), "", null);
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

        }

    }

    private enum SetsAdapterHolderType {
        ENTRANCE_SET(1), EMPTY_HOLDER(50), LOADING_HOLDER(51);
        private final int value;

        SetsAdapterHolderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    private class GetSetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private ArrayList<JsonElement> mArrayList = new ArrayList<>();
        private Boolean isLoaded = false;

        public GetSetsAdapter(Context context, ArrayList<JsonElement> arrayList) {
            this.context = context;
            this.mArrayList = arrayList;
        }

        public void setItems(ArrayList<JsonElement> arrayList) {
            this.mArrayList = arrayList;
            this.changeLoadingState(true);
        }

        public void changeLoadingState(boolean state) {
            this.isLoaded = state;
        }

        private class ItemHolder extends RecyclerView.ViewHolder {
            private ImageView entranceLogo;

            private TextView concourName;
            private TextView concourCode;
            private TextView concourCount;
            private View constraint;
            private ArchiveEsetStructs archiveEsetStructs;

            private JsonObject extraData;


            public ItemHolder(View itemView) {
                super(itemView);

                concourName = (TextView) itemView.findViewById(R.id.ccListitemArchiveL_concourName);
                concourCode = (TextView) itemView.findViewById(R.id.ccListitemArchiveL_concourCode);
                concourCount = (TextView) itemView.findViewById(R.id.ccListitemArchiveL_concourCount);
                entranceLogo = (ImageView) itemView.findViewById(R.id.settingUserInfoL_userImage);
                constraint = itemView.findViewById(R.id.container);

                concourName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                concourCode.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                concourCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }


            public void setupHolder(final JsonElement jsonElement) {
                entranceLogo.setImageResource(R.drawable.no_image);

                String t1 = jsonElement.getAsJsonObject().get("title").getAsString().trim();
                concourName.setText(t1);

                String concourCodeInt = FormatterSingleton.getInstance().getNumberFormatter().format(jsonElement.getAsJsonObject().get("code").getAsInt());
                String t2 = "";
                if (jsonElement.getAsJsonObject().get("code").getAsInt() == 0) {
                    t2 = "کد: " + "ندارد";
                } else {
                    t2 = "کد: " + concourCodeInt;
                }

                t2 = t2.trim();

                concourCode.setText(t2);

                String concourCountInt = FormatterSingleton.getInstance().getNumberFormatter().format(jsonElement.getAsJsonObject().get("entrance_count").getAsInt());
                String t3 = concourCountInt + " آزمون";
                concourCount.setText(t3);

                Date georgianDate = null;
                String persianDateString = "";
                String currentDateString = jsonElement.getAsJsonObject().get("updated").getAsString();
                try {
                    georgianDate = FormatterSingleton.getInstance().getUTCDateFormatter().parse(currentDateString);
                    persianDateString = FormatterSingleton.getInstance().getPersianDateString(georgianDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                ViewGroup.LayoutParams entranceLogoLP = entranceLogo.getLayoutParams();
                int imageId = jsonElement.getAsJsonObject().get("id").getAsInt();

                archiveEsetStructs = new ArchiveEsetStructs();
                archiveEsetStructs.updated = jsonElement.getAsJsonObject().get("updated").getAsString();
                archiveEsetStructs.code = jsonElement.getAsJsonObject().get("code").getAsString();
                archiveEsetStructs.entrance_count = jsonElement.getAsJsonObject().get("entrance_count").getAsInt();
                archiveEsetStructs.title = jsonElement.getAsJsonObject().get("title").getAsString();
                archiveEsetStructs.id = jsonElement.getAsJsonObject().get("id").getAsInt();


                entranceLogo.setLayoutParams(entranceLogoLP);

                constraint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArchiveActivity.this.mArchiveEsetDetailStruct.esetStruct = archiveEsetStructs;
                        ArchiveActivity.this.mArchiveEsetDetailStruct.groupTitle = ArchiveActivity.this.tabbarJsonElement.get(ArchiveActivity.this.tabLayout.getSelectedTabPosition()).getAsJsonObject().get("title").getAsString();

                        Integer dropdownPosition = ArchiveActivity.this.currentPositionDropDown;
                        ArchiveActivity.this.mArchiveEsetDetailStruct.typeTitle = ArchiveActivity.this.dropDownJsonElement.get(dropdownPosition).getAsJsonObject().get("title").getAsString();

                        if (jsonElement.getAsJsonObject().get("entrance_count").getAsInt() != 0) {
                            Intent i = ArchiveDetailActivity.newIntent(ArchiveActivity.this, ArchiveActivity.this.mArchiveEsetDetailStruct);
                            startActivity(i);
                        } else {
                            Toast.makeText(context, "تعداد آزمون موجود 0 عدد است", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                downloadImage(imageId);

            }

            private void downloadImage(final int imageId) {
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
                byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                if (data != null) {

                    Glide.with(ArchiveActivity.this)
                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(entranceLogo)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(ArchiveActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
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

                                Glide.with(ArchiveActivity.this)

                                        .load(data)
                                        .crossFade()
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

                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ArchiveActivity.this.currentGroupSelected != null && ArchiveActivity.this.currentGroupSelected > 0)
                            ArchiveActivity.this.getSets(ArchiveActivity.this.currentGroupSelected);
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
                        ContextCompat.getColor(ArchiveActivity.this, R.color.colorConcoughGray),
                        PorterDuff.Mode.SRC_IN);
            }

            public void setupHolder() {
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == SetsAdapterHolderType.EMPTY_HOLDER.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_recycle_not_item, parent, false);
                return new ItemEmptyHolder(view);
            } else if (viewType == SetsAdapterHolderType.LOADING_HOLDER.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_recycle_loading, parent, false);
                return new LoadingHolder(view);

            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_archive_listitem_details, parent, false);
                return new ItemHolder(view);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (this.isLoaded) {
                if (mArrayList.size() == 0) {
                    return SetsAdapterHolderType.EMPTY_HOLDER.getValue();
                } else {
                    return SetsAdapterHolderType.ENTRANCE_SET.getValue();
                }
            } else {
                return SetsAdapterHolderType.LOADING_HOLDER.getValue();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder.getClass() == ItemEmptyHolder.class) {
                ItemEmptyHolder itemEmptyHolder = (ItemEmptyHolder) holder;
                itemEmptyHolder.setupHolder();
            } else if (holder.getClass() == LoadingHolder.class) {
                LoadingHolder h = (LoadingHolder) holder;
                h.setupHolder();
            } else if (holder.getClass() == ItemHolder.class) {
                JsonElement oneItem = this.mArrayList.get(position);
                ItemHolder itemHolder = (ItemHolder) holder;
                itemHolder.setupHolder(oneItem);
            }
        }


        @Override
        public int getItemCount() {
            if (this.isLoaded) {
                if (mArrayList.size() == 0) {
                    return 1;
                } else {
                    return mArrayList.size();
                }
            } else {
                return 1;
            }
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new SectionFragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return ArchiveActivity.this.adapterTab.getCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return (CharSequence) ArchiveActivity.this.adapterTab.getItem(position);

        }
    }


    public class DialogAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<String> mArrayList;

        public DialogAdapter(Context mContext, ArrayList<String> mArrayList) {
            this.mContext = mContext;
            this.mArrayList = mArrayList;
        }

        @Override
        public int getCount() {
            return this.mArrayList.size();
        }

        @Override
        public Object getItem(int position) {

            return this.mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = LayoutInflater.from(ArchiveActivity.this.getApplicationContext()).inflate(R.layout.cc_archive_listitem_archive, null);
            TextView tv = (TextView) v.findViewById(R.id.text1);
            tv.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
            tv.setText(mArrayList.get(position).trim());

            return v;
        }

        public void addAll(ArrayList<String> arrayList) {
            this.mArrayList.addAll(arrayList);
        }

        public void setItems(ArrayList<String> arrayList) {
            this.mArrayList = arrayList;
        }
    }

}



