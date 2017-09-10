package com.concough.android.concough;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.concough.android.general.AlertClass;
import com.concough.android.models.EntranceBookletModel;
import com.concough.android.models.EntranceLessonModel;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.EntranceOpenedCountModelHandler;
import com.concough.android.models.EntranceQuestionModel;
import com.concough.android.models.EntranceQuestionModelHandler;
import com.concough.android.models.EntranceQuestionStarredModelHandler;
import com.concough.android.models.EntranceStarredQuestionModel;
import com.concough.android.models.UserLogModelHandler;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.LogTypeEnum;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.MD5Digester;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.cryptonode.jncryptor.AES256JNCryptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;
import static com.concough.android.concough.R.id.container;
import static com.concough.android.settings.ConstantsKt.getSECRET_KEY;

//import com.bumptech.glide.load.resource.drawable.GlideDrawable;


public class EntranceShowActivity extends AppCompatActivity implements Handler.Callback {

    private class StarredQuestionsContainer {
        public String lessionId;
        public String lessionTitle;
        public int count;
        public RealmList<EntranceQuestionModel> questions;


    }


    private final static String TAG = "EntranceShowActivity";


    private final static String ENTRANCE_UNIQUE_ID_KEY = "entranceUniqueId";
    private final static String SHOW_TYPE_KEY = "showType";
    private static final String HANDLE_THREAD_NAME = "Concough-EntranceShowActivity";
    private static final int LOAD_IMAGE = 0;
    private static final int LOAD_STARRED_IMAGE = 1;


    private HandlerThread handlerThread = null;
    private Handler handler = null;

    private HandlerThread starredHandlerThread = null;
    private Handler starredHandler = null;


    private String entranceUniqueId;
    private String showType = "Show";
    private String username = null;
    private HashMap<String, byte[]> imageRepo = new HashMap<>();
    //    private HashMap<String, Bitmap> imageRepo = new HashMap<>();
    private EntranceStruct entranceStruct;
    private String hashKey = null;

    private EntranceModel entranceDB;

    private RealmList<EntranceBookletModel> bookletsDB;
    private RealmList<EntranceLessonModel> lessonsDB;
    private RealmList<EntranceQuestionModel> questionsDB;
    private ArrayList<StarredQuestionsContainer> starredQuestions = new ArrayList<>();
    private int globalPairListInteger = 0;

    private Boolean showAllAnswers = false;
    //    private Boolean showStarredQuestions = false;
    private ArrayList<String> bookletList;
    private ArrayList<String> dialogInfoList;
    private ArrayList<String> starredIds = new ArrayList<>();
    private ArrayList<String> showedAnsweresIds = new ArrayList<>();
    private ArrayAdapter<String> lessonAdapter;
    private DialogAdapter bookletAdapter;
    private DialogAdapter dialogInfoAdapter;
    private EntranceShowAdapter entranceShowAdapter;
//    private StarredShowAdapter entranceShowStarredAdapter;


    private CustomTabLayout tabLayout;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private DialogPlus dialog;
    private Dialog dialogInfo;
    private Button texButton;
    private Button infoButton;
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewStar;
    private KProgressHUD loading;

    private ImageView esetImageView;

    private ArrayList<String> mList;
    private StarredShowAdapter starredAdapter;
    private HeaderItemDecoration itemDecoration;


    public static Intent newIntent(Context packageContext, String entranceUniqueId, String showType) {
        Intent i = new Intent(packageContext, EntranceShowActivity.class);
        i.putExtra(ENTRANCE_UNIQUE_ID_KEY, entranceUniqueId);
        i.putExtra(SHOW_TYPE_KEY, showType);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_show);


        this.handlerThread = new HandlerThread(HANDLE_THREAD_NAME);
        if (this.handlerThread != null) {
            this.handlerThread.start();
        }

        Looper looper = this.handlerThread.getLooper();
        if (looper != null) {
            this.handler = new Handler(looper, this);
        }


        this.starredHandlerThread = new HandlerThread(HANDLE_THREAD_NAME + "2");
        if (this.starredHandlerThread != null) {
            this.starredHandlerThread.start();
        }

        Looper starredLooper = this.starredHandlerThread.getLooper();
        if (starredLooper != null) {
            this.starredHandler = new Handler(starredLooper, this);
        }


        recyclerView = (RecyclerView) findViewById(R.id.entranceShowA_recycleEntranceShow);
        recyclerViewStar = (RecyclerView) findViewById(R.id.entranceShowA_recycleEntranceShowFav);

        recyclerViewStar.setVisibility(View.GONE);

        entranceShowAdapter = new EntranceShowAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(EntranceShowActivity.this));
        recyclerView.setAdapter(entranceShowAdapter);

        entranceUniqueId = getIntent().getStringExtra(ENTRANCE_UNIQUE_ID_KEY);
        showType = getIntent().getStringExtra(SHOW_TYPE_KEY);

        username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername(getApplicationContext());

        if (entranceUniqueId.equals("")) {
            finish();
        }

        lessonAdapter = new ArrayAdapter<String>(this, R.layout.cc_archive_listitem_tabbar);


        dialogInfoList = new ArrayList<>();
        bookletList = new ArrayList<>();


        bookletAdapter = new DialogAdapter(EntranceShowActivity.this, bookletList);
        dialogInfoAdapter = new DialogAdapter(EntranceShowActivity.this, dialogInfoList);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        //Tab
        tabLayout = (CustomTabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setTabGravity(Gravity.RIGHT);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                loadQuestions(index);
                loading = AlertClass.showLoadingMessage(EntranceShowActivity.this);
                loading.show();
                if (showType.equals("Show")) {
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            EntranceShowActivity.this.entranceShowAdapter.setItems(questionsDB);
                            EntranceShowActivity.this.entranceShowAdapter.notifyDataSetChanged();
                            AlertClass.hideLoadingMessage(loading);
                        }
                    }, 1000);
                    // TODO : show data on recycle view
                }

                Log.d(TAG, "onTabSelected: ");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        bookletAdapter.setItems(new ArrayList<String>());
        int toolBarHeight = toolbar.getLayoutParams().height;

        dialog = DialogPlus.newDialog(this)
                .setAdapter(bookletAdapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, final int position) {

                        dialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadLessions(position);
                                tabLayout.scrollTo(0, 1);
                            }
                        });

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogPlus dialog) {
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

        View mCustomView = LayoutInflater.from(this).inflate(R.layout.cc_entrance_actionbar, null);
        infoButton = (Button) mCustomView.findViewById(R.id.actionBarL_infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInfo.show();
            }
        });

        ImageButton backButton = (ImageButton) mCustomView.findViewById(R.id.actionBarL_backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        texButton = (Button) mCustomView
                .findViewById(R.id.actionBarL_dropDown);
        texButton.setText(buttonTextMaker("انتخاب کنید", false));


        if (mActionBar != null) {
            mActionBar.setCustomView(mCustomView);
            mActionBar.setDisplayShowCustomEnabled(true);
        }


        texButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (dialog.isShowing()) {
                    // texButton.setText(buttonTextMaker(typeList.get(currentPositionDropDown).toString(), false));


                    dialog.dismiss();


                } else {
                    //  texButton.setText(buttonTextMaker(typeList.get(currentPositionDropDown).toString(), true));

                    dialog.show();

                }

            }

        });

        texButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


        starredAdapter = new StarredShowAdapter(EntranceShowActivity.this);
        itemDecoration = new HeaderItemDecoration(recyclerView, starredAdapter);

        loadEntranceDB();
        loadBooklets();
        loadStarredQuestion();
        loadStarredQuestionRecords();

        infoDialog();

        EntranceOpenedCountModelHandler.update(getApplicationContext(), username, entranceUniqueId, showType);

        JsonObject eData = new JsonObject();
        eData.addProperty("uniqueId", entranceUniqueId);
        if (showType.equals("Show")) {
            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceShowNormal.getTitle(), eData);
        } else if (showType.equals("Starred")) {
            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceShowStarred.getTitle(), eData);
        }


        if (showType.equals("Starred")) {
            tabLayout.setVisibility(View.GONE);

            recyclerViewStar.addItemDecoration(itemDecoration);
            recyclerViewStar.setAdapter(starredAdapter);
            recyclerViewStar.setLayoutManager(new StaggeredGridLayoutManager(1, 1));

            texButton.setText("سوالات نشان شده"); // TODO: add count of starred images
        }


        globalPairListInteger = starredIds.size();


    }


    private void infoDialog() {
        dialogInfo = new Dialog(EntranceShowActivity.this);
        dialogInfo = new Dialog(EntranceShowActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
        dialogInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogInfo.setCancelable(true);
        dialogInfo.setContentView(R.layout.cc_alert_dialog_entrance_info_icon);
        dialogInfo.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        TextView tvEntranceTypeName = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_typeName);
        TextView tvEntranceGroupName = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_groupName);
        TextView tvEntranceExtraData = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_extraData);
        final Switch entranceSwitch = (Switch) dialogInfo.findViewById(R.id.entranceInfoIcon_switch);
        final TextView buttonStarred = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_btnStarred);
        final TextView tvStarredCount = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_starredCount);


        tvEntranceTypeName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        tvEntranceGroupName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        tvEntranceExtraData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        entranceSwitch.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        buttonStarred.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        tvStarredCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());


        String entranceYear = FormatterSingleton.getInstance().getNumberFormatter().format(this.entranceDB.year);
        tvEntranceTypeName.setText("کنکور " + this.entranceDB.type + " " + this.entranceDB.organization + " " + entranceYear);
        tvEntranceGroupName.setText(this.entranceDB.group + " (" + this.entranceDB.set + ")");


        String extra = "";
        ArrayList<String> extraArray = new ArrayList<>();
        JsonObject extraData = new JsonParser().parse(this.entranceDB.extraData).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
            extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
        }

        extra = TextUtils.join(" - ", extraArray);
        tvEntranceExtraData.setText(extra);


        entranceSwitch.setHighlightColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGreen));

        dialogInfo.setCanceledOnTouchOutside(true);
        dialogInfo.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                tvStarredCount.setText(String.format("%s", FormatterSingleton.getInstance().getNumberFormatter().format(EntranceShowActivity.this.globalPairListInteger)));

            }
        });
        dialogInfo.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (entranceSwitch.isChecked() != showAllAnswers) {
                    showAllAnswers = entranceSwitch.isChecked();
                    entranceShowAdapter.notifyDataSetChanged();
                    starredAdapter.notifyDataSetChanged();
                }

//
//                if (tvStarredCount.getVisibility() == View.VISIBLE) {
//                    showStarredQuestions = false;
//                    tabLayout.setVisibility(View.VISIBLE);
//                } else {
//                    showStarredQuestions = true;
//                    tabLayout.setVisibility(View.INVISIBLE);
//                }


            }


        });

        if (showType.equals("Show")) {
            buttonStarred.setText("سوالات نشان شده");
            tvStarredCount.setVisibility(View.VISIBLE);
        } else {
            buttonStarred.setText("کلیه سوالات");
            tvStarredCount.setVisibility(View.INVISIBLE);

        }

        if (showAllAnswers) {
            entranceSwitch.setEnabled(false);
        } else {
            entranceSwitch.setEnabled(true);
        }


        buttonStarred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvStarredCount.getVisibility() == View.VISIBLE) { // if must show all questions
                    showType = "Starred";

                    buttonStarred.setText("کلیه سوالات");
                    tvStarredCount.setVisibility(View.INVISIBLE);
                    tabLayout.setVisibility(View.GONE);

                    recyclerView.setVisibility(View.GONE);
                    recyclerViewStar.setVisibility(View.VISIBLE);

                    texButton.setEnabled(false);
                    texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));


                    recyclerViewStar.addItemDecoration(itemDecoration);
                    recyclerViewStar.setAdapter(starredAdapter);
                    starredAdapter.setItems(starredQuestions);
                    starredAdapter.notifyDataSetChanged();
                    recyclerViewStar.setLayoutManager(new StaggeredGridLayoutManager(1, 1));

                    texButton.setText(String.format("سوالات نشان شده (%s)", FormatterSingleton.getInstance().getNumberFormatter().format(EntranceShowActivity.this.globalPairListInteger)));


                    dialogInfo.dismiss();

                } else {
                    buttonStarred.setText("سوالات نشان شده");
                    tvStarredCount.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);


                    texButton.setVisibility(View.VISIBLE);
                    texButton.setEnabled(true);
                    texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));

                    showType = "Show";


                    recyclerViewStar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    recyclerView.setLayoutManager(new LinearLayoutManager(EntranceShowActivity.this));


//                    View decorView = getWindow().getDecorView();
//                    recyclerViewStar.measure(0,0);
//                    recyclerView.measure(0,0);
//                    View vg = itemDecoration.getHeaderViewForItem(0,recyclerViewStar);
//                    vg.setVisibility(View.GONE);
//                    View vg2 = itemDecoration.getHeaderViewForItem(0,recyclerView);
//                    vg2.setVisibility(View.GONE);

//
//                    final ViewGroup viewGroup =  (ViewGroup)((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
//                    for(int i=0; i<viewGroup.getChildCount(); i++) {
//                        View v4 = viewGroup.getChildAt(i);
//                        Log.d(TAG, "onClick: ");
//
//                    }

//                    final ViewGroup viewGroup =  ((ViewGroup) findViewById(android.R.id.content));
//                    viewGroup.clearDisappearingChildren();

//                    viewGroup.setVisibility(View.GONE);

//
//                    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;


//                   LinearLayout ln = (LinearLayout) getWindow().getDecorView().findViewById(R.id.headerShowStarred_linear);
//                    ln.setVisibility(View.GONE);
//                    getAllChildren();
//                    previousWidthMeasureSpec = 1073742544
                    //recyclerView.removeItemDecoration(itemDecoration);
                    //recyclerView.invalidateItemDecorations();
//                    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, 1));
                    //recyclerView.invalidate();


                    //recyclerView.setAdapter(entranceShowAdapter);

                    //entranceShowAdapter.notifyDataSetChanged();


                    dialogInfo.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadLessions(0);
                            tabLayout.scrollTo(0, 1);
                        }
                    });

                    dialogInfo.dismiss();


                }
            }
        });

        esetImageView = (ImageView) dialogInfo.findViewById(R.id.entranceInfoIcon_entLogo);

        downloadImage(this.entranceDB.setId);

    }

    private void downloadImage(final int imageId) {
        MediaRestAPIClass.downloadEsetImage(EntranceShowActivity.this, imageId, esetImageView, new Function2<JsonObject, HTTPErrorType, Unit>() {
            @Override
            public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (httpErrorType != HTTPErrorType.Success) {
                            Log.d(TAG, "run: ");
                            if (httpErrorType == HTTPErrorType.Refresh) {
                                downloadImage(imageId);
                            } else {
                                esetImageView.setImageResource(R.drawable.no_image);
                            }
                        }
                    }
                });
                Log.d(TAG, "invoke: " + jsonObject);
                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(final NetworkErrorType networkErrorType) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (networkErrorType) {
                            case NoInternetAccess:
                            case HostUnreachable: {
                                AlertClass.showTopMessage(EntranceShowActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "error", null);
                                break;
                            }
                            default: {
                                AlertClass.showTopMessage(EntranceShowActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "", null);
                                break;
                            }

                        }
                    }
                });

                return null;
            }
        });

    }


    private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }


    private void createLog(String logType, JsonObject extraData) {
        if (username != null) {
            String uniqueId = UUID.randomUUID().toString();
            Date created = new Date();
            // TODO : change all Dates to UTC

            try {
                UserLogModelHandler.add(getApplicationContext(), username, uniqueId, created, logType, extraData);
            } catch (Exception exc) {
            }
        }
    }

    private void loadEntranceDB() {
        EntranceModel item = EntranceModelHandler.getByUsernameAndId(getApplicationContext(), username, entranceUniqueId);
        if (item != null) {
            entranceDB = item;
        } else {
            finish();
        }
    }


    private void loadBooklets() {
        bookletsDB = entranceDB.booklets;
        final ArrayList<String> items = new ArrayList<>();

        for (EntranceBookletModel item : bookletsDB) {
            items.add(item.title);
        }

        if (items.size() > 0) {

//
//            final Handler handler25 = new Handler();
//            handler25.postDelayed(new Runnable() {
//                @Override
//                public void run() {
            bookletAdapter.setItems(items);
            bookletAdapter.notifyDataSetChanged();
//
//                }
//            }, 500);


            loadLessions(0);
        }
    }

    private void loadLessions(int index) {
        if (index >= 0) {
            lessonsDB = new RealmList<EntranceLessonModel>();
            RealmResults<EntranceLessonModel> lessionModel = bookletsDB.get(index).lessons.sort("order", Sort.ASCENDING);
            lessonsDB.addAll(lessionModel.subList(0, lessionModel.size()));

            lessonAdapter.clear();

            for (EntranceLessonModel item : lessonsDB) {
                lessonAdapter.add(item.title);
            }

            lessonAdapter.notifyDataSetChanged();


            EntranceShowActivity.this.mSectionsPagerAdapter.notifyDataSetChanged();
            tabLayout.setupWithViewPager(EntranceShowActivity.this.mViewPager);

            //loadQuestions(0);
//            if (showType.equals("Show")) {
//                EntranceShowActivity.this.entranceShowAdapter.setItems(questionsDB);
//                EntranceShowActivity.this.entranceShowAdapter.notifyDataSetChanged();
//                // TODO : show data on recycle view
//            }


            texButton.setText(buttonTextMaker(bookletAdapter.getItem(index).toString(), false));
            EntranceShowActivity.this.tabLayout.getTabAt(0).select();


        }
    }

    private void loadQuestions(int index) {
        if (index >= 0) {
            questionsDB = new RealmList<EntranceQuestionModel>();
            RealmResults<EntranceQuestionModel> questionModel = lessonsDB.get(index).questions.sort("number", Sort.ASCENDING);
            questionsDB.addAll(questionModel.subList(0, questionModel.size()));
        }
    }


    private void loadStarredQuestion() {
        RealmResults<EntranceStarredQuestionModel> items = EntranceQuestionStarredModelHandler.getStarredQuestions(getApplicationContext(), username, entranceUniqueId);

        for (EntranceStarredQuestionModel item : items) {
            starredIds.add(item.question.uniqueId);
        }
    }

    private void loadStarredQuestionRecords() {
        starredQuestions.clear();
        String[] temp = new String[starredIds.size()];


        if (starredIds.size() > 0) {
            RealmResults<EntranceQuestionModel> items = EntranceQuestionModelHandler.getStarredQuestions(getApplicationContext(), username, entranceUniqueId, starredIds.toArray(temp));
            if (items != null) {


                for (EntranceQuestionModel item : items) {
                    int index = -1;
                    for (int i = 0; i < starredQuestions.size(); i++) {
                        String itemUniqId = item.lesson.first().uniqueId;
                        if (starredQuestions.get(i).lessionId.equals(itemUniqId)) {  //item.lesson.first().uniqueId
                            index = i;
                            break;
                        }
                    }

                    if (index == -1) {
                        StarredQuestionsContainer container = new StarredQuestionsContainer();
                        container.lessionId = item.lesson.first().uniqueId;
                        container.lessionTitle = item.lesson.first().title;
                        container.count = 1;
                        container.questions = new RealmList<>();
                        container.questions.add(item);
                        starredQuestions.add(container);
                    } else {
                        StarredQuestionsContainer container = starredQuestions.get(index);
                        container.questions.add(item);
                        container.count = container.count + 1;
                        starredQuestions.set(index, container);
                    }

                }

            }
        }

        if (showType.equals("Starred")) {
            starredAdapter.setItems(starredQuestions);
            starredAdapter.notifyDataSetChanged();
        }


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

    @Override
    public boolean handleMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case LOAD_IMAGE:
                    handleLoadImage(msg);
                    break;
                case LOAD_STARRED_IMAGE:
                    handleLoadStarredImage(msg);
                    break;
            }
        }
        return true;
    }

    private void handleLoadImage(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle == null)
            return;
        final String imageStr = bundle.getString("IMAGES_STRING");
        entranceShowAdapter.loadImages(imageStr);
    }


    private void handleLoadStarredImage(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle == null)
            return;
        final String imageStr = bundle.getString("IMAGES_STRING");
        starredAdapter.loadImages(imageStr);
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
            return EntranceShowActivity.this.lessonAdapter.getCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return (CharSequence) EntranceShowActivity.this.lessonAdapter.getItem(position);

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

            View v = LayoutInflater.from(EntranceShowActivity.this.getApplicationContext()).inflate(R.layout.cc_archive_listitem_archive, null);
            TextView tv = (TextView) v.findViewById(R.id.text1);
            tv.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
            tv.setText(mArrayList.get(position));

            return v;
        }

        public void addAll(ArrayList<String> arrayList) {
            this.mArrayList.addAll(arrayList);
        }

        public void setItems(ArrayList<String> arrayList) {
            this.mArrayList = arrayList;
        }


    }


    public class DialogInfoAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<String> mArrayList;

        public DialogInfoAdapter(Context mContext, ArrayList<String> mArrayList) {
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

            View v = LayoutInflater.from(EntranceShowActivity.this.getApplicationContext()).inflate(R.layout.cc_archive_listitem_archive, null);
            TextView tv = (TextView) v.findViewById(R.id.text1);
            tv.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
            tv.setText(mArrayList.get(position));

            return v;
        }

        public void addAll(ArrayList<String> arrayList) {
            this.mArrayList.addAll(arrayList);
        }

        public void setItems(ArrayList<String> arrayList) {
            this.mArrayList = arrayList;
        }


    }


    private class EntranceShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private Context context;
        private RealmList<EntranceQuestionModel> questionModelList;

//        private  String hashKey;

        public EntranceShowAdapter(Context context) {
            this.context = context;
            this.questionModelList = new RealmList<>();


        }

        public void setItems(RealmList<EntranceQuestionModel> questionModelList) {
            this.questionModelList = questionModelList;
            int i = 0;
            int j = 0;
            for (EntranceQuestionModel item : questionModelList) {


                if (i > 5) {
                    //  int temp = j % 3;
                    if (EntranceShowActivity.this.handler != null) {
                        Message msg = EntranceShowActivity.this.handler.obtainMessage(LOAD_IMAGE);
                        msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));

                        Bundle bundle = new Bundle();
                        bundle.putString("IMAGES_STRING", item.images);
                        msg.setData(bundle);

                        EntranceShowActivity.this.handler.sendMessage(msg);

                    }
//
//                    switch (temp) {
//                        case 0:
//                            if (EntranceShowActivity.this.handler != null) {
//                                Message msg = EntranceShowActivity.this.handler.obtainMessage(LOAD_IMAGE);
//                                msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("IMAGES_STRING", item.images);
//                                msg.setData(bundle);
//
//                                EntranceShowActivity.this.handler.sendMessage(msg);
//
//                            }
//                            break;
//
//
//                        case 1:
//                            if (EntranceShowActivity.this.starredHandler != null) {
//                                Message msg = EntranceShowActivity.this.starredHandler.obtainMessage(LOAD_IMAGE);
//                                msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("IMAGES_STRING", item.images);
//                                msg.setData(bundle);
//
//                                EntranceShowActivity.this.starredHandler.sendMessage(msg);
//
//                            }
//                            break;
//
//
//                        case 2:
//                            if (EntranceShowActivity.this.handler3 != null) {
//                                Message msg = EntranceShowActivity.this.handler3.obtainMessage(LOAD_IMAGE);
//                                msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("IMAGES_STRING", item.images);
//                                msg.setData(bundle);
//
//                                EntranceShowActivity.this.handler3.sendMessage(msg);
//
//                            }
//                            break;
//                    }
//                    j++;

                } else {
                    loadImages(item.images);
                }
                i++;

            }

            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    // Call smooth scroll
                    recyclerView.smoothScrollToPosition(0);
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.cc_entrance_show_holder1, parent, false);
            return new EntranceShowHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final EntranceQuestionModel oneItem = this.questionModelList.get(position);
            ((EntranceShowHolder) holder).setupHolder(oneItem);
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);

//            ((EntranceShowHolder) holder).setImages();
//            (ImageView) img1 = (ImageView) holder.itemView.getRootView().findViewById(R.id.ccEntranceShowHolder1I_img1);
        }

        @Override
        public int getItemCount() {
            return questionModelList.size();
        }

        public byte[] convertStreamToByteArray(InputStream is) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[4096];
            int i = Integer.MAX_VALUE;
            while ((i = is.read(buff, 0, buff.length)) > 0) {
                baos.write(buff, 0, i);
            }

            return baos.toByteArray(); // be sure to close InputStream in calling function
        }

        public void loadImages(String imageString) {
            ArrayList<JsonObject> jsonObjects = new ArrayList<>();
            JsonArray jsonArray = new JsonParser().parse(imageString).getAsJsonArray();

            for (JsonElement item : jsonArray) {
                jsonObjects.add(item.getAsJsonObject());
            }

            Collections.sort(jsonObjects, new SortedList.Callback<JsonObject>() {
                @Override
                public int compare(JsonObject o1, JsonObject o2) {

                    if (o1.get("order").getAsInt() > o2.get("order").getAsInt()) {
                        return 1;
                    } else if (o1.get("order").getAsInt() < o2.get("order").getAsInt()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }

                @Override
                public void onChanged(int position, int count) {

                }

                @Override
                public boolean areContentsTheSame(JsonObject oldItem, JsonObject newItem) {
                    return false;
                }

                @Override
                public boolean areItemsTheSame(JsonObject item1, JsonObject item2) {
                    return false;
                }

                @Override
                public void onInserted(int position, int count) {

                }

                @Override
                public void onRemoved(int position, int count) {

                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {

                }
            });

            String hashStr = username + ":" + getSECRET_KEY();
            String hashKey = MD5Digester.digest(hashStr);

            for (JsonElement item : jsonObjects) {
                String imageId = item.getAsJsonObject().get("unique_key").getAsString();
                if (!imageRepo.containsKey(imageId)) {
                    String filePath = entranceUniqueId + "/" + imageId;

                    File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
                    if (file.exists()) {
                        try {
                            byte[] buffer = new byte[(int) file.length()];
                            FileInputStream input = new FileInputStream(file);


                            input.read(buffer);

                            byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
                            byte[] i = new AES256JNCryptor(1000).decryptData(decoded, hashKey.toCharArray());

//                            ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
//                            AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());
//
//                            byte[] i = convertStreamToByteArray(cryptStream);


//                            RNCryptorNative rncryptor = new RNCryptorNative();
//
//                            String t = Base64.encodeToString(buffer, Base64.NO_WRAP);
//                            String data = rncryptor.decrypt(t, hashKey);
//                            byte[] i = data.getBytes();


                            //Bitmap bitmap = BitmapFactory.decodeByteArray(i, 0, i.length);

                            imageRepo.put(imageId, i);

                            i = null;
//                            data = null;
//                            rncryptor = null;
                            buffer = null;

//                            imageRepo.put(imageId, i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }


        private class EntranceShowHolder extends RecyclerView.ViewHolder {
            private TextView questionNumber;
            private ImageView starImage;
            private ImageView imgPreLoad;
            private ConstraintLayout mainConstraint;
            private ImageView img1;
            private ImageView img2;
            private ImageView img3;
            private LinearLayout linearShowAnswer;
            private TextView answerLabel;
            private ImageView answerLabelCheckbox;

            private TextView answer;
            private Boolean starred = false;
            private EntranceQuestionModel mEntranceQuestionModel;
            private Integer mWidth;
            private Integer mHeight;


            public EntranceShowHolder(View itemView) {

                super(itemView);

                questionNumber = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_questionNumber);
                starImage = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_star);

                imgPreLoad = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_imgPreLoad);
                img1 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img1);
                img2 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img2);
                img3 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img3);


                mainConstraint = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_mainConstrant);

//                img1.setImageDrawable(null);
//                img2.setImageDrawable(null);
//                img3.setImageDrawable(null);

//                img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                img1.setAdjustViewBounds(true);

                linearShowAnswer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowAnswer);
                answerLabel = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowAnswer);
                answerLabelCheckbox = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_checkBox);
                answer = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer);


                questionNumber.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answerLabel.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }


            public void setImages() {


//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                insertImage(mEntranceQuestionModel.images);
//                    }
//                }, 100);
            }


            public void setupHolder(final EntranceQuestionModel entranceQuestionModel) {
                answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
                answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
                answer.setVisibility(View.INVISIBLE);
//                imgPreLoad.setVisibility(View.VISIBLE);

                questionNumber.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.number));
                answer.setText("گزینه " + FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.answer) + " صحیح است");

                mEntranceQuestionModel = entranceQuestionModel;

                if (EntranceShowActivity.this.starredIds.contains(entranceQuestionModel.uniqueId)) {
                    starred = true;
                } else {
                    starred = false;
                }

                changeStarredState(starred);


                linearShowAnswer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        answer.setVisibility(View.VISIBLE);
                        if (!EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
                            EntranceShowActivity.this.showedAnsweresIds.add(entranceQuestionModel.uniqueId);
//                            mainConstraint.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable)));
//                            img1.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));
//                            img2.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));
//                            img3.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));

//                            questionNumber.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.concough_border_round_outline_blue_disabled_style));
//                            questionNumber.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGray4));

                            answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                            answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                        }
                    }
                });

                starImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        starred = !starred;
                        changeStarredState(starred);
                        addStarQuestionId(entranceQuestionModel.uniqueId, entranceQuestionModel.number, starred);
                        EntranceShowActivity.this.loadStarredQuestionRecords();
                        globalPairListInteger = starredIds.size();
                    }
                });

                if (EntranceShowActivity.this.showAllAnswers) {
                    answer.setVisibility(View.VISIBLE);
                    answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                    answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                } else {

                    if (EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
                        answer.setVisibility(View.VISIBLE);
                        answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                        answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                    }
                }

                setImages();
            }

            public void changeStarredState(Boolean state) {
                if (state) {
                    starImage.setImageResource(R.drawable.star_filled);
                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughRedLight));
                } else {
                    starImage.setImageResource(R.drawable.star_empty);
                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray2));
                }
            }


            public Boolean addStarQuestionId(String questionId, Integer questionNumber, Boolean state) {
                Boolean flag = false;

                if (state) {
                    if (!EntranceShowActivity.this.starredIds.contains(questionId)) {
                        if (EntranceQuestionStarredModelHandler.add(getApplicationContext(), username, entranceUniqueId, questionId)) {
                            EntranceShowActivity.this.starredIds.add(questionId);
                            flag = true;

                            JsonObject eData = new JsonObject();
                            eData.addProperty("uniqueId", entranceUniqueId);
                            eData.addProperty("questionNo", questionNumber);
                            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceQuestionStar.getTitle(), eData);

                        }
                    }
                } else {
                    if (EntranceShowActivity.this.starredIds.contains(questionId)) {
                        if (EntranceQuestionStarredModelHandler.remove(getApplicationContext(), username, entranceUniqueId, questionId)) {
                            EntranceShowActivity.this.starredIds.remove(questionId);
                            flag = true;

                            JsonObject eData = new JsonObject();
                            eData.addProperty("uniqueId", entranceUniqueId);
                            eData.addProperty("questionNo", questionNumber);
                            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceQuestionUnStar.getTitle(), eData);

                        }
                    }
                }

                return flag;
            }

            public void insertImage(String imageString) {
                img1.setImageDrawable(null);
                img2.setImageDrawable(null);
                img3.setImageDrawable(null);

                imgPreLoad.setVisibility(View.VISIBLE);

                ArrayList<JsonObject> jsonObjects = new ArrayList<>();
                JsonArray jsonArray = new JsonParser().parse(imageString).getAsJsonArray();

                if (hashKey == null) {
                    String hashStr = username + ":" + getSECRET_KEY();
                    hashKey = MD5Digester.digest(hashStr);
                }

                for (JsonElement item : jsonArray) {
                    jsonObjects.add(item.getAsJsonObject());
                }

                Collections.sort(jsonObjects, new SortedList.Callback<JsonObject>() {
                    @Override
                    public int compare(JsonObject o1, JsonObject o2) {

                        if (o1.get("order").getAsInt() > o2.get("order").getAsInt()) {
                            return 1;
                        } else if (o1.get("order").getAsInt() < o2.get("order").getAsInt()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }

                    @Override
                    public void onChanged(int position, int count) {

                    }

                    @Override
                    public boolean areContentsTheSame(JsonObject oldItem, JsonObject newItem) {
                        return false;
                    }

                    @Override
                    public boolean areItemsTheSame(JsonObject item1, JsonObject item2) {
                        return false;
                    }

                    @Override
                    public void onInserted(int position, int count) {

                    }

                    @Override
                    public void onRemoved(int position, int count) {

                    }

                    @Override
                    public void onMoved(int fromPosition, int toPosition) {

                    }
                });

                final ArrayList<byte[]> localBitmaps = new ArrayList<>();
                for (JsonElement item : jsonObjects) {
                    String imageId = item.getAsJsonObject().get("unique_key").getAsString();
//                    if (!imageRepo.containsKey(imageId)) {
//                        String filePath = entranceUniqueId + "/" + imageId;
//
//                        File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
//                        if (file.exists()) {
//                            try {
//                                byte[] buffer = new byte[(int) file.length()];
//                                FileInputStream input = new FileInputStream(file);
//
//
//                                input.read(buffer);
//
//                                byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
//                                byte[] i = new AES256JNCryptor(1000).decryptData(decoded, hashKey.toCharArray());
////
////                                ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
////                                AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());
////
////                                byte[] i = convertStreamToByteArray(cryptStream);
//
////                                RNCryptorNative rncryptor = new RNCryptorNative();
////                                String data = rncryptor.decrypt(new String(buffer), hashKey);
////                                byte[] i = data.getBytes();
//
//                                localBitmaps.add(i);
//                                imageRepo.put(imageId, i);
//
//                                i = null;
////                                data = null;
////                                rncryptor = null;
//                                buffer = null;
//                                //decoded = null;
//                                input.close();
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    } else {
//                    }
                    localBitmaps.add(imageRepo.get(imageId));
                }

                //img1.setVisibility(View.INVISIBLE);
                //img2.setVisibility(View.INVISIBLE);
                //img3.setVisibility(View.INVISIBLE);

                if (localBitmaps.size() >= 1) {
                    try {
                        final byte[] local1 = localBitmaps.get(0);
                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);


                        Glide.with(EntranceShowActivity.this)

                                .load(local1)

//                                .placeholder(R.drawable.no_image)
                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img1.setVisibility(View.VISIBLE);
                                        img1.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                //.error(R.drawable.ic_thumb_placeholder)
                                // .transform(new CircleTransform(this))
                                //.override(mWidth,mHeight)
                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()
                                .into(img1);


                        //img1.setImageBitmap(bitmap);
                        // img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        // img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img1.setVisibility(View.VISIBLE);
                        img1.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (localBitmaps.size() >= 2) {
                    try {

                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);

                        Glide.with(EntranceShowActivity.this)

                                .load(localBitmaps.get(1))

//                                .placeholder(R.drawable.no_image)
                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
//                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img2.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img2.setVisibility(View.VISIBLE);
                                        img2.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                //.error(R.drawable.ic_thumb_placeholder)
                                // .transform(new CircleTransform(this))
                                //.override(mWidth,mHeight)
                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()

                                .into(img2);


                        //img1.setImageBitmap(bitmap);
//                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        img2.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img2.setVisibility(View.VISIBLE);
                        img2.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (localBitmaps.size() >= 3) {
                    try {

                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);

                        Glide.with(EntranceShowActivity.this)

                                .load(localBitmaps.get(2))

//                                .placeholder(R.drawable.no_image)
                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
//                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img3.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img3.setVisibility(View.VISIBLE);
                                        img3.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                //.error(R.drawable.ic_thumb_placeholder)
                                // .transform(new CircleTransform(this))
                                //.override(mWidth,mHeight)
                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()
                                .into(img3);


                        //img1.setImageBitmap(bitmap);
//                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        img3.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img3.setVisibility(View.VISIBLE);
                        img3.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                localBitmaps.clear();


            }


        }

    }


    // Starred Adapter
    public class StarredShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyHeaderInterface {


        private class Lessions {
            public String lessionId;
            public String lessionTitle;
            public Integer count;

        }

        private Context context;
        private ArrayList<Pair<String, Object>> mPairList = new ArrayList<>();
        private ArrayList<Pair<String, Integer>> mPairListInteger = new ArrayList<>();

        public StarredShowAdapter(Context context) {
            this.context = context;
        }


        public void setItems(ArrayList<StarredQuestionsContainer> containerList) {
            mPairList.clear();
            mPairListInteger.clear();

            int i = 0;
            for (StarredQuestionsContainer item : containerList) {
                Lessions lesson = new Lessions();
                lesson.lessionId = item.lessionId;
                lesson.lessionTitle = item.lessionTitle;
                lesson.count = item.count;

                mPairList.add(new Pair<String, Object>("h", lesson));

                for (EntranceQuestionModel item2 : item.questions) {
                    mPairList.add(new Pair<String, Object>("q", item2));
                    mPairListInteger.add(new Pair<>(item2.uniqueId, i));
                }


                i++;
            }

            EntranceShowActivity.this.texButton.setText(String.format("سوالات نشان شده (%s)", FormatterSingleton.getInstance().getNumberFormatter().format(mPairListInteger.size())));


            i = 0;

            for (Pair<String, Object> item : mPairList) {
                if (item.first.equals("q")) {
                    EntranceQuestionModel itemQuestion = (EntranceQuestionModel) item.second;
                    if (i > 5) {

                        if (EntranceShowActivity.this.starredHandler != null) {
                            Message msg = EntranceShowActivity.this.starredHandler.obtainMessage(LOAD_STARRED_IMAGE);
                            msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));

                            Bundle bundle = new Bundle();
                            bundle.putString("IMAGES_STRING", itemQuestion.images);
                            msg.setData(bundle);

                            EntranceShowActivity.this.starredHandler.sendMessage(msg);

                        }

                    } else {
                        loadImages(itemQuestion.images);
                    }
                    i++;

                }

            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_sticky_header_entrance_show_starred, parent, false);
                return new HeaderHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_entrance_show_holder1, parent, false);
                return new ListHolder(view);
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (mPairList.get(position).first.equals("h")) {
                HeaderHolder itemHolder = (HeaderHolder) holder;
                itemHolder.setupHolder(mPairList.get(position).second);
            } else {
                ListHolder itemHolder = (ListHolder) holder;
                itemHolder.setupHolder(mPairList.get(position).second);
            }

        }

        @Override
        public int getItemCount() {
            return this.mPairList.size();
        }


        public class HeaderHolder extends RecyclerView.ViewHolder {
            private TextView tv;
            private TextView tvCount;

            public HeaderHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.headerShowStarred_headerTitle);
                tvCount = (TextView) itemView.findViewById(R.id.headerShowStarred_countEntrance);
                tv.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                tvCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

//                itemView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.colorAccent));

            }


            public void setupHolder(Object item) {
                Lessions castItem = (Lessions) item;
                tv.setText(castItem.lessionTitle);
                tvCount.setText(String.format("%s", FormatterSingleton.getInstance().getNumberFormatter().format(castItem.count)));
            }


        }


        public class ListHolder extends RecyclerView.ViewHolder {
            private TextView questionNumber;
            private ImageView starImage;
            private ImageView imgPreLoad;
            private ConstraintLayout mainConstraint;
            private ImageView img1;
            private ImageView img2;
            private ImageView img3;
            private LinearLayout linearShowAnswer;
            private TextView answerLabel;
            private ImageView answerLabelCheckbox;

            private TextView answer;
            private Boolean starred = false;
            private EntranceQuestionModel mEntranceQuestionModel;
            private Integer mWidth;
            private Integer mHeight;

            public ListHolder(View itemView) {
                super(itemView);


                questionNumber = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_questionNumber);
                starImage = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_star);

                imgPreLoad = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_imgPreLoad);
                img1 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img1);
                img2 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img2);
                img3 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img3);


                mainConstraint = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_mainConstrant);

//                img1.setImageDrawable(null);
//                img2.setImageDrawable(null);
//                img3.setImageDrawable(null);

//                img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                img1.setAdjustViewBounds(true);

                linearShowAnswer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowAnswer);
                answerLabel = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowAnswer);
                answerLabelCheckbox = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_checkBox);
                answer = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer);


                questionNumber.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answerLabel.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }


            public void setupHolder(Object item) {
                final EntranceQuestionModel entranceQuestionModel = (EntranceQuestionModel) item;
                this.mEntranceQuestionModel = entranceQuestionModel;
                answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
                answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
                answer.setVisibility(View.INVISIBLE);
//                imgPreLoad.setVisibility(View.VISIBLE);

                questionNumber.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.number));
                answer.setText("گزینه " + FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.answer) + " صحیح است");

                mEntranceQuestionModel = entranceQuestionModel;

                if (EntranceShowActivity.this.starredIds.contains(entranceQuestionModel.uniqueId)) {
                    starred = true;
                } else {
                    starred = false;
                }

                changeStarredState(starred);


                linearShowAnswer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        answer.setVisibility(View.VISIBLE);
                        if (!EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
                            EntranceShowActivity.this.showedAnsweresIds.add(entranceQuestionModel.uniqueId);
//                            mainConstraint.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable)));
//                            img1.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));
//                            img2.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));
//                            img3.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));

//                            questionNumber.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.concough_border_round_outline_blue_disabled_style));
//                            questionNumber.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGray4));

                            answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                            answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                        }
                    }
                });

                starImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeStarredState(false);
                        addStarQuestionId(entranceQuestionModel.uniqueId, entranceQuestionModel.number, false);
                        EntranceShowActivity.this.starredIds.remove(entranceQuestionModel.uniqueId);
                        EntranceShowActivity.this.loadStarredQuestionRecords();
                        globalPairListInteger = starredIds.size();

//                        for (Pair<String,Integer> item: EntranceShowActivity.this.globalPairListInteger) {
//                            if(item.first == )
//                        }
                    }
                });

                if (EntranceShowActivity.this.showAllAnswers) {
                    answer.setVisibility(View.VISIBLE);
                    answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                    answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                } else {

                    if (EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
                        answer.setVisibility(View.VISIBLE);
                        answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                        answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
                    }
                }


                setImages();
            }

            public void setImages() {
                insertImage(mEntranceQuestionModel.images);
            }


            public Boolean addStarQuestionId(String questionId, Integer questionNumber, Boolean state) {
                Boolean flag = false;

                if (state) {
                    if (!EntranceShowActivity.this.starredIds.contains(questionId)) {
                        if (EntranceQuestionStarredModelHandler.add(getApplicationContext(), username, entranceUniqueId, questionId)) {
                            EntranceShowActivity.this.starredIds.add(questionId);
                            flag = true;

                            JsonObject eData = new JsonObject();
                            eData.addProperty("uniqueId", entranceUniqueId);
                            eData.addProperty("questionNo", questionNumber);
                            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceQuestionStar.getTitle(), eData);

                        }
                    }
                } else {
                    if (EntranceShowActivity.this.starredIds.contains(questionId)) {
                        if (EntranceQuestionStarredModelHandler.remove(getApplicationContext(), username, entranceUniqueId, questionId)) {
                            EntranceShowActivity.this.starredIds.remove(questionId);
                            flag = true;

                            JsonObject eData = new JsonObject();
                            eData.addProperty("uniqueId", entranceUniqueId);
                            eData.addProperty("questionNo", questionNumber);
                            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceQuestionUnStar.getTitle(), eData);

                        }
                    }
                }

                return flag;
            }


            public void changeStarredState(Boolean state) {
                if (state) {
                    starImage.setImageResource(R.drawable.star_filled);
                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughRedLight));
                } else {
                    starImage.setImageResource(R.drawable.star_empty);
                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray2));
                }
            }


            public void insertImage(String imageString) {
                img1.setImageDrawable(null);
                img2.setImageDrawable(null);
                img3.setImageDrawable(null);

                imgPreLoad.setVisibility(View.VISIBLE);

                ArrayList<JsonObject> jsonObjects = new ArrayList<>();
                JsonArray jsonArray = new JsonParser().parse(imageString).getAsJsonArray();

                if (hashKey == null) {
                    String hashStr = username + ":" + getSECRET_KEY();
                    hashKey = MD5Digester.digest(hashStr);
                }

                for (JsonElement item : jsonArray) {
                    jsonObjects.add(item.getAsJsonObject());
                }

                Collections.sort(jsonObjects, new SortedList.Callback<JsonObject>() {
                    @Override
                    public int compare(JsonObject o1, JsonObject o2) {

                        if (o1.get("order").getAsInt() > o2.get("order").getAsInt()) {
                            return 1;
                        } else if (o1.get("order").getAsInt() < o2.get("order").getAsInt()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }

                    @Override
                    public void onChanged(int position, int count) {

                    }

                    @Override
                    public boolean areContentsTheSame(JsonObject oldItem, JsonObject newItem) {
                        return false;
                    }

                    @Override
                    public boolean areItemsTheSame(JsonObject item1, JsonObject item2) {
                        return false;
                    }

                    @Override
                    public void onInserted(int position, int count) {

                    }

                    @Override
                    public void onRemoved(int position, int count) {

                    }

                    @Override
                    public void onMoved(int fromPosition, int toPosition) {

                    }
                });

                final ArrayList<byte[]> localBitmaps = new ArrayList<>();
                for (JsonElement item : jsonObjects) {
                    String imageId = item.getAsJsonObject().get("unique_key").getAsString();
//                    if (!imageRepo.containsKey(imageId)) {
//                        String filePath = entranceUniqueId + "/" + imageId;
//
//                        File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
//                        if (file.exists()) {
//                            try {
//                                byte[] buffer = new byte[(int) file.length()];
//                                FileInputStream input = new FileInputStream(file);
//
//
//                                input.read(buffer);
//
//                                byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
//                                byte[] i = new AES256JNCryptor(1000).decryptData(decoded, hashKey.toCharArray());
////
////                                ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
////                                AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());
////
////                                byte[] i = convertStreamToByteArray(cryptStream);
//
////                                RNCryptorNative rncryptor = new RNCryptorNative();
////                                String data = rncryptor.decrypt(new String(buffer), hashKey);
////                                byte[] i = data.getBytes();
//
//                                localBitmaps.add(i);
//                                imageRepo.put(imageId, i);
//
//                                i = null;
////                                data = null;
////                                rncryptor = null;
//                                buffer = null;
//                                //decoded = null;
//                                input.close();
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    } else {
//                    }
                    localBitmaps.add(imageRepo.get(imageId));
                }

                //img1.setVisibility(View.INVISIBLE);
                //img2.setVisibility(View.INVISIBLE);
                //img3.setVisibility(View.INVISIBLE);

                if (localBitmaps.size() >= 1) {
                    try {
                        final byte[] local1 = localBitmaps.get(0);
                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);


                        Glide.with(EntranceShowActivity.this)

                                .load(local1)

//                                .placeholder(R.drawable.no_image)
                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img1.setVisibility(View.VISIBLE);
                                        img1.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                //.error(R.drawable.ic_thumb_placeholder)
                                // .transform(new CircleTransform(this))
                                //.override(mWidth,mHeight)
                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()
                                .into(img1);


                        //img1.setImageBitmap(bitmap);
                        // img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        // img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img1.setVisibility(View.VISIBLE);
                        img1.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (localBitmaps.size() >= 2) {
                    try {


                        Glide.with(EntranceShowActivity.this)

                                .load(localBitmaps.get(1))

                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
//                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img2.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img2.setVisibility(View.VISIBLE);
                                        img2.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })

                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()

                                .into(img2);

                        img2.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img2.setVisibility(View.VISIBLE);
                        img2.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (localBitmaps.size() >= 3) {
                    try {


                        Glide.with(EntranceShowActivity.this)

                                .load(localBitmaps.get(2))

                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
                                        img3.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img3.setVisibility(View.VISIBLE);
                                        img3.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()
                                .into(img3);

                        img3.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img3.setVisibility(View.VISIBLE);
                        img3.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                localBitmaps.clear();


            }
        }


        @Override
        public int getItemViewType(int position) {
            String type = mPairList.get(position).first;
            if (type.equals("h")) {
                return 0;
            } else {
                return 1;
            }
//
//            if (i == 0 || i == 7 || i == 14 || i == 24 || i == 47) {
//
//            } else {
//                return 1;
//            }
        }


        @Override
        public int getHeaderPositionForItem(int itemPosition) {
            int headerPosition = 0;
            do {
                if (this.isHeader(itemPosition)) {
                    headerPosition = itemPosition;
                    break;
                }
                itemPosition -= 1;
            } while (itemPosition >= 0);
            return headerPosition;
        }


        @Override
        public int getHeaderLayout(int headerPosition) {
            Log.d("ppp", "getHeaderLayout: Header-Position :" + headerPosition);
            return R.layout.item_sticky_header_entrance_show_starred;
        }

        @Override
        public void bindHeaderData(View header, int headerPosition) {
            TextView tv = (TextView) header.findViewById(R.id.headerShowStarred_headerTitle);
            TextView tvCount = (TextView) header.findViewById(R.id.headerShowStarred_countEntrance);
            tv.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            tvCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            Lessions temp = (Lessions) mPairList.get(headerPosition).second;
            tv.setText(temp.lessionTitle);
            tvCount.setText(String.format("%s", FormatterSingleton.getInstance().getNumberFormatter().format(temp.count)));

        }

        @Override
        public boolean isHeader(int itemPosition) {

            String type = mPairList.get(itemPosition).first;
            if (type.equals("h")) {
                return true;
            } else {
                return false;
            }

//
//            int i = itemPosition;
//            if (i == 0 || i == 7 || i == 14 || i == 24 || i == 47) {
//                return true;
//            } else {
//                return false;
//            }
        }


        public void loadImages(String imageString) {
            ArrayList<JsonObject> jsonObjects = new ArrayList<>();
            JsonArray jsonArray = new JsonParser().parse(imageString).getAsJsonArray();

            for (JsonElement item : jsonArray) {
                jsonObjects.add(item.getAsJsonObject());
            }

            Collections.sort(jsonObjects, new SortedList.Callback<JsonObject>() {
                @Override
                public int compare(JsonObject o1, JsonObject o2) {

                    if (o1.get("order").getAsInt() > o2.get("order").getAsInt()) {
                        return 1;
                    } else if (o1.get("order").getAsInt() < o2.get("order").getAsInt()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }

                @Override
                public void onChanged(int position, int count) {

                }

                @Override
                public boolean areContentsTheSame(JsonObject oldItem, JsonObject newItem) {
                    return false;
                }

                @Override
                public boolean areItemsTheSame(JsonObject item1, JsonObject item2) {
                    return false;
                }

                @Override
                public void onInserted(int position, int count) {

                }

                @Override
                public void onRemoved(int position, int count) {

                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {

                }
            });

            String hashStr = username + ":" + getSECRET_KEY();
            String hashKey = MD5Digester.digest(hashStr);

            for (JsonElement item : jsonObjects) {
                String imageId = item.getAsJsonObject().get("unique_key").getAsString();
                if (!imageRepo.containsKey(imageId)) {
                    String filePath = entranceUniqueId + "/" + imageId;

                    File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
                    if (file.exists()) {
                        try {
                            byte[] buffer = new byte[(int) file.length()];
                            FileInputStream input = new FileInputStream(file);


                            input.read(buffer);

                            byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
                            byte[] i = new AES256JNCryptor(1000).decryptData(decoded, hashKey.toCharArray());

//                            ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
//                            AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());
//
//                            byte[] i = convertStreamToByteArray(cryptStream);


//                            RNCryptorNative rncryptor = new RNCryptorNative();
//
//                            String t = Base64.encodeToString(buffer, Base64.NO_WRAP);
//                            String data = rncryptor.decrypt(t, hashKey);
//                            byte[] i = data.getBytes();


                            //Bitmap bitmap = BitmapFactory.decodeByteArray(i, 0, i.length);

                            imageRepo.put(imageId, i);

                            i = null;
//                            data = null;
//                            rncryptor = null;
                            buffer = null;

//                            imageRepo.put(imageId, i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }


    }


    public class HeaderItemDecoration extends RecyclerView.ItemDecoration {


        private StickyHeaderInterface mListener;
        private int mStickyHeaderHeight;

        public HeaderItemDecoration(RecyclerView recyclerView, @NonNull StickyHeaderInterface listener) {
            mListener = listener;

            // On Sticky Header Click
            recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                    if (motionEvent.getY() <= mStickyHeaderHeight) {
                        // Handle the clicks on the header here ...
                        return true;
                    }
                    return false;
                }

                public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

                }

                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                }
            });
        }


        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

            View topChild = parent.getChildAt(0);
            if (topChild == null) {
                return;
            }

            int topChildPosition = parent.getChildAdapterPosition(topChild);
            if (topChildPosition == RecyclerView.NO_POSITION) {
                return;
            }

            View currentHeader = getHeaderViewForItem(topChildPosition, parent);
            fixLayoutSize(parent, currentHeader);
            int contactPoint = currentHeader.getBottom();
            View childInContact = getChildInContact(parent, contactPoint);
            if (childInContact == null) {
                return;
            }

            if (mListener.isHeader(parent.getChildAdapterPosition(childInContact))) {
                moveHeader(c, currentHeader, childInContact);
                return;
            }

            drawHeader(c, currentHeader);
        }

        private View getHeaderViewForItem(int itemPosition, RecyclerView parent) {
            int headerPosition = mListener.getHeaderPositionForItem(itemPosition);
            int layoutResId = mListener.getHeaderLayout(headerPosition);
            View header = LayoutInflater.from(EntranceShowActivity.this).inflate(layoutResId, parent, false);
//            header.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            mListener.bindHeaderData(header, headerPosition);
            return header;
        }

        private void drawHeader(Canvas c, View header) {
            c.save();
            c.translate(0, 0);
            header.draw(c);
            c.restore();
        }

        private void moveHeader(Canvas c, View currentHeader, View nextHeader) {
            c.save();
            c.translate(0, nextHeader.getTop() - currentHeader.getHeight());
            currentHeader.draw(c);
            c.restore();
        }

        private View getChildInContact(RecyclerView parent, int contactPoint) {
            View childInContact = null;
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child.getBottom() > contactPoint) {
                    if (child.getTop() <= contactPoint) {
                        // This child overlaps the contactPoint
                        childInContact = child;
                        break;
                    }
                }
            }
            return childInContact;
        }

        /**
         * Properly measures and layouts the top sticky header.
         *
         * @param parent ViewGroup: RecyclerView in this case.
         */
        private void fixLayoutSize(ViewGroup parent, View view) {

            // Specs for parent (RecyclerView)
            int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

            // Specs for children (headers)
            int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec, parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec, parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);

            view.measure(childWidthSpec, childHeightSpec);

            view.layout(0, 0, view.getMeasuredWidth(), mStickyHeaderHeight = view.getMeasuredHeight());
        }

    }

    public interface StickyHeaderInterface {

        /**
         * This method gets called by {@link HeaderItemDecoration} to fetch the position of the header item in the starredAdapter
         * that is used for (represents) item at specified position.
         *
         * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
         * @return int. Position of the header item in the starredAdapter.
         */
        int getHeaderPositionForItem(int itemPosition);

        /**
         * This method gets called by {@link HeaderItemDecoration} to get layout resource id for the header item at specified starredAdapter's position.
         *
         * @param headerPosition int. Position of the header item in the starredAdapter.
         * @return int. Layout resource id.
         */
        int getHeaderLayout(int headerPosition);

        /**
         * This method gets called by {@link HeaderItemDecoration} to setup the header View.
         *
         * @param header         View. Header to set the data on.
         * @param headerPosition int. Position of the header item in the starredAdapter.
         */
        void bindHeaderData(View header, int headerPosition);

        /**
         * This method gets called by {@link HeaderItemDecoration} to verify whether the item represents a header.
         *
         * @param itemPosition int.
         * @return true, if item at the specified starredAdapter's position represents a header.
         */
        boolean isHeader(int itemPosition);
    }


//
//
//    private class EntranceShowStarredAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//
//        private Context context;
//        private RealmList<EntranceStarredQuestionModel> starredQuestionModelList;
//
////        private  String hashKey;
//
//        public EntranceShowStarredAdapter(Context context) {
//            this.context = context;
//            this.starredQuestionModelList = new RealmList<>();
//
//
//        }
//
//
//
//        public void setItems(RealmList<EntranceStarredQuestionModel> starredQuestionModelList) {
//            this.starredQuestionModelList = starredQuestionModelList;
//            int i = 0;
//            int j = 0;
//            for (EntranceStarredQuestionModel item : starredQuestionModelList) {
//
//
//                if (i > 5) {
//                    int temp = j % 3;
//
//                    switch (temp) {
//                        case 0:
//                            if (EntranceShowActivity.this.handler != null) {
//                                Message msg = EntranceShowActivity.this.handler.obtainMessage(LOAD_IMAGE);
//                                msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("IMAGES_STRING", item.images);
//                                msg.setData(bundle);
//
//                                EntranceShowActivity.this.handler.sendMessage(msg);
//
//                            }
//                            break;
//
//
//                        case 1:
//                            if (EntranceShowActivity.this.starredHandler != null) {
//                                Message msg = EntranceShowActivity.this.starredHandler.obtainMessage(LOAD_IMAGE);
//                                msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("IMAGES_STRING", item.images);
//                                msg.setData(bundle);
//
//                                EntranceShowActivity.this.starredHandler.sendMessage(msg);
//
//                            }
//                            break;
//
//
//                        case 2:
//                            if (EntranceShowActivity.this.handler3 != null) {
//                                Message msg = EntranceShowActivity.this.handler3.obtainMessage(LOAD_IMAGE);
//                                msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("IMAGES_STRING", item.images);
//                                msg.setData(bundle);
//
//                                EntranceShowActivity.this.handler3.sendMessage(msg);
//
//                            }
//                            break;
//                    }
//                    j++;
//
//                } else {
//                    loadImages(item.images);
//                }
//                i++;
//
//            }
//
//            recyclerView.post(new Runnable() {
//                @Override
//                public void run() {
//                    // Call smooth scroll
//                    recyclerView.smoothScrollToPosition(0);
//                }
//            });
//        }
//
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(context).inflate(R.layout.cc_entrance_show_holder1, parent, false);
//            return new EntranceShowHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
//            final EntranceQuestionModel oneItem = this.questionModelList.get(position);
//            ((EntranceShowHolder) holder).setupHolder(oneItem);
//        }
//
//        @Override
//        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
//            super.onViewAttachedToWindow(holder);
//
////            ((EntranceShowHolder) holder).setImages();
////            (ImageView) img1 = (ImageView) holder.itemView.getRootView().findViewById(R.id.ccEntranceShowHolder1I_img1);
//        }
//
//        @Override
//        public int getItemCount() {
//            return questionModelList.size();
//        }
//
//        public byte[] convertStreamToByteArray(InputStream is) throws IOException {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte[] buff = new byte[4096];
//            int i = Integer.MAX_VALUE;
//            while ((i = is.read(buff, 0, buff.length)) > 0) {
//                baos.write(buff, 0, i);
//            }
//
//            return baos.toByteArray(); // be sure to close InputStream in calling function
//        }
//
//        public void loadImages(String imageString) {
//            ArrayList<JsonObject> jsonObjects = new ArrayList<>();
//            JsonArray jsonArray = new JsonParser().parse(imageString).getAsJsonArray();
//
//            for (JsonElement item : jsonArray) {
//                jsonObjects.add(item.getAsJsonObject());
//            }
//
//            Collections.sort(jsonObjects, new SortedList.Callback<JsonObject>() {
//                @Override
//                public int compare(JsonObject o1, JsonObject o2) {
//
//                    if (o1.get("order").getAsInt() > o2.get("order").getAsInt()) {
//                        return 1;
//                    } else if (o1.get("order").getAsInt() < o2.get("order").getAsInt()) {
//                        return -1;
//                    } else {
//                        return 0;
//                    }
//                }
//
//                @Override
//                public void onChanged(int position, int count) {
//
//                }
//
//                @Override
//                public boolean areContentsTheSame(JsonObject oldItem, JsonObject newItem) {
//                    return false;
//                }
//
//                @Override
//                public boolean areItemsTheSame(JsonObject item1, JsonObject item2) {
//                    return false;
//                }
//
//                @Override
//                public void onInserted(int position, int count) {
//
//                }
//
//                @Override
//                public void onRemoved(int position, int count) {
//
//                }
//
//                @Override
//                public void onMoved(int fromPosition, int toPosition) {
//
//                }
//            });
//
//            String hashStr = username + ":" + getSECRET_KEY();
//            String hashKey = MD5Digester.digest(hashStr);
//
//            for (JsonElement item : jsonObjects) {
//                String imageId = item.getAsJsonObject().get("unique_key").getAsString();
//                if (!imageRepo.containsKey(imageId)) {
//                    String filePath = entranceUniqueId + "/" + imageId;
//
//                    File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
//                    if (file.exists()) {
//                        try {
//                            byte[] buffer = new byte[(int) file.length()];
//                            FileInputStream input = new FileInputStream(file);
//
//
//                            input.read(buffer);
//
//                            byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
//                            byte[] i = new AES256JNCryptor(1000).decryptData(decoded, hashKey.toCharArray());
//
////                            ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
////                            AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());
////
////                            byte[] i = convertStreamToByteArray(cryptStream);
//
//
////                            RNCryptorNative rncryptor = new RNCryptorNative();
////
////                            String t = Base64.encodeToString(buffer, Base64.NO_WRAP);
////                            String data = rncryptor.decrypt(t, hashKey);
////                            byte[] i = data.getBytes();
//
//
//                            //Bitmap bitmap = BitmapFactory.decodeByteArray(i, 0, i.length);
//
//                            imageRepo.put(imageId, i);
//
//                            i = null;
////                            data = null;
////                            rncryptor = null;
//                            buffer = null;
//
////                            imageRepo.put(imageId, i);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        }
//
//
//        private class EntranceShowHolder extends RecyclerView.ViewHolder {
//            private TextView questionNumber;
//            private ImageView starImage;
//            private ImageView imgPreLoad;
//            private ConstraintLayout mainConstraint;
//            private ImageView img1;
//            private ImageView img2;
//            private ImageView img3;
//            private LinearLayout linearShowAnswer;
//            private TextView answerLabel;
//            private ImageView answerLabelCheckbox;
//
//            private TextView answer;
//            private Boolean starred = false;
//            private EntranceQuestionModel mEntranceQuestionModel;
//            private Integer mWidth;
//            private Integer mHeight;
//
//
//            public EntranceShowHolder(View itemView) {
//
//                super(itemView);
//
//                questionNumber = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_questionNumber);
//                starImage = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_star);
//
//                imgPreLoad = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_imgPreLoad);
//                img1 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img1);
//                img2 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img2);
//                img3 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img3);
//
//
//                mainConstraint = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_mainConstrant);
//
////                img1.setImageDrawable(null);
////                img2.setImageDrawable(null);
////                img3.setImageDrawable(null);
//
////                img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
////                img1.setAdjustViewBounds(true);
//
//                linearShowAnswer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowAnswer);
//                answerLabel = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowAnswer);
//                answerLabelCheckbox = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_checkBox);
//                answer = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer);
//
//
//                questionNumber.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
//                answerLabel.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
//                answer.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
//            }
//
//
//            public void setImages() {
//
//
////                final Handler handler = new Handler();
////                handler.postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
//                insertImage(mEntranceQuestionModel.images);
////                    }
////                }, 100);
//            }
//
//
//            public void setupHolder(final EntranceQuestionModel entranceQuestionModel) {
//                answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
//                answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
//                answer.setVisibility(View.INVISIBLE);
////                imgPreLoad.setVisibility(View.VISIBLE);
//
//                questionNumber.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.number));
//                answer.setText("گزینه " + FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.answer) + " صحیح است");
//
//                mEntranceQuestionModel = entranceQuestionModel;
//
//                if (EntranceShowActivity.this.starredIds.contains(entranceQuestionModel.uniqueId)) {
//                    starred = true;
//                } else {
//                    starred = false;
//                }
//
//                changeStarredState(starred);
//
//
//                linearShowAnswer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        answer.setVisibility(View.VISIBLE);
//                        if (!EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
//                            EntranceShowActivity.this.showedAnsweresIds.add(entranceQuestionModel.uniqueId);
////                            mainConstraint.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable)));
////                            img1.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));
////                            img2.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));
////                            img3.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGrayDiasable));
//
////                            questionNumber.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.concough_border_round_outline_blue_disabled_style));
////                            questionNumber.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGray4));
//
//                            answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                            answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                        }
//                    }
//                });
//
//                starImage.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        starred = !starred;
//                        changeStarredState(starred);
//                        addStarQuestionId(entranceQuestionModel.uniqueId, entranceQuestionModel.number, starred);
//                    }
//                });
//
//                if (EntranceShowActivity.this.showAllAnswers) {
//                    answer.setVisibility(View.VISIBLE);
//                    answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                    answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                } else {
//
//                    if (EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
//                        answer.setVisibility(View.VISIBLE);
//                        answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                        answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                    }
//                }
//
//                setImages();
//            }
//
//            public void changeStarredState(Boolean state) {
//                if (state) {
//                    starImage.setImageResource(R.drawable.star_filled);
//                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughRedLight));
//                } else {
//                    starImage.setImageResource(R.drawable.star_empty);
//                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray2));
//                }
//            }
//
//
//            public Boolean addStarQuestionId(String questionId, Integer questionNumber, Boolean state) {
//                Boolean flag = false;
//
//                if (state) {
//                    if (!EntranceShowActivity.this.starredIds.contains(questionId)) {
//                        if (EntranceQuestionStarredModelHandler.add(getApplicationContext(), username, entranceUniqueId, questionId)) {
//                            EntranceShowActivity.this.starredIds.add(questionId);
//                            flag = true;
//
//                            JsonObject eData = new JsonObject();
//                            eData.addProperty("uniqueId", entranceUniqueId);
//                            eData.addProperty("questionNo", questionNumber);
//                            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceQuestionStar.getTitle(), eData);
//
//                        }
//                    }
//                } else {
//                    if (EntranceShowActivity.this.starredIds.contains(questionId)) {
//                        if (EntranceQuestionStarredModelHandler.remove(getApplicationContext(), username, entranceUniqueId, questionId)) {
//                            EntranceShowActivity.this.starredIds.remove(questionId);
//                            flag = true;
//
//                            JsonObject eData = new JsonObject();
//                            eData.addProperty("uniqueId", entranceUniqueId);
//                            eData.addProperty("questionNo", questionNumber);
//                            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceQuestionUnStar.getTitle(), eData);
//
//                        }
//                    }
//                }
//
//                return flag;
//            }
//
//            public void insertImage(String imageString) {
//                img1.setImageDrawable(null);
//                img2.setImageDrawable(null);
//                img3.setImageDrawable(null);
//
//                imgPreLoad.setVisibility(View.VISIBLE);
//
//                ArrayList<JsonObject> jsonObjects = new ArrayList<>();
//                JsonArray jsonArray = new JsonParser().parse(imageString).getAsJsonArray();
//
//                if (hashKey == null) {
//                    String hashStr = username + ":" + getSECRET_KEY();
//                    hashKey = MD5Digester.digest(hashStr);
//                }
//
//                for (JsonElement item : jsonArray) {
//                    jsonObjects.add(item.getAsJsonObject());
//                }
//
//                Collections.sort(jsonObjects, new SortedList.Callback<JsonObject>() {
//                    @Override
//                    public int compare(JsonObject o1, JsonObject o2) {
//
//                        if (o1.get("order").getAsInt() > o2.get("order").getAsInt()) {
//                            return 1;
//                        } else if (o1.get("order").getAsInt() < o2.get("order").getAsInt()) {
//                            return -1;
//                        } else {
//                            return 0;
//                        }
//                    }
//
//                    @Override
//                    public void onChanged(int position, int count) {
//
//                    }
//
//                    @Override
//                    public boolean areContentsTheSame(JsonObject oldItem, JsonObject newItem) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean areItemsTheSame(JsonObject item1, JsonObject item2) {
//                        return false;
//                    }
//
//                    @Override
//                    public void onInserted(int position, int count) {
//
//                    }
//
//                    @Override
//                    public void onRemoved(int position, int count) {
//
//                    }
//
//                    @Override
//                    public void onMoved(int fromPosition, int toPosition) {
//
//                    }
//                });
//
//                final ArrayList<byte[]> localBitmaps = new ArrayList<>();
//                for (JsonElement item : jsonObjects) {
//                    String imageId = item.getAsJsonObject().get("unique_key").getAsString();
////                    if (!imageRepo.containsKey(imageId)) {
////                        String filePath = entranceUniqueId + "/" + imageId;
////
////                        File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
////                        if (file.exists()) {
////                            try {
////                                byte[] buffer = new byte[(int) file.length()];
////                                FileInputStream input = new FileInputStream(file);
////
////
////                                input.read(buffer);
////
////                                byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
////                                byte[] i = new AES256JNCryptor(1000).decryptData(decoded, hashKey.toCharArray());
//////
//////                                ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
//////                                AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());
//////
//////                                byte[] i = convertStreamToByteArray(cryptStream);
////
//////                                RNCryptorNative rncryptor = new RNCryptorNative();
//////                                String data = rncryptor.decrypt(new String(buffer), hashKey);
//////                                byte[] i = data.getBytes();
////
////                                localBitmaps.add(i);
////                                imageRepo.put(imageId, i);
////
////                                i = null;
//////                                data = null;
//////                                rncryptor = null;
////                                buffer = null;
////                                //decoded = null;
////                                input.close();
////
////                            } catch (Exception e) {
////                                e.printStackTrace();
////                            }
////                        }
////                    } else {
////                    }
//                    localBitmaps.add(imageRepo.get(imageId));
//                }
//
//                //img1.setVisibility(View.INVISIBLE);
//                //img2.setVisibility(View.INVISIBLE);
//                //img3.setVisibility(View.INVISIBLE);
//
//                if (localBitmaps.size() >= 1) {
//                    try {
//                        final byte[] local1 = localBitmaps.get(0);
//                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);
//
//
//                        Glide.with(EntranceShowActivity.this)
//
//                                .load(local1)
//
////                                .placeholder(R.drawable.no_image)
//                                .listener(new RequestListener<byte[], GlideDrawable>() {
//                                    @Override
//                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                        return false;
//                                    }
//
//                                    @Override
//                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                        imgPreLoad.setVisibility(View.GONE);
//                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                                        img1.setVisibility(View.VISIBLE);
//                                        img1.setAdjustViewBounds(true);
//
//                                        return false;
//                                    }
//
//                                })
//                                //.error(R.drawable.ic_thumb_placeholder)
//                                // .transform(new CircleTransform(this))
//                                //.override(mWidth,mHeight)
//                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
//                                .crossFade()
//                                .fitCenter()
//                                .into(img1);
//
//
//                        //img1.setImageBitmap(bitmap);
//                        // img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                        // img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                        img1.setVisibility(View.VISIBLE);
//                        img1.setAdjustViewBounds(true);
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (localBitmaps.size() >= 2) {
//                    try {
//
//                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);
//
//                        Glide.with(EntranceShowActivity.this)
//
//                                .load(localBitmaps.get(1))
//
////                                .placeholder(R.drawable.no_image)
//                                .listener(new RequestListener<byte[], GlideDrawable>() {
//                                    @Override
//                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                        return false;
//                                    }
//
//                                    @Override
//                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                        imgPreLoad.setVisibility(View.GONE);
////                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                                        img2.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                                        img2.setVisibility(View.VISIBLE);
//                                        img2.setAdjustViewBounds(true);
//
//                                        return false;
//                                    }
//
//                                })
//                                //.error(R.drawable.ic_thumb_placeholder)
//                                // .transform(new CircleTransform(this))
//                                //.override(mWidth,mHeight)
//                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
//                                .crossFade()
//                                .fitCenter()
//
//                                .into(img2);
//
//
//                        //img1.setImageBitmap(bitmap);
////                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                        img2.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                        img2.setVisibility(View.VISIBLE);
//                        img2.setAdjustViewBounds(true);
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (localBitmaps.size() >= 3) {
//                    try {
//
//                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);
//
//                        Glide.with(EntranceShowActivity.this)
//
//                                .load(localBitmaps.get(2))
//
////                                .placeholder(R.drawable.no_image)
//                                .listener(new RequestListener<byte[], GlideDrawable>() {
//                                    @Override
//                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                        return false;
//                                    }
//
//                                    @Override
//                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                        imgPreLoad.setVisibility(View.GONE);
////                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                                        img3.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                                        img3.setVisibility(View.VISIBLE);
//                                        img3.setAdjustViewBounds(true);
//
//                                        return false;
//                                    }
//
//                                })
//                                //.error(R.drawable.ic_thumb_placeholder)
//                                // .transform(new CircleTransform(this))
//                                //.override(mWidth,mHeight)
//                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
//                                .crossFade()
//                                .fitCenter()
//                                .into(img3);
//
//
//                        //img1.setImageBitmap(bitmap);
////                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                        img3.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                        img3.setVisibility(View.VISIBLE);
//                        img3.setAdjustViewBounds(true);
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                localBitmaps.clear();
//
//
//            }
//
//
//        }
//
//    }
//
//


}
