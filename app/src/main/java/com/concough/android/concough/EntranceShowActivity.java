package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.concough.android.chartaccessory.ChartValueNumberFormatter;
import com.concough.android.concough.dialogs.EntranceNewLessonExamDialog;
import com.concough.android.concough.dialogs.EntranceShowAllCommentsDialog;
import com.concough.android.concough.dialogs.EntranceShowInfoDialog;
import com.concough.android.concough.dialogs.EntranceShowNewCommentDialog;
import com.concough.android.concough.interfaces.EntranceLessonExamDelegate;
import com.concough.android.concough.dialogs.EntranceLessonExamResultDialog;
import com.concough.android.concough.interfaces.EntranceShowCommentDelegate;
import com.concough.android.concough.interfaces.EntranceShowInfoDelegate;
import com.concough.android.general.AlertClass;
import com.concough.android.models.EntranceBookletModel;
import com.concough.android.models.EntranceLastVisitInfoModel;
import com.concough.android.models.EntranceLastVisitInfoModelHandler;
import com.concough.android.models.EntranceLessonExamModel;
import com.concough.android.models.EntranceLessonExamModelHandler;
import com.concough.android.models.EntranceLessonModel;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.EntranceOpenedCountModelHandler;
import com.concough.android.models.EntranceQuestionCommentModel;
import com.concough.android.models.EntranceQuestionCommentModelHandler;
import com.concough.android.models.EntranceQuestionExamStatModelHandler;
import com.concough.android.models.EntranceQuestionModel;
import com.concough.android.models.EntranceQuestionModelHandler;
import com.concough.android.models.EntranceQuestionStarredModelHandler;
import com.concough.android.models.EntranceStarredQuestionModel;
import com.concough.android.models.UserLogModelHandler;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.EntranceCommentType;
import com.concough.android.structures.EntranceLessonExamStructure;
import com.concough.android.structures.EntranceQuestionAnswerState;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.LogTypeEnum;
import com.concough.android.utils.MD5Digester;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;
import static com.concough.android.concough.R.id.headerShowStarred_countEntrance;
import static com.concough.android.extensions.TypeExtensionsKt.diffInHourMinSec;
import static com.concough.android.extensions.TypeExtensionsKt.timeAgoSinceDate;
import static com.concough.android.settings.ConstantsKt.getSECRET_KEY;
import static com.concough.android.utils.DataConvertorsKt.questionAnswerToString;
import static com.concough.android.utils.UtilitiesKt.spToDp;

public class EntranceShowActivity extends AppCompatActivity implements Handler.Callback,
        EntranceShowCommentDelegate, EntranceShowInfoDelegate, EntranceLessonExamDelegate {

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
    private Timer examTimer = null;

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

    private EntranceQuestionAnswerState defaultShowType = EntranceQuestionAnswerState.None;
    private HashMap<String, EntranceQuestionAnswerState> showedAnswer = new HashMap<>();
    private EntranceLastVisitInfoModel lastVisitInfo = null;
    private EntranceLessonExamStructure lessonExamStructure = null;

    //    private Boolean showStarredQuestions = false;
    private ArrayList<String> dialogInfoList;
    private ArrayList<String> starredIds = new ArrayList<>();

    private int selectedBooklet = -1;
    private int selectedLesson = -1;

    private ArrayAdapter<String> lessonAdapter;
    private DialogAdapter bookletAdapter;
    private DialogAdapter dialogInfoAdapter;
    private EntranceShowAdapter entranceShowAdapter;

    private CustomTabLayout tabLayout;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private DialogPlus dialog;
//    private Dialog dialogInfo;
    private Button texButton;
    private Button infoButton;
    private LinearLayout backButton;
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewStar;
    private KProgressHUD loading;
    private PowerManager.WakeLock wakeLock;
    private ConstraintLayout timerContainer;
    private ConstraintLayout backToDefaultContainer;
    private TextView timerTextView;
    private TextView examQCountTextView;

    private StarredShowAdapter starredAdapter;
    private HeaderItemDecoration itemDecoration;

    private CustomGridLayoutManager recycleLinearLayout;

    public static Intent newIntent(Context packageContext, String entranceUniqueId, String showType) {
        Intent i = new Intent(packageContext, EntranceShowActivity.class);
        i.putExtra(ENTRANCE_UNIQUE_ID_KEY, entranceUniqueId);
        i.putExtra(SHOW_TYPE_KEY, showType);
        return i;
    }

    public class CustomGridLayoutManager extends LinearLayoutManager {
        private boolean isScrollEnabled = true;
        private final Handler handler = new Handler();

        public CustomGridLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(final boolean flag) {

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CustomGridLayoutManager.this.isScrollEnabled = flag;
                }
            }, 100);

        }

        @Override
        public boolean canScrollVertically() {
            //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
            return isScrollEnabled && super.canScrollVertically();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_show);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        entranceUniqueId = getIntent().getStringExtra(ENTRANCE_UNIQUE_ID_KEY);
        showType = getIntent().getStringExtra(SHOW_TYPE_KEY);

        if (entranceUniqueId.equals("")) {
            finish();
        }

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

        this.backToDefaultContainer = (ConstraintLayout) findViewById(R.id.entranceShowA_backToDefaultContainer);
        this.timerContainer = (ConstraintLayout) findViewById(R.id.entranceShowA_timerContainer);
        this.timerTextView = (TextView) findViewById(R.id.entranceShowA_timerTextView);
        this.examQCountTextView = (TextView) findViewById(R.id.entranceShowA_examQCountTextView);
        this.timerTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        this.examQCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

        Button backToDefaultButton = (Button) findViewById(R.id.entranceShowA_backToDefaultButton);
        Button finishLessonExamButton = (Button) findViewById(R.id.entranceShowA_finishLessonExamButton);
        backToDefaultButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
        finishLessonExamButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

        backToDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EntranceShowActivity.this.cancelLessonExam(false);
            }
        });

        finishLessonExamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertClass.Message msg = AlertClass.convertMessage("ExamAction", "FinishEntranceExam");
                if (msg.getShowMsg()) {
                    AlertClass.showAlertMessageCustom(EntranceShowActivity.this,
                            msg.getTitle(), msg.getMessage(),
                            "اتمام سنجش", "دستم خورد", new Function0<Unit>() {

                                @Override
                                public Unit invoke() {
                                    AlertClass.Message msg2 = AlertClass.convertMessage("ExamAction", "FinishEntranceExamResult");
                                    AlertClass.showSucceessMessageCustom(EntranceShowActivity.this,
                                            msg2.getTitle(), msg2.getMessage(),
                                            "محاسیه نتیجه", "انصراف از سنجش",
                                            new Function0<Unit>() {
                                                @Override
                                                public Unit invoke() {
                                                    EntranceShowActivity.this.finishExam();
                                                    return null;
                                                }
                                            }, new Function0<Unit>() {
                                                @Override
                                                public Unit invoke() {
                                                    EntranceShowActivity.this.cancelLessonExam(true);
                                                    return null;
                                                }
                                            });
                                    return null;
                                }
                            });
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.entranceShowA_recycleEntranceShow);
        recyclerViewStar = (RecyclerView) findViewById(R.id.entranceShowA_recycleEntranceShowFav);

        recycleLinearLayout = new CustomGridLayoutManager(EntranceShowActivity.this);
        recycleLinearLayout.setScrollEnabled(true);

        recyclerViewStar.setVisibility(View.GONE);

        entranceShowAdapter = new EntranceShowAdapter(this);
        recyclerView.setLayoutManager(recycleLinearLayout);
        recyclerView.setAdapter(entranceShowAdapter);

        username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
        lessonAdapter = new ArrayAdapter<String>(this, R.layout.cc_archive_listitem_tabbar);

        dialogInfoList = new ArrayList<>();
        ArrayList<String> bookletList = new ArrayList<>();

        bookletAdapter = new DialogAdapter(EntranceShowActivity.this, bookletList);
        dialogInfoAdapter = new DialogAdapter(EntranceShowActivity.this, dialogInfoList);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.containerViewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Tab
        tabLayout = (CustomTabLayout) findViewById(R.id.tabs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            tabLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();

                EntranceShowActivity.this.selectedLesson = index;
                loadQuestions(index);
                EntranceShowActivity.this.showedAnswer.clear();

                loading = AlertClass.showLoadingMessage(EntranceShowActivity.this);
                loading.show();
                if (showType.equals("Show")) {
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            EntranceShowActivity.this.entranceShowAdapter.setItems(questionsDB);
                            EntranceShowActivity.this.entranceShowAdapter.notifyDataSetChanged();
                            EntranceShowActivity.this.recyclerView.smoothScrollToPosition(0);

                            AlertClass.hideLoadingMessage(loading);
                        }
                    }, 1500);
                }
                //Log.d(TAG, "onTabSelected: ");
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
//                                tabLayout.scrollTo(0, 1);
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

//        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        View mCustomView = LayoutInflater.from(this).inflate(R.layout.cc_entrance_actionbar, null);

        infoButton = (Button) mCustomView.findViewById(R.id.actionBarL_infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dialogInfo.show();

                int aq = 0;
                if (EntranceShowActivity.this.lessonExamStructure != null) {
                    aq = EntranceShowActivity.this.lessonExamStructure.getAnswers().size();
                }

                // make entrance struct
                if (EntranceShowActivity.this.entranceStruct == null) {
                    String extraStr = EntranceShowActivity.this.entranceDB.extraData;
                    JsonElement extraData = null;
                    if (extraStr != null &&  !"".equals(extraStr)) {
                        try {
                            extraData = new JsonParser().parse(extraStr);
                        } catch (Exception exc) {
                            extraData = new JsonParser().parse("[]");
                        }
                    }

                    EntranceShowActivity.this.entranceStruct = new EntranceStruct();
                    EntranceShowActivity.this.entranceStruct.setEntranceUniqueId(EntranceShowActivity.this.entranceDB.uniqueId);
                    EntranceShowActivity.this.entranceStruct.setEntranceLastPublished(EntranceShowActivity.this.entranceDB.lastPublished);
                    EntranceShowActivity.this.entranceStruct.setEntranceDuration(EntranceShowActivity.this.entranceDB.duration);
                    EntranceShowActivity.this.entranceStruct.setEntranceMonth(EntranceShowActivity.this.entranceDB.month);
                    EntranceShowActivity.this.entranceStruct.setEntranceYear(EntranceShowActivity.this.entranceDB.year);
                    EntranceShowActivity.this.entranceStruct.setEntranceBookletCounts(EntranceShowActivity.this.entranceDB.bookletsCount);
                    EntranceShowActivity.this.entranceStruct.setEntranceExtraData(extraData);
                    EntranceShowActivity.this.entranceStruct.setEntranceSetId(EntranceShowActivity.this.entranceDB.setId);
                    EntranceShowActivity.this.entranceStruct.setEntranceSetTitle(EntranceShowActivity.this.entranceDB.set);
                    EntranceShowActivity.this.entranceStruct.setEntranceGroupTitle(EntranceShowActivity.this.entranceDB.group);
                    EntranceShowActivity.this.entranceStruct.setEntranceOrgTitle(EntranceShowActivity.this.entranceDB.organization);
                    EntranceShowActivity.this.entranceStruct.setEntranceTypeTitle(EntranceShowActivity.this.entranceDB.type);
                }

                EntranceShowInfoDialog showInfoDialog = new EntranceShowInfoDialog(EntranceShowActivity.this);
                showInfoDialog.setCancelable(true);
                showInfoDialog.setCanceledOnTouchOutside(true);
                showInfoDialog.setListener(EntranceShowActivity.this);
                showInfoDialog.show();
                showInfoDialog.setupDialog(EntranceShowActivity.this.entranceStruct,
                        EntranceShowActivity.this.starredIds.size(),
                        EntranceShowActivity.this.defaultShowType,
                        EntranceShowActivity.this.showType,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).qCount,
                        aq, EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).fullTitle,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).duration);
            }
        });

        backButton = (LinearLayout) mCustomView.findViewById(R.id.linearBack);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        texButton = (Button) mCustomView
                .findViewById(R.id.actionBarL_dropDown);
        texButton.setText(buttonTextMaker("انتخاب کنید", false));
        texButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
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

        if (mActionBar != null) {
            mActionBar.setCustomView(mCustomView, new ActionBar.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mActionBar.setDisplayShowCustomEnabled(true);
        }

        starredAdapter = new StarredShowAdapter(EntranceShowActivity.this);
        itemDecoration = new HeaderItemDecoration(recyclerView, starredAdapter);

        loadLastVisitInfoState();
        loadEntranceDB();
        loadBooklets();
        loadStarredQuestion();
        loadStarredQuestionRecords();

//        infoDialog();
//
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

            recyclerView.setVisibility(View.GONE);
            recyclerViewStar.setVisibility(View.VISIBLE);

            texButton.setText(String.format("سوالات نشان شده (%s)", FormatterSingleton.getInstance().getNumberFormatter().format(starredIds.size()))); //starredAdapter.getItemCount() - 1

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertClass.hideLoadingMessage(loading);
                }
            }, 1500);
        }

        globalPairListInteger = starredIds.size();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Lock");
        wakeLock.acquire();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.wakeLock != null) {
            this.wakeLock.release();
        }
    }

    @Override
    protected void onStop() {
        saveLastVisitInfoState();
        super.onStop();
    }

//    private void infoDialog() {
//        dialogInfo = new Dialog(EntranceShowActivity.this);
//        dialogInfo = new Dialog(EntranceShowActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
//        dialogInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialogInfo.setCancelable(true);
//        dialogInfo.setContentView(R.layout.cc_alert_dialog_entrance_info_icon);
//        dialogInfo.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//
//        TextView tvEntranceTypeName = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_typeName);
//        TextView tvEntranceGroupName = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_groupName);
//        TextView tvEntranceExtraData = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_extraData);
//        final Switch entranceSwitch = (Switch) dialogInfo.findViewById(R.id.entranceInfoIcon_switch);
//        final TextView buttonStarred = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_btnStarred);
//        final TextView tvStarredCount = (TextView) dialogInfo.findViewById(R.id.entranceInfoIcon_starredCount);
//
//
//        tvEntranceTypeName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
//        tvEntranceGroupName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
//        tvEntranceExtraData.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
//        entranceSwitch.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
//        buttonStarred.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
//        tvStarredCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
//
//
//        String entranceYear = FormatterSingleton.getInstance().getNumberFormatter().format(this.entranceDB.year);
//        if (this.entranceDB.month > 0) {
//            tvEntranceTypeName.setText("آزمون " + this.entranceDB.type + " " + monthToString(this.entranceDB.month) + " " + entranceYear);
//        } else {
//            tvEntranceTypeName.setText("آزمون " + this.entranceDB.type + " " + entranceYear);
//        }
//        tvEntranceGroupName.setText(this.entranceDB.group + " (" + this.entranceDB.set + ")");
//
//
////        String extra = "";
////        ArrayList<String> extraArray = new ArrayList<>();
////        JsonObject extraData = new JsonParser().parse(this.entranceDB.extraData).getAsJsonObject();
////
////        for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
////            extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
////        }
////
////        extra = TextUtils.join(" - ", extraArray);
//        tvEntranceExtraData.setText(this.entranceDB.organization);
//
//
//        entranceSwitch.setHighlightColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGreen));
//
//        dialogInfo.setCanceledOnTouchOutside(true);
//        dialogInfo.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                tvStarredCount.setText(String.format("%s", FormatterSingleton.getInstance().getNumberFormatter().format(EntranceShowActivity.this.globalPairListInteger)));
//            }
//        });
//        dialogInfo.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                if (entranceSwitch.isChecked()) {
//                    EntranceShowActivity.this.defaultShowType = EntranceQuestionAnswerState.ANSWER;
//                } else {
//                    EntranceShowActivity.this.defaultShowType = EntranceQuestionAnswerState.None;
//                }
//                entranceShowAdapter.notifyDataSetChanged();
//                starredAdapter.notifyDataSetChanged();
//
////                if (entranceSwitch.isChecked() != showAllAnswers) {
////                    showAllAnswers = entranceSwitch.isChecked();
////                    entranceShowAdapter.notifyDataSetChanged();
////                    starredAdapter.notifyDataSetChanged();
////                }
//            }
//        });
//
//        dialogInfo.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                if (entranceSwitch.isChecked()) {
//                    EntranceShowActivity.this.defaultShowType = EntranceQuestionAnswerState.ANSWER;
//                } else {
//                    EntranceShowActivity.this.defaultShowType = EntranceQuestionAnswerState.None;
//                }
//                entranceShowAdapter.notifyDataSetChanged();
//                starredAdapter.notifyDataSetChanged();
//
////                if (entranceSwitch.isChecked() != showAllAnswers) {
////                    showAllAnswers = entranceSwitch.isChecked();
////                    entranceShowAdapter.notifyDataSetChanged();
////                    starredAdapter.notifyDataSetChanged();
////                }
//            }
//        });
//
//
//        if (showType.equals("Show")) {
//            buttonStarred.setText("سوالات نشان شده");
//            tvStarredCount.setVisibility(View.VISIBLE);
//        } else {
//            buttonStarred.setText("کلیه سوالات");
//            tvStarredCount.setVisibility(View.INVISIBLE);
//
//        }
//
////        if (showAllAnswers) {
//        if (EntranceShowActivity.this.defaultShowType == EntranceQuestionAnswerState.ANSWER) {
//            entranceSwitch.setChecked(false);
//        } else {
//            entranceSwitch.setChecked(true);
//        }
//
//
//        buttonStarred.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                if (tvStarredCount.getVisibility() == View.VISIBLE) { // if must show all questions
//
//                    backButton.setVisibility(View.VISIBLE);
//
//                    showType = "Starred";
//
//                    loading = AlertClass.showLoadingMessage(EntranceShowActivity.this);
//                    loading.show();
//
//                    buttonStarred.setText("کلیه سوالات");
//                    tvStarredCount.setVisibility(View.INVISIBLE);
//                    tabLayout.setVisibility(View.GONE);
//
//                    recyclerView.setVisibility(View.GONE);
//                    recyclerViewStar.setVisibility(View.VISIBLE);
//
//                    texButton.setEnabled(false);
//                    texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
//
//
//                    recyclerViewStar.addItemDecoration(itemDecoration);
//                    recyclerViewStar.setAdapter(starredAdapter);
//                    starredAdapter.setItems(starredQuestions);
//                    starredAdapter.notifyDataSetChanged();
//                    recyclerViewStar.setLayoutManager(new StaggeredGridLayoutManager(1, 1));
//
//                    texButton.setText(String.format("سوالات نشان شده (%s)", FormatterSingleton.getInstance().getNumberFormatter().format(EntranceShowActivity.this.globalPairListInteger)));
//
//
//                    dialogInfo.dismiss();
//
//                    Handler h = new Handler();
//                    h.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            AlertClass.hideLoadingMessage(loading);
//                        }
//                    }, 1500);
//
//
//                } else {
//
//                    backButton.setVisibility(View.VISIBLE);
//
//                    buttonStarred.setText("سوالات نشان شده");
//                    tvStarredCount.setVisibility(View.VISIBLE);
//                    tabLayout.setVisibility(View.VISIBLE);
//
//
//                    texButton.setVisibility(View.VISIBLE);
//                    texButton.setEnabled(true);
//                    texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
//
//                    showType = "Show";
//
//
//                    recyclerViewStar.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//
//                    recyclerView.setLayoutManager(new LinearLayoutManager(EntranceShowActivity.this));
//
//                    dialogInfo.dismiss();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            loadLessions(0);
////                            tabLayout.scrollTo(0, 1);
//                        }
//                    });
//
//                    Handler h = new Handler();
//                    h.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            AlertClass.hideLoadingMessage(loading);
//                        }
//                    }, 1500);
//
//                    dialogInfo.dismiss();
//                }
//            }
//
//        });
//
//        esetImageView = (ImageView) dialogInfo.findViewById(R.id.entranceInfoIcon_entLogo);
//
//        downloadImage(this.entranceDB.setId);
//
//    }

//    private void downloadImage(final int imageId) {
//        byte[] data;
//        final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
//
//        File photo = new File(getApplicationContext().getFilesDir() + "/images/eset", String.valueOf(imageId));
//        if (photo.exists()) {
//            data = convertFileToByteArray(photo);
//            Log.d(TAG, "downloadImage: From File");
//        } else {
//            data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
//        }
//
//        if (data != null) {
//
//            Glide.with(EntranceShowActivity.this)
//
//                    .load(data)
//                    //.crossFade()
//                    .dontAnimate()
//                    .into(esetImageView)
//                    .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));
//
//
//        } else {
//            MediaRestAPIClass.downloadEsetImage(EntranceShowActivity.this, imageId, new Function2<byte[], HTTPErrorType, Unit>() {
//                @Override
//                public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
////                            runOnUiThread(new Runnable() {
////                                @Override
////                                public void run() {
//                    if (httpErrorType != HTTPErrorType.Success) {
//                        Log.d(TAG, "run: ");
//                        if (httpErrorType == HTTPErrorType.Refresh) {
//                            downloadImage(imageId);
//                        } else {
//                            esetImageView.setImageResource(R.drawable.no_image);
//                        }
//                    } else {
//                        if (url != null) {
//                            MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);
//                        }
//
//                        Glide.with(EntranceShowActivity.this)
//
//                                .load(data)
//                                //.crossFade()
//                                .dontAnimate()
//                                .into(esetImageView)
//                                .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));
//
//                    }
////                                }
////                            });
//                    return null;
//                }
//            }, new Function1<NetworkErrorType, Unit>() {
//                @Override
//                public Unit invoke(NetworkErrorType networkErrorType) {
//                    return null;
//                }
//            });
//        }
//    }

    private void saveLastVisitInfoState() {
        String username = UserDefaultsSingleton.getInstance(this.getApplicationContext()).getUsername();

        if (this.selectedBooklet >= 0 && this.selectedLesson >= 0) {
            if ("Show".equals(this.showType)) {
                String index = "0";
                int row = 0;

                int position = ((CustomGridLayoutManager)this.recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                if (position == RecyclerView.NO_POSITION) {
                    position = ((CustomGridLayoutManager)this.recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                }

                if (position >= 0) {
                    index = Integer.toString(position);
                }

                row = position;
                if (row >= this.questionsDB.size()) {
                    row = this.questionsDB.size() - 1;
                }

                boolean updated = EntranceLastVisitInfoModelHandler.update(getApplicationContext(),
                        username, this.entranceUniqueId, this.selectedBooklet, this.selectedLesson,
                        index, new Date(), this.showType);

                if (updated) {
                    JsonObject eData = new JsonObject();
                    eData.addProperty("uniqueId", this.entranceUniqueId);
                    eData.addProperty("bookletIndex", this.selectedBooklet);
                    eData.addProperty("bookletString", (String) this.bookletAdapter.getItem(this.selectedBooklet));
                    eData.addProperty("lessonIndex", this.selectedLesson);
                    eData.addProperty("bookletString", this.lessonAdapter.getItem(this.selectedLesson));
                    eData.addProperty("question", this.questionsDB.get(row).number);

                    this.createLog(LogTypeEnum.EntranceLastVisitInfo.getTitle(), eData);

                }
            }
        }
    }

    private void loadLastVisitInfoState() {
        String username = UserDefaultsSingleton.getInstance(this.getApplicationContext()).getUsername();
        this.lastVisitInfo = EntranceLastVisitInfoModelHandler.get(getApplicationContext(),
                username,
                this.entranceUniqueId,
                this.showType);
    }

    private void createLog(String logType, JsonObject extraData) {
        if (username != null) {
            String uniqueId = UUID.randomUUID().toString();
            Date created = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendar.setTime(created);
            Date createdUtc = calendar.getTime();

            try {
                UserLogModelHandler.add(getApplicationContext(), username, uniqueId, createdUtc, logType, extraData);
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
            this.selectedBooklet = 0;

            if (EntranceShowActivity.this.lastVisitInfo != null) {
                if ("Show".equals(EntranceShowActivity.this.lastVisitInfo.showType)) {
                    this.selectedBooklet = EntranceShowActivity.this.lastVisitInfo.bookletIndex;
                }
            }

            bookletAdapter.setItems(items);
            bookletAdapter.notifyDataSetChanged();

            loadLessions(this.selectedBooklet);
        }
    }

    private void loadLessions(int index) {
        if (index >= 0) {
            lessonsDB = new RealmList<EntranceLessonModel>();
            RealmResults<EntranceLessonModel> lessionModel;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                lessionModel = bookletsDB.get(index).lessons.sort("order", Sort.ASCENDING);
            } else {
                lessionModel = bookletsDB.get(index).lessons.sort("order", Sort.DESCENDING);
            }
            lessonsDB.addAll(lessionModel.subList(0, lessionModel.size()));

            lessonAdapter.clear();
            for (EntranceLessonModel item : lessonsDB) {
                lessonAdapter.add(item.title);
            }
            lessonAdapter.notifyDataSetChanged();

            EntranceShowActivity.this.mSectionsPagerAdapter.notifyDataSetChanged();
            tabLayout.setupWithViewPager(EntranceShowActivity.this.mViewPager);

            texButton.setText(buttonTextMaker(bookletAdapter.getItem(index).toString(), false));

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (EntranceShowActivity.this.lastVisitInfo != null) {

                        int lessonIndex1 = EntranceShowActivity.this.lastVisitInfo.lessonIndex;
                        EntranceShowActivity.this.tabLayout.getTabAt(lessonIndex1).select();

                        Handler h1 = new Handler();
                        h1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    int index = Integer.parseInt(EntranceShowActivity.this.lastVisitInfo.index);
                                    if (EntranceShowActivity.this.lastVisitInfo.showType.equals("Show")) {
                                        if (index < EntranceShowActivity.this.entranceShowAdapter.getItemCount()) {
                                            EntranceShowActivity.this.recyclerView.smoothScrollToPosition(index);
                                        } else {
                                            EntranceShowActivity.this.recyclerView.smoothScrollToPosition(0);
                                        }
                                    }
                                } catch (Exception exc) {}
                                EntranceShowActivity.this.lastVisitInfo = null;

                            }
                        }, 5000);

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            EntranceShowActivity.this.tabLayout.getTabAt(0).select();
                        } else {
                            EntranceShowActivity.this.tabLayout.getTabAt(mViewPager.getAdapter().getCount() - 1).select();
                        }
                    }
                }
            }, 1500);
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

    private void finishExam() {
        if (EntranceShowActivity.this.examTimer != null) {
            EntranceShowActivity.this.examTimer.cancel();
            EntranceShowActivity.this.examTimer = null;
        }

        if (EntranceShowActivity.this.lessonExamStructure != null) {
            this.lessonExamStructure.setFinished(new Date());

            if (this.lessonExamStructure.getAnswers().size() > 0) {
                int trueAnswer = 0;
                int falseAnswer = 0;

                HashMap<Integer, Integer> items = EntranceShowActivity.this.lessonExamStructure.getAnswers();
                for (int item:
                        items.keySet()) {
                    EntranceQuestionModel q = EntranceShowActivity.this.questionsDB.get(item);
                    if (q.answer == items.get(item)) {
                        trueAnswer += 1;
                    } else {
                        falseAnswer += 1;
                    }
                }

                this.lessonExamStructure.setTrueAnswer(trueAnswer);
                this.lessonExamStructure.setFalseAnswer(falseAnswer);
                this.lessonExamStructure.setNoAnswer(this.lessonExamStructure.getQCount() -
                    this.lessonExamStructure.getAnswers().size());
                this.lessonExamStructure.setPercentage(Double.valueOf((trueAnswer * 3) + (falseAnswer * -1)) /
                Double.valueOf(this.lessonExamStructure.getQCount() * 3));

                this.loading = AlertClass.showLoadingMessage(this);

                JsonArray answersLocal = new JsonArray();
                for (int item: items.keySet()) {
                    JsonObject t = new JsonObject();
                    t.addProperty("" + this.questionsDB.get(item).number, items.get(item));
                    answersLocal.add(t);
                }

                EntranceLessonExamModelHandler.add(this, username,
                        this.entranceUniqueId, this.lessonExamStructure, new Date(),
                        answersLocal.toString());

                for (int i = 0; i < this.questionsDB.size(); i++) {
                    Integer ans = items.get(i);
                    if (ans != null) {
                        if (Arrays.binarySearch(this.translateAnswer(this.questionsDB.get(i).answer), ans) >= 0) {
                            EntranceQuestionExamStatModelHandler.update(this,
                                    username, entranceUniqueId,
                                    this.questionsDB.get(i).number, 1);
                        } else {
                            EntranceQuestionExamStatModelHandler.update(this,
                                    username, entranceUniqueId,
                                    this.questionsDB.get(i).number, -1);
                        }
                    } else {
                        EntranceQuestionExamStatModelHandler.update(this,
                                username, entranceUniqueId,
                                this.questionsDB.get(i).number, 0);
                    }
                }

                JsonObject eData = new JsonObject();
                eData.addProperty("uniqueId", entranceUniqueId);
                eData.addProperty("bookletOrder", this.bookletsDB.get(this.selectedBooklet).order);
                eData.addProperty("lessonOrder", this.lessonsDB.get(this.selectedLesson).order);
                eData.addProperty("lessonString", this.lessonsDB.get(this.selectedLesson).fullTitle);
                eData.addProperty("started", FormatterSingleton.getInstance().getUTCDateFormatter().format(this.lessonExamStructure.getStarted()));
                eData.addProperty("finished", FormatterSingleton.getInstance().getUTCDateFormatter().format(this.lessonExamStructure.getFinished()));
                eData.addProperty("qCount", this.lessonExamStructure.getQCount());
                eData.addProperty("trueAnswer", trueAnswer);
                eData.addProperty("falseAnswer", falseAnswer);
                eData.addProperty("duration", this.lessonExamStructure.getDuration());
                eData.addProperty("percentage", this.lessonExamStructure.getPercentage());
                eData.add("answers", answersLocal);

                this.createLog(LogTypeEnum.EntranceLessonExamFinished.getTitle(), eData);

                AlertClass.hideLoadingMessage(this.loading);

                EntranceLessonExamResultDialog dialog = new EntranceLessonExamResultDialog(EntranceShowActivity.this);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setListener(EntranceShowActivity.this);
                dialog.show();
                dialog.setupDialog(EntranceShowActivity.this.lessonExamStructure);

            } else {
                EntranceShowActivity.this.cancelLessonExam(true);
            }
        } else {
            EntranceShowActivity.this.cancelLessonExam(false);
        }
    }

    public int[] translateAnswer(int answer) {
        switch (answer) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return new int[] {answer};
            case 5:
                return new int[] {1, 2};
            case 6:
                return new int[] {1, 3};
            case 7:
                return new int[] {1, 4};
            case 8:
                return new int[] {2, 3};
            case 9:
                return new int[] {2, 4};
            case 10:
                return new int[] {3, 4};
        }
        return new int[] {};
    }

    // Delegates

    @Override
    public boolean addTextComment(@NotNull String questionId, int questionNo, int position, @NotNull JsonObject commentData) {
        String username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername();
        EntranceQuestionCommentModel result = EntranceQuestionCommentModelHandler.add(getApplicationContext(),
                this.entranceUniqueId,
                username,
                questionId,
                EntranceCommentType.TEXT.getCode(),
                commentData.toString());

        if (result != null) {
            JsonObject data = new JsonObject();
            data.addProperty("text", commentData.get("text").getAsString());
            data.addProperty("commentId", result.uniqueId);

            JsonObject eData = new JsonObject();
            eData.addProperty("uniqueId", this.entranceUniqueId);
            eData.addProperty("questionNo", questionNo);
            eData.addProperty("commentType", EntranceCommentType.TEXT.getCode());
            eData.add("data", data);

            this.createLog(LogTypeEnum.EntranceCommentCreate.getTitle(), eData);
            if (this.showType == "Starred") {
                this.starredAdapter.notifyItemChanged(position);
            } else {
                this.entranceShowAdapter.notifyItemChanged(position);
            }

            return true;
        }
        return false;
    }

    @Override
    public void cancelComment() {
    }

    @Override
    public boolean deleteComment(@NotNull String questionId, int questionNo, @NotNull String commentId, int position) {
        boolean result = EntranceQuestionCommentModelHandler.removeOneComment(EntranceShowActivity.this,
                username, commentId);

        if (result) {

            JsonObject eData = new JsonObject();
            eData.addProperty("uniqueId", this.entranceUniqueId);
            eData.addProperty("questionNo", questionNo);
            eData.addProperty("commentId", commentId);

            this.createLog(LogTypeEnum.EntranceCommentDelete.getTitle(), eData);

            if (this.showType == "Starred") {
                this.starredAdapter.notifyItemChanged(position);
            } else {
                this.entranceShowAdapter.notifyItemChanged(position);
            }
        }

        return result;
    }

    @Override
    public void showStarredQuestionButtonClicked() {
        if ("Show".equals(showType)) {
            EntranceShowActivity.this.saveLastVisitInfoState();
            showType = "Starred";

            loading = AlertClass.showLoadingMessage(EntranceShowActivity.this);
            loading.show();

            tabLayout.setVisibility(View.GONE);

            recyclerView.setVisibility(View.GONE);
            recyclerViewStar.setVisibility(View.VISIBLE);

            texButton.setEnabled(false);
            texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
            texButton.setText(String.format("سوالات نشان شده (%s)", FormatterSingleton.getInstance().getNumberFormatter().format(EntranceShowActivity.this.globalPairListInteger)));

            recyclerViewStar.addItemDecoration(itemDecoration);
            recyclerViewStar.setAdapter(starredAdapter);
            starredAdapter.setItems(starredQuestions);
            starredAdapter.notifyDataSetChanged();
            recyclerViewStar.setLayoutManager(new StaggeredGridLayoutManager(1, 1));

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertClass.hideLoadingMessage(loading);
                }
            }, 1500);


        } else if ("Starred".equals(showType)) {
            showType = "Show";
            EntranceShowActivity.this.loadLastVisitInfoState();

            tabLayout.setVisibility(View.VISIBLE);

            texButton.setVisibility(View.VISIBLE);
            texButton.setEnabled(true);
            texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));

            recyclerViewStar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new CustomGridLayoutManager(EntranceShowActivity.this));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadLessions(EntranceShowActivity.this.selectedBooklet);
                }
            });

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertClass.hideLoadingMessage(loading);
                }
            }, 1500);
        }
    }

    @Override
    public void defaultShowSegmantChanged(@NotNull EntranceQuestionAnswerState state) {
        if ("Show".equals(showType) || "Starred".equals(showType)) {
            defaultShowType = state;

            if ("Show".equals(showType)) {
                entranceShowAdapter.notifyDataSetChanged();
            } else {
                starredAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void startLessonExam() {
        if(showType.equals("Show"))
            EntranceShowActivity.this.saveLastVisitInfoState();

        showType = "LessonExam";

        loading = AlertClass.showMakeExamMessage(EntranceShowActivity.this);
        loading.show();

        tabLayout.setVisibility(View.GONE);

        texButton.setEnabled(false);
        texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
        texButton.setText("سنجش درس");

        backButton.setVisibility(View.INVISIBLE);
        timerContainer.setVisibility(View.VISIBLE);

        EntranceShowActivity.this.lessonExamStructure = new EntranceLessonExamStructure(
                this.lessonsDB.get(this.selectedLesson).fullTitle,
                this.lessonsDB.get(this.selectedLesson).order,
                this.bookletsDB.get(this.selectedBooklet).order,
                new Date(),
                null,
                this.lessonsDB.get(this.selectedLesson).qCount,
                new HashMap<>(),
                0, 0, 0,
                false,
                this.lessonsDB.get(this.selectedLesson).duration,
                0.0
        );

        this.entranceShowAdapter.notifyDataSetChanged();
        this.recyclerView.smoothScrollToPosition(0);

        Handler h = new Handler();

        TimerTask examTimerTick = new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Integer[] d = diffInHourMinSec(lessonExamStructure.getStarted(),
                                new Date()); // hour, minute, second

                        String s = "";
                        if (d[0] > 0) {
                            s += FormatterSingleton.getInstance().getNumberFormatter().format(d[0])  + " : ";
                        }

                        String m = FormatterSingleton.getInstance().getNumberFormatter().format(d[1]);
                        if (m.length() == 1) {
                            m = "۰" + m;
                        }
                        s += m + " : ";

                        String se = FormatterSingleton.getInstance().getNumberFormatter().format(d[2]);
                        if (se.length() == 1) {
                            se = "۰" + se;
                        }
                        s += se;

                        int factor = (d[0] * 60) + d[1];
                        if (factor >= EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).duration) {
                            EntranceShowActivity.this.timerTextView.setTextColor(ContextCompat.getColor(EntranceShowActivity.this, R.color.colorConcoughOrange));
                        }

                        EntranceShowActivity.this.timerTextView.setText(s);
                    }
                });
            }
        };

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertClass.hideLoadingMessage(loading);

                EntranceShowActivity.this.examTimer = new Timer(false);
                EntranceShowActivity.this.examTimer.schedule(examTimerTick, 0, 1000);

            }
        }, 3000);
    }

    @Override
    public void cancelLessonExam(boolean withLog) {
        if (EntranceShowActivity.this.examTimer != null) {
            EntranceShowActivity.this.examTimer.cancel();
            EntranceShowActivity.this.examTimer = null;
        }

        if (withLog) {
            if (EntranceShowActivity.this.lessonExamStructure != null) {
                EntranceShowActivity.this.lessonExamStructure.setFinished(new Date());

                int trueAnswer = 0;
                int falseAnswer = 0;

                if (EntranceShowActivity.this.lessonExamStructure.getAnswers().size() > 0) {
                    HashMap<Integer, Integer> items = EntranceShowActivity.this.lessonExamStructure.getAnswers();
                    for (int item:
                         items.keySet()) {
                        EntranceQuestionModel q = EntranceShowActivity.this.questionsDB.get(item);
                        if (q.answer == items.get(item)) {
                            trueAnswer += 1;
                        } else {
                            falseAnswer += 1;
                        }
                    }
                }

                JsonObject eData = new JsonObject();
                eData.addProperty("uniqueId", entranceUniqueId);
                eData.addProperty("bookletOrder", bookletsDB.get(selectedBooklet).order);
                eData.addProperty("lessonString", lessonsDB.get(selectedLesson).fullTitle);
                eData.addProperty("lessonOrder", lessonsDB.get(selectedLesson).order);
                eData.addProperty("started", FormatterSingleton.getInstance().getUTCDateFormatter().format(lessonExamStructure.getStarted()));
                eData.addProperty("finished", FormatterSingleton.getInstance().getUTCDateFormatter().format(lessonExamStructure.getFinished()));
                eData.addProperty("qCount", lessonExamStructure.getQCount());
                eData.addProperty("trueAnswer", trueAnswer);
                eData.addProperty("falseAnswer", falseAnswer);
                eData.addProperty("duration", lessonExamStructure.getDuration());

                EntranceShowActivity.this.createLog(LogTypeEnum.EntranceLessonExamCancel.getTitle(), eData);
            }
        }

        EntranceShowActivity.this.lessonExamStructure = null;
        EntranceShowActivity.this.timerTextView.setText(" ");
        EntranceShowActivity.this.examQCountTextView.setText(" ");

        EntranceShowActivity.this.timerContainer.setVisibility(View.GONE);
        EntranceShowActivity.this.backToDefaultContainer.setVisibility(View.GONE);

        EntranceShowActivity.this.tabLayout.setVisibility(View.VISIBLE);
        EntranceShowActivity.this.backButton.setVisibility(View.VISIBLE);

        EntranceShowActivity.this.texButton.setVisibility(View.VISIBLE);
        EntranceShowActivity.this.texButton.setEnabled(true);
        EntranceShowActivity.this.texButton.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));

        EntranceShowActivity.this.showType = "Show";
        EntranceShowActivity.this.loadLastVisitInfoState();

//        recyclerView.setVisibility(View.VISIBLE);
//        recyclerView.setLayoutManager(new CustomGridLayoutManager(EntranceShowActivity.this));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadLessions(EntranceShowActivity.this.selectedBooklet);
            }
        });
    }

    @Override
    public void showLessonExamResult() {
        if (EntranceShowActivity.this.examTimer != null) {
            EntranceShowActivity.this.examTimer.cancel();
            EntranceShowActivity.this.examTimer = null;
        }

        EntranceShowActivity.this.timerTextView.setText(" ");
        EntranceShowActivity.this.examQCountTextView.setText(" ");

        EntranceShowActivity.this.timerContainer.setVisibility(View.GONE);
        EntranceShowActivity.this.backToDefaultContainer.setVisibility(View.VISIBLE);

        EntranceShowActivity.this.showType = "LessonExamResult";
        EntranceShowActivity.this.entranceShowAdapter.notifyDataSetChanged();
        EntranceShowActivity.this.recyclerView.smoothScrollToPosition(0);

        EntranceShowActivity.this.backButton.setVisibility(View.VISIBLE);
    }

    // Normal Adapter

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

    private enum EntranceShowAdapterHolderType {
        NORMAL_QUESTION(1),
        EXAM_HISTORY(2),
        EXAM_QUESTION(3),
        EMPTY_HOLDER(50);

        private final int value;
        private EntranceShowAdapterHolderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private class EntranceShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;
        private RealmList<EntranceQuestionModel> questionModelList;

        public EntranceShowAdapter(Context context) {
            this.context = context;
            this.questionModelList = new RealmList<>();
        }

        public void setItems(RealmList<EntranceQuestionModel> questionModelList) {
            this.questionModelList = questionModelList;
            int i = 0;
            for (EntranceQuestionModel item : questionModelList) {


                if (i > 5) {
                    if (EntranceShowActivity.this.handler != null) {
                        Message msg = EntranceShowActivity.this.handler.obtainMessage(LOAD_IMAGE);
                        msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));

                        Bundle bundle = new Bundle();
                        bundle.putString("IMAGES_STRING", item.images);
                        msg.setData(bundle);

                        EntranceShowActivity.this.handler.sendMessage(msg);

                    }

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
            if (viewType == EntranceShowAdapterHolderType.EXAM_HISTORY.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_entrance_show_chart_holder, parent, false);
                return new EntranceShowChartHolder(view);
            } else if (viewType == EntranceShowAdapterHolderType.NORMAL_QUESTION.getValue()){
                View view = LayoutInflater.from(context).inflate(R.layout.cc_entrance_show_holder1, parent, false);
                return new EntranceShowHolder(view);
            } else if (viewType == EntranceShowAdapterHolderType.EXAM_QUESTION.getValue()) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_entrance_show_lexam_holder, parent, false);
                return new EntranceQuestionExamHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_entrance_show_empty_holder, parent, false);
                return new EmptyHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder.getClass() == EntranceShowChartHolder.class) {
                long count = EntranceLessonExamModelHandler.getExamCount(context.getApplicationContext(),
                        username, EntranceShowActivity.this.entranceUniqueId,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).fullTitle,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).order,
                        EntranceShowActivity.this.bookletsDB.get(EntranceShowActivity.this.selectedBooklet).order);
                Number sum = EntranceLessonExamModelHandler.getPercentageSum(context.getApplicationContext(),
                        username, EntranceShowActivity.this.entranceUniqueId,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).fullTitle,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).order,
                        EntranceShowActivity.this.bookletsDB.get(EntranceShowActivity.this.selectedBooklet).order);

                double average = 0.0;
                if (count > 0) {
                    average = (double)sum / (double)count;
                }

                EntranceLessonExamModel lastExam = EntranceLessonExamModelHandler.getLastExam(context.getApplicationContext(),
                        username, EntranceShowActivity.this.entranceUniqueId,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).fullTitle,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).order,
                        EntranceShowActivity.this.bookletsDB.get(EntranceShowActivity.this.selectedBooklet).order);

                ((EntranceShowChartHolder) holder).setupHolder(context, count, average,
                        lastExam, EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).fullTitle,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).order,
                        EntranceShowActivity.this.bookletsDB.get(EntranceShowActivity.this.selectedBooklet).order);

            } else if (holder.getClass() == EntranceShowHolder.class) {
                final EntranceQuestionModel oneItem = this.questionModelList.get(position - 1);
                ((EntranceShowHolder) holder).setupHolder(oneItem, position);

            } else if (holder.getClass() == EntranceQuestionExamHolder.class) {
                final EntranceQuestionModel oneItem = this.questionModelList.get(position);

                int lqn = 0;
                if(EntranceShowActivity.this.lessonExamStructure != null) {
                    if (EntranceShowActivity.this.lessonExamStructure.getAnswers().containsKey(position)) {
                        lqn = EntranceShowActivity.this.lessonExamStructure.getAnswers().get(position);
                    }
                }
                ((EntranceQuestionExamHolder) holder).setupHolder(oneItem,
                        EntranceShowActivity.this.lessonsDB.get(EntranceShowActivity.this.selectedLesson).qEnd,
                        lqn, oneItem.answer , position);
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
        }

        @Override
        public int getItemCount() {
            if (showType.equals("Show")) {
                return questionModelList.size() + 1;
            } else if (showType.equals("LessonExam") || showType.equals("LessonExamResult")) {
                return questionModelList.size() + 1;
            }
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (showType.equals("Show")) {
                if (position == 0) {
                    return EntranceShowAdapterHolderType.EXAM_HISTORY.getValue();
                } else {
                    return EntranceShowAdapterHolderType.NORMAL_QUESTION.getValue();
                }
            } else {
                if (position == this.questionModelList.size()) {
                    return EntranceShowAdapterHolderType.EMPTY_HOLDER.getValue();
                }
                return EntranceShowAdapterHolderType.EXAM_QUESTION.getValue();
            }
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

            String finalPath = username + "_" + entranceUniqueId;
            File f = new File(EntranceShowActivity.this.getFilesDir(), finalPath);
            if (!f.exists()) {
                finalPath = entranceUniqueId;
            }

            for (JsonElement item : jsonObjects) {
                String imageId = item.getAsJsonObject().get("unique_key").getAsString();
                if (!imageRepo.containsKey(imageId)) {
                    String filePath = finalPath + "/" + imageId;

                    File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
                    if (file.exists()) {
                        try {
                            byte[] buffer = new byte[(int) file.length()];
                            FileInputStream input = new FileInputStream(file);

                            input.read(buffer);

                            byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
                            byte[] i = new AES256JNCryptor(1023).decryptData(decoded, hashKey.toCharArray());

                            imageRepo.put(imageId, i);

                            i = null;

                            buffer = null;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        private class EntranceShowHolder extends RecyclerView.ViewHolder {
            private LinearLayout questionContainer;
            private LinearLayout questionAnswerContainer;

            private ConstraintLayout answerContainer;
            private ConstraintLayout commentsContainer;
            private ConstraintLayout chartContainer;

            private TextView questionNumber;
            private ImageView starImage;
            private ImageView imgPreLoad;
            private ConstraintLayout mainConstraint;
            private ImageView img1;
            private ImageView img2;
            private ImageView img3;
            private LinearLayout showAnswerContainer;
            private TextView showAnswerTextView;
            private ImageView showAnswerImageView;

            private LinearLayout showCommentsContainer;
            private TextView showCommentsTextView;
            private ImageView showCommentsImageView;

            private LinearLayout showStatContainer;
            private TextView showStatTextView;
            private ImageView showStatImageView;

            private Button newCommentButton;
            private Button moreCommentsButton;
            private TextView lastCommentTextView;
            private TextView noCommentTextView;
            private ImageView lastCommentImageView;
            private TextView lastCommentDateTextView;
            private LinearLayout lastCommentContainer;

            private TextView answer;
            private Boolean starred = false;
            private EntranceQuestionModel mEntranceQuestionModel;

            public EntranceShowHolder(View itemView) {
                super(itemView);

                questionNumber = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_questionNumber);
                starImage = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_star);

                imgPreLoad = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_imgPreLoad);
                img1 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img1);
                img2 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img2);
                img3 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img3);

                mainConstraint = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_mainConstrant);
                questionContainer = (LinearLayout)  itemView.findViewById(R.id.ccEntranceShowHolder1I_questionContainer);
                questionAnswerContainer = (LinearLayout)  itemView.findViewById(R.id.ccEntranceShowHolder1I_questionAnswerContainer);

                answerContainer = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_answerContainer);
                commentsContainer = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_commentContainer);
                chartContainer = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_chartContainer);

                showAnswerContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowAnswer);
                showAnswerTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowAnswer);
                showAnswerImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_checkBox);
                answer = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer);

                showCommentsContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowComments);
                showCommentsTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowComments);
                showCommentsImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_comments);

                showStatContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowCharts);
                showStatTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowChart);
                showStatImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_chart);

                lastCommentContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentContainer);
                newCommentButton = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_newCommentButton);
                moreCommentsButton = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_moreCommentsButton);
                lastCommentTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentTextView);
                lastCommentDateTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentDateTextView);
                lastCommentImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentImageView);
                noCommentTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_noCommentTextView);

                lastCommentImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorConcoughGray2));

                questionNumber.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                showAnswerTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

                showCommentsTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                showStatTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

                newCommentButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                moreCommentsButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                lastCommentTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                lastCommentDateTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                noCommentTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());

            }

            public void setImages() {
                insertImage(mEntranceQuestionModel.images);
            }

            public void setupHolder(final EntranceQuestionModel entranceQuestionModel, int position) {

                questionNumber.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.number));
                answer.setText("گزینه " + questionAnswerToString(entranceQuestionModel.answer) + " صحیح است");

                int commentCount = (int) EntranceQuestionCommentModelHandler.getCommentsCount(context,
                        EntranceShowActivity.this.entranceUniqueId,
                        EntranceShowActivity.this.username,
                        entranceQuestionModel.uniqueId);
                showCommentsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(commentCount));

                mEntranceQuestionModel = entranceQuestionModel;

                if (EntranceShowActivity.this.starredIds.contains(entranceQuestionModel.uniqueId)) {
                    starred = true;
                } else {
                    starred = false;
                }
                changeStarredState(starred);

                showAnswerContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EntranceShowActivity.this.showedAnswer.put(entranceQuestionModel.uniqueId,
                                EntranceQuestionAnswerState.ANSWER);
                        changeAnswerContainerState(EntranceQuestionAnswerState.ANSWER);

//                        if (!EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
//                            EntranceShowActivity.this.showedAnsweresIds.add(entranceQuestionModel.uniqueId);
//
//                            answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                            answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                        }
                    }
                });

                showCommentsContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EntranceShowActivity.this.showedAnswer.put(entranceQuestionModel.uniqueId,
                                EntranceQuestionAnswerState.COMMENTS);
                        changeAnswerContainerState(EntranceQuestionAnswerState.COMMENTS);
                    }
                });

                showStatContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EntranceShowActivity.this.showedAnswer.put(entranceQuestionModel.uniqueId,
                                EntranceQuestionAnswerState.STATS);
                        changeAnswerContainerState(EntranceQuestionAnswerState.STATS);
                    }
                });

                newCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EntranceShowNewCommentDialog dialog = new EntranceShowNewCommentDialog(EntranceShowActivity.this);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setListener(EntranceShowActivity.this);
                        dialog.show();
                        dialog.setupDialog(entranceQuestionModel.uniqueId,
                                entranceQuestionModel.number,
                                position);

                    }
                });

                moreCommentsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EntranceShowAllCommentsDialog dialog = new EntranceShowAllCommentsDialog(EntranceShowActivity.this);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setListener(EntranceShowActivity.this);
                        dialog.show();
                        dialog.setupDialog(entranceQuestionModel.uniqueId,
                                entranceUniqueId,
                                entranceQuestionModel.number,
                                position);
                    }
                });

                imgPreLoad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        setImages();
//                        Toast.makeText(EntranceShowActivity.this,"Refresh Clicked", Toast.LENGTH_LONG).show();
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

                EntranceQuestionAnswerState localState = EntranceQuestionAnswerState.None;
                if (EntranceShowActivity.this.showedAnswer.containsKey(entranceQuestionModel.uniqueId)) {
                    localState = EntranceShowActivity.this.showedAnswer.get(entranceQuestionModel.uniqueId);
                }

                if (EntranceShowActivity.this.defaultShowType != EntranceQuestionAnswerState.None) {
                    localState = EntranceShowActivity.this.defaultShowType;
                }

                this.changeAnswerContainerState(localState);

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

                mainConstraint.canScrollVertically(0);

                setImages();
            }

            public void changeStarredState(Boolean state) {
                if (state) {
                    starImage.setImageResource(R.drawable.bookmark_filled);
                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughRedLight));
                } else {
                    starImage.setImageResource(R.drawable.bookmark_empty);
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

                    localBitmaps.add(imageRepo.get(imageId));
                }

                if (localBitmaps.size() >= 1) {
                    try {
                        final byte[] local1 = localBitmaps.get(0);

                        Glide.with(EntranceShowActivity.this)

                                .load(local1)

                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
                                        img1.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img1.setVisibility(View.VISIBLE);
                                        img1.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()
                                .into(img1);

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

            public void setupComment(int commentCount, EntranceQuestionCommentModel lastComment) {
                this.lastCommentContainer.setVisibility(View.GONE);
                this.noCommentTextView.setVisibility(View.GONE);
                this.moreCommentsButton.setVisibility(View.INVISIBLE);

                if (commentCount > 0) {
                    this.showCommentsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(commentCount));
                    if (lastComment != null) {
                        EntranceCommentType t = EntranceCommentType.toType(lastComment.commentType);
                        switch (t) {
                            case TEXT:
                                JsonElement data = new JsonParser().parse(lastComment.commentData);
                                String strComment = data.getAsJsonObject().get("text").getAsString();
                                if (strComment != null) {
                                    this.lastCommentTextView.setText(strComment);
                                }
                                this.lastCommentDateTextView.setText(timeAgoSinceDate(lastComment.created, "fa", false));
                                break;
                            default:
                                break;
                        }
                        lastCommentContainer.setVisibility(View.VISIBLE);
                    }

                    this.moreCommentsButton.setVisibility(View.VISIBLE);
                } else {
                    this.showCommentsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(0));
                    this.noCommentTextView.setVisibility(View.VISIBLE);
                }
            }

            public void changeAnswerContainerState(EntranceQuestionAnswerState state) {
                questionAnswerContainer.setVisibility(View.GONE);
                answerContainer.setVisibility(View.GONE);
                commentsContainer.setVisibility(View.GONE);
                chartContainer.setVisibility(View.GONE);

                if (state != EntranceQuestionAnswerState.None) {
                    questionAnswerContainer.setVisibility(View.VISIBLE);
                }

                int color1 = ContextCompat.getColor(context, R.color.colorConcoughGray5);
                int color2 = ContextCompat.getColor(context, R.color.colorConcoughBlue);
                int color3 = ContextCompat.getColor(context, R.color.colorBlack);

                showAnswerTextView.setTextColor(color2);
                showCommentsTextView.setTextColor(color1);
                showStatTextView.setTextColor(color1);

                showAnswerImageView.setColorFilter(color2);
                showCommentsImageView.setColorFilter(color1);
                showStatImageView.setColorFilter(color1);

                switch (state) {
                    case ANSWER:
                        showAnswerTextView.setTextColor(color3);
                        showAnswerImageView.setColorFilter(color3);
                        answerContainer.setVisibility(View.VISIBLE);
                        break;
                    case COMMENTS:
                        showCommentsTextView.setTextColor(color3);
                        showCommentsImageView.setColorFilter(color3);
                        commentsContainer.setVisibility(View.VISIBLE);
                        int commentCount = (int) EntranceQuestionCommentModelHandler.getCommentsCount(context,
                                EntranceShowActivity.this.entranceUniqueId,
                                EntranceShowActivity.this.username,
                                mEntranceQuestionModel.uniqueId);
                        EntranceQuestionCommentModel lastComment = EntranceQuestionCommentModelHandler.getLastComment(context,
                                EntranceShowActivity.this.entranceUniqueId,
                                EntranceShowActivity.this.username,
                                mEntranceQuestionModel.uniqueId);

                        this.setupComment(commentCount, lastComment);
                        break;
                    case STATS:
                        showStatTextView.setTextColor(color3);
                        showStatImageView.setColorFilter(color3);
                        chartContainer.setVisibility(View.VISIBLE);
                        break;
                    case None:
                        break;
                }
            }
        }

        private class EntranceQuestionExamHolder extends RecyclerView.ViewHolder {
            private TextView questionNumber;
            private ImageView imgPreLoad;
            private ConstraintLayout mainConstraint;
            private ImageView img1;
            private ImageView img2;
            private ImageView img3;
            private Button answer1;
            private Button answer2;
            private Button answer3;
            private Button answer4;
            private ImageView answerEraserImageView;
            private TextView correctAnswerLabel;

            private EntranceQuestionModel mEntranceQuestionModel;

            public EntranceQuestionExamHolder(View itemView) {
                super(itemView);

                questionNumber = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_questionNumber);

                imgPreLoad = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_imgPreLoad);
                img1 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img1);
                img2 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img2);
                img3 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img3);
                answer1 = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer1Button);
                answer2 = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer2Button);
                answer3 = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer3Button);
                answer4 = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer4Button);
                answerEraserImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_eraserImageView);
                correctAnswerLabel = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_correctAnswerTextView);

                mainConstraint = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_mainConstrant);

                questionNumber.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer1.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer2.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer3.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer4.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                correctAnswerLabel.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
            }

            public void setImages() {
                insertImage(mEntranceQuestionModel.images);
            }

            public void setupHolder(final EntranceQuestionModel entranceQuestionModel,
                                    int lastQuestionNo,
                                    int answered,
                                    int correctAnswer,
                                    int position) {
                mEntranceQuestionModel = entranceQuestionModel;

                this.resetAnswerButtons();

                questionNumber.setText("سوال " +
                        FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.number) +
                        " از " +
                        FormatterSingleton.getInstance().getNumberFormatter().format(lastQuestionNo)
                );

                imgPreLoad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        setImages();
//                        Toast.makeText(EntranceShowActivity.this,"Refresh Clicked", Toast.LENGTH_LONG).show();
                    }
                });
                mainConstraint.canScrollVertically(0);
                setImages();

                if (showType.equals("LessonExam")) {
                    this.answerEraserImageView.setVisibility(View.VISIBLE);
                    this.correctAnswerLabel.setVisibility(View.GONE);

                    View.OnClickListener answerClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            resetAnswerButtons();
                            int index = 0;
                            if (view == answer1) {
                                index = 1;
                            } else if (view == answer2) {
                                index = 2;
                            } else if (view == answer3) {
                                index = 3;
                            } else if (view == answer4) {
                                index = 4;
                            }

                            if (index > 0) {
                                setAnswerButton(index, 0);
                            }

                            if (EntranceShowActivity.this.lessonExamStructure != null) {
                                EntranceShowActivity.this.lessonExamStructure.getAnswers().put(position, index);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        EntranceShowActivity.this.examQCountTextView.setText(
                                                FormatterSingleton.getInstance().getNumberFormatter().format(
                                                        EntranceShowActivity.this.lessonExamStructure.getAnswers().size()
                                                ) + "/" + FormatterSingleton.getInstance().getNumberFormatter().format(
                                                        EntranceShowActivity.this.lessonExamStructure.getQCount()
                                                )
                                        );
                                    }
                                });
                            }
                        }
                    };
                    this.answer1.setOnClickListener(answerClickListener);
                    this.answer2.setOnClickListener(answerClickListener);
                    this.answer3.setOnClickListener(answerClickListener);
                    this.answer4.setOnClickListener(answerClickListener);

                    this.answerEraserImageView.setClickable(true);
                    this.answerEraserImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            resetAnswerButtons();

                            if (EntranceShowActivity.this.lessonExamStructure != null) {
                                EntranceShowActivity.this.lessonExamStructure.getAnswers().remove(position);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        EntranceShowActivity.this.examQCountTextView.setText(
                                                FormatterSingleton.getInstance().getNumberFormatter().format(
                                                        EntranceShowActivity.this.lessonExamStructure.getAnswers().size()
                                                ) + "/" + FormatterSingleton.getInstance().getNumberFormatter().format(
                                                        EntranceShowActivity.this.lessonExamStructure.getQCount()
                                                )
                                        );
                                    }
                                });
                            }
                        }
                    });

                    if (answered > 0) {
                        this.setAnswerButton(answered, 0);
                    }
                } else if (showType.equals("LessonExamResult")) {
                    this.answerEraserImageView.setVisibility(View.GONE);
                    this.correctAnswerLabel.setVisibility(View.VISIBLE);

                    this.correctAnswerLabel.setText(questionAnswerToString(correctAnswer) + " ✔");

                    int[] convert = translateAnswer(correctAnswer);
                    int i = Arrays.binarySearch(convert, answered);
                    if (i >= 0) {
                        this.setAnswerButton(answered, 1);
                    } else {
                        this.setAnswerButton(answered, -1);
                    }
                }
            }

            private void resetAnswerButtons() {
                int color = ContextCompat.getColor(context, R.color.colorConcoughGray2);
                this.answer1.setTextColor(color);
                this.answer2.setTextColor(color);
                this.answer3.setTextColor(color);
                this.answer4.setTextColor(color);

                Drawable d = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_2gray_style);
                this.answer1.setBackground(d);
                this.answer2.setBackground(d);
                this.answer3.setBackground(d);
                this.answer4.setBackground(d);
            }

            private void setAnswerButton(int index, int answerState) {
                Drawable d = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_2gray_inverse_style);

                if (answerState == 1) {
                    d = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_greengray_style);
                } else if (answerState == -1) {
                    d = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_full_redgray_style);
                }

                switch (index) {
                    case 1:
                        this.answer1.setBackground(d);
                        this.answer1.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        break;
                    case 2:
                        this.answer2.setBackground(d);
                        this.answer2.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        break;
                    case 3:
                        this.answer3.setBackground(d);
                        this.answer3.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        break;
                    case 4:
                        this.answer4.setBackground(d);
                        this.answer4.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        break;
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

                    localBitmaps.add(imageRepo.get(imageId));
                }

                if (localBitmaps.size() >= 1) {
                    try {
                        final byte[] local1 = localBitmaps.get(0);

                        Glide.with(EntranceShowActivity.this)

                                .load(local1)

                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
                                        img1.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img1.setVisibility(View.VISIBLE);
                                        img1.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()
                                .into(img1);

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

        private class EntranceShowChartHolder extends RecyclerView.ViewHolder {
            private PieChart statContainer;
            private TextView averageLabelTextView;
            private TextView averageTextView;
            private TextView examCountLabelTextView;
            private TextView examCountTextView;
            private TextView examHistoryLabelTextView;
            private LinearLayout examHistoryContainer;
            private Button newExamButton;

            private long examCount = 0;

            public EntranceShowChartHolder(View itemView) {
                super(itemView);

                statContainer = (PieChart) itemView.findViewById(R.id.itemESCH_statContainer);
                averageLabelTextView = (TextView) itemView.findViewById(R.id.itemESCH_averageLabelTextView);
                averageTextView = (TextView) itemView.findViewById(R.id.itemESCH_averageTextView);
                examCountLabelTextView = (TextView) itemView.findViewById(R.id.itemESCH_examCountLabelTextView);
                examCountTextView = (TextView) itemView.findViewById(R.id.itemESCH_examCountTextView);
                examHistoryLabelTextView = (TextView) itemView.findViewById(R.id.itemESCH_examsHistoryTextView);
                examHistoryContainer = (LinearLayout) itemView.findViewById(R.id.itemESCH_examsHistoryContainer);
                newExamButton = (Button) itemView.findViewById(R.id.itemESCH_newExamButton);

                averageLabelTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                averageTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                examCountLabelTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                examCountTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                examHistoryLabelTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                newExamButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

                statContainer.setTransparentCircleRadius(40f);
                statContainer.setHoleRadius(40f);
                statContainer.setTransparentCircleColor(ContextCompat.getColor(context, android.R.color.transparent));

                statContainer.setDescription("");

                statContainer.animateXY(1, 1);
                statContainer.getLegend().setEnabled(false);
            }

            public void setupHolder(Context context, long examCount, double examAverage,
                                    @Nullable EntranceLessonExamModel lastExam, String lessonTitle,
                                    int lessonOrder, int bookletOrder) {
                //double avg = (double)(Math.round((Math.round(examAverage * 10000) / 100)) * 10) / 10;
                this.averageTextView.setText(FormatterSingleton.getInstance().getDecimalNumberFormatter().format(examAverage * 100) + " %");
                this.examCountTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(examCount));

                this.examCount = examCount;

                if (examCount == 0) {
                    this.statContainer.setTouchEnabled(false);
                    this.statContainer.setCenterTextTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                    this.statContainer.setCenterTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                    this.statContainer.setCenterTextSize(12);
                    this.statContainer.setCenterText("بدون\nسنجش");

                    this.setDefaultChart(context);
                } else {
                    if (lastExam != null) {
                        this.statContainer.setTouchEnabled(true);
                        this.statContainer.setOnChartGestureListener(new OnChartGestureListener() {
                            @Override
                            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

                            }

                            @Override
                            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

                            }

                            @Override
                            public void onChartLongPressed(MotionEvent me) {

                            }

                            @Override
                            public void onChartDoubleTapped(MotionEvent me) {

                            }

                            @Override
                            public void onChartSingleTapped(MotionEvent me) {
                                // TODO: create dialog to show answers and pie chart
                            }

                            @Override
                            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

                            }

                            @Override
                            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

                            }

                            @Override
                            public void onChartTranslate(MotionEvent me, float dX, float dY) {

                            }
                        });

                        String[] labels = new String[] {"", "", ""};
                        float[] data = new float[]{
                                lastExam.trueAnswer,
                                lastExam.falseAnswer,
                                lastExam.noAnswer
                        };

                        this.statContainer.setCenterTextTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                        this.statContainer.setCenterTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                        this.statContainer.setCenterTextSize(12);
                        this.statContainer.setCenterText("آخرین\nسنجش");

                        this.setChart(labels, data);
                    }
                }

                newExamButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EntranceNewLessonExamDialog dialog = new EntranceNewLessonExamDialog(EntranceShowActivity.this);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setListener(EntranceShowActivity.this);
                        dialog.show();
                        dialog.setupDialog();
                    }
                });
            }

            public void setDefaultChart(Context context) {
                ArrayList<Entry> dataEntries = new ArrayList<>();

                Entry dataEntry = new Entry(1, 0);
                dataEntries.add(dataEntry);

                PieDataSet chartDataSet = new PieDataSet(dataEntries, "");
                chartDataSet.setValueFormatter(new ChartValueNumberFormatter());
                chartDataSet.setSelectionShift(0.0f);
                chartDataSet.setValueTextSize(12);
                chartDataSet.setValueTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                chartDataSet.setColors(new int[]{R.color.colorConcoughGray2}, context);

                PieData chartData = new PieData(new String[]{""}, chartDataSet);
                this.statContainer.setData(chartData);
            }

            public void setChart(String[] dataPoints, float[] values) {
                ArrayList<Entry> dataEntries = new ArrayList<>();

                for(int i=0; i < dataPoints.length; i++) {
                    Entry dataEntry = new Entry(values[i], i);
                    dataEntries.add(dataEntry);
                }

                PieDataSet chartDataSet = new PieDataSet(dataEntries, "");
                chartDataSet.setValueFormatter(new ChartValueNumberFormatter());
                chartDataSet.setSelectionShift(0.0f);
                chartDataSet.setValueTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                chartDataSet.setValueTextSize(14);
                chartDataSet.setValueTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                chartDataSet.setColors(new int[]{R.color.colorConcoughGreen,
                R.color.colorConcoughRedLight, R.color.colorConcoughOrange}, context);

                PieData chartData = new PieData(dataPoints, chartDataSet);
                this.statContainer.setData(chartData);

            }
        }

        private class EmptyHolder extends RecyclerView.ViewHolder {
            public EmptyHolder(View itemView) {
                super(itemView);
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
                itemHolder.setupHolder(mPairList.get(position).second, position);
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
                tvCount = (TextView) itemView.findViewById(headerShowStarred_countEntrance);

                tv.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                tvCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());


            }


            public void setupHolder(Object item) {
                Lessions castItem = (Lessions) item;
                tv.setText(castItem.lessionTitle);
                tvCount.setText(String.format("%s", FormatterSingleton.getInstance().getNumberFormatter().format(castItem.count)));
            }


        }


        public class ListHolder extends RecyclerView.ViewHolder {
            private LinearLayout questionContainer;
            private LinearLayout questionAnswerContainer;
            private ConstraintLayout answerContainer;
            private ConstraintLayout commentsContainer;
            private ConstraintLayout chartContainer;

            private TextView questionNumber;
            private ImageView starImage;
            private ImageView imgPreLoad;
            private ImageView img1;
            private ImageView img2;
            private ImageView img3;
            private LinearLayout showAnswerContainer;
            private TextView showAnswerTextView;
            private ImageView showAnswerImageView;

            private LinearLayout showCommentsContainer;
            private TextView showCommentsTextView;
            private ImageView showCommentsImageView;

            private LinearLayout showStatContainer;
            private TextView showStatTextView;
            private ImageView showStatImageView;

            private Button newCommentButton;
            private Button moreCommentsButton;
            private TextView lastCommentTextView;
            private TextView noCommentTextView;
            private ImageView lastCommentImageView;
            private TextView lastCommentDateTextView;
            private LinearLayout lastCommentContainer;

            private TextView answer;
            private Boolean starred = false;
            private EntranceQuestionModel mEntranceQuestionModel;

            public ListHolder(View itemView) {
                super(itemView);


                questionNumber = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_questionNumber);
                starImage = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_star);

                imgPreLoad = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_imgPreLoad);
                img1 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img1);
                img2 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img2);
                img3 = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_img3);

                questionContainer = (LinearLayout)  itemView.findViewById(R.id.ccEntranceShowHolder1I_questionContainer);
                questionAnswerContainer = (LinearLayout)  itemView.findViewById(R.id.ccEntranceShowHolder1I_questionAnswerContainer);

                answerContainer = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_answerContainer);
                commentsContainer = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_commentContainer);
                chartContainer = (ConstraintLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_chartContainer);

                showAnswerContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowAnswer);
                showAnswerTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowAnswer);
                showAnswerImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_checkBox);
                answer = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_answer);

                showCommentsContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowComments);
                showCommentsTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowComments);
                showCommentsImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_comments);

                showStatContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_linearShowCharts);
                showStatTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_textViewClickShowChart);
                showStatImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_chart);

                lastCommentContainer = (LinearLayout) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentContainer);
                newCommentButton = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_newCommentButton);
                moreCommentsButton = (Button) itemView.findViewById(R.id.ccEntranceShowHolder1I_moreCommentsButton);
                lastCommentTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentTextView);
                lastCommentDateTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentDateTextView);
                lastCommentImageView = (ImageView) itemView.findViewById(R.id.ccEntranceShowHolder1I_lastCommentImageView);
                noCommentTextView = (TextView) itemView.findViewById(R.id.ccEntranceShowHolder1I_noCommentTextView);

                lastCommentImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorConcoughGray2));

                questionNumber.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                showAnswerTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                answer.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

                showCommentsTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                showStatTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

                newCommentButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                moreCommentsButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                lastCommentTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                lastCommentDateTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                noCommentTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
            }


            public void setupHolder(Object item, int position) {
                final EntranceQuestionModel entranceQuestionModel = (EntranceQuestionModel) item;
                this.mEntranceQuestionModel = entranceQuestionModel;

//                imgPreLoad.setVisibility(View.VISIBLE);

                questionNumber.setText(FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.number));

                answer.setText("گزینه " + questionAnswerToString(entranceQuestionModel.answer) + " صحیح است");
//                answer.setText("گزینه " + FormatterSingleton.getInstance().getNumberFormatter().format(entranceQuestionModel.answer) + " صحیح است");

                int commentCount = (int) EntranceQuestionCommentModelHandler.getCommentsCount(context,
                        EntranceShowActivity.this.entranceUniqueId,
                        EntranceShowActivity.this.username,
                        entranceQuestionModel.uniqueId);
                showCommentsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(commentCount));

                if (EntranceShowActivity.this.starredIds.contains(entranceQuestionModel.uniqueId)) {
                    starred = true;
                } else {
                    starred = false;
                }

                changeStarredState(starred);

                showAnswerContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if (!EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
//                            EntranceShowActivity.this.showedAnsweresIds.add(entranceQuestionModel.uniqueId);
//
//                            answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                            answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray4));
//                        }

                        EntranceShowActivity.this.showedAnswer.put(entranceQuestionModel.uniqueId,
                                EntranceQuestionAnswerState.ANSWER);
                        questionAnswerContainer.setVisibility(View.VISIBLE);
                        answerContainer.setVisibility(View.VISIBLE);
                        commentsContainer.setVisibility(View.GONE);
                        chartContainer.setVisibility(View.GONE);
                    }
                });

                newCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EntranceShowNewCommentDialog dialog = new EntranceShowNewCommentDialog(EntranceShowActivity.this);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setListener(EntranceShowActivity.this);
                        dialog.show();
                        dialog.setupDialog(entranceQuestionModel.uniqueId,
                                entranceQuestionModel.number,
                                position);

                    }
                });

                moreCommentsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EntranceShowAllCommentsDialog dialog = new EntranceShowAllCommentsDialog(EntranceShowActivity.this);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setListener(EntranceShowActivity.this);
                        dialog.show();
                        dialog.setupDialog(entranceQuestionModel.uniqueId,
                                entranceUniqueId,
                                entranceQuestionModel.number,
                                position);
                    }
                });

                imgPreLoad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NotNull View v) {
                        setImages();
//                        Toast.makeText(EntranceShowActivity.this,"Refresh Clicked", Toast.LENGTH_LONG).show();
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

                EntranceQuestionAnswerState localState = EntranceQuestionAnswerState.None;
                if (EntranceShowActivity.this.showedAnswer.containsKey(entranceQuestionModel.uniqueId)) {
                    localState = EntranceShowActivity.this.showedAnswer.get(entranceQuestionModel.uniqueId);
                }

                if (EntranceShowActivity.this.defaultShowType != EntranceQuestionAnswerState.None) {
                    localState = EntranceShowActivity.this.defaultShowType;
                }

                this.changeAnswerContainerState(localState);

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
                    starImage.setImageResource(R.drawable.bookmark_filled);
                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughRedLight));
                } else {
                    starImage.setImageResource(R.drawable.bookmark_empty);
                    starImage.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray2));
                }
            }

            public void setupComment(int commentCount, EntranceQuestionCommentModel lastComment) {
                this.lastCommentContainer.setVisibility(View.GONE);
                this.noCommentTextView.setVisibility(View.GONE);
                this.moreCommentsButton.setVisibility(View.INVISIBLE);

                if (commentCount > 0) {
                    this.showCommentsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(commentCount));
                    if (lastComment != null) {
                        EntranceCommentType t = EntranceCommentType.toType(lastComment.commentType);
                        switch (t) {
                            case TEXT:
                                JsonElement data = new JsonParser().parse(lastComment.commentData);
                                String strComment = data.getAsJsonObject().get("text").getAsString();
                                if (strComment != null) {
                                    this.lastCommentTextView.setText(strComment);
                                }
                                this.lastCommentDateTextView.setText(timeAgoSinceDate(lastComment.created, "fa", false));
                                break;
                            default:
                                break;
                        }
                        lastCommentContainer.setVisibility(View.VISIBLE);
                    }

                    this.moreCommentsButton.setVisibility(View.VISIBLE);
                } else {
                    this.showCommentsTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(0));
                    this.noCommentTextView.setVisibility(View.VISIBLE);
                }
            }

            public void changeAnswerContainerState(EntranceQuestionAnswerState state) {
                questionAnswerContainer.setVisibility(View.GONE);
                answerContainer.setVisibility(View.GONE);
                commentsContainer.setVisibility(View.GONE);
                chartContainer.setVisibility(View.GONE);

                if (state != EntranceQuestionAnswerState.None) {
                    questionAnswerContainer.setVisibility(View.VISIBLE);
                }

                int color1 = ContextCompat.getColor(context, R.color.colorConcoughGray5);
                int color2 = ContextCompat.getColor(context, R.color.colorConcoughBlue);
                int color3 = ContextCompat.getColor(context, R.color.colorBlack);

                showAnswerTextView.setTextColor(color2);
                showCommentsTextView.setTextColor(color1);
                showStatTextView.setTextColor(color1);

                showAnswerImageView.setColorFilter(color2);
                showCommentsImageView.setColorFilter(color1);
                showStatImageView.setColorFilter(color1);

                switch (state) {
                    case ANSWER:
                        showAnswerTextView.setTextColor(color3);
                        showAnswerImageView.setColorFilter(color3);
                        answerContainer.setVisibility(View.VISIBLE);
                        break;
                    case COMMENTS:
                        showCommentsTextView.setTextColor(color3);
                        showCommentsImageView.setColorFilter(color3);
                        commentsContainer.setVisibility(View.VISIBLE);

                        int commentCount = (int) EntranceQuestionCommentModelHandler.getCommentsCount(context,
                                EntranceShowActivity.this.entranceUniqueId,
                                EntranceShowActivity.this.username,
                                mEntranceQuestionModel.uniqueId);
                        EntranceQuestionCommentModel lastComment = EntranceQuestionCommentModelHandler.getLastComment(context,
                                EntranceShowActivity.this.entranceUniqueId,
                                EntranceShowActivity.this.username,
                                mEntranceQuestionModel.uniqueId);

                        this.setupComment(commentCount, lastComment);

                        break;
                    case STATS:
                        showStatTextView.setTextColor(color3);
                        showStatImageView.setColorFilter(color3);
                        chartContainer.setVisibility(View.VISIBLE);
                        break;
                    case None:
                        break;
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

                    localBitmaps.add(imageRepo.get(imageId));
                }

                if (localBitmaps.size() >= 1) {
                    try {
                        final byte[] local1 = localBitmaps.get(0);

                        Glide.with(EntranceShowActivity.this)

                                .load(local1)

                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        imgPreLoad.setVisibility(View.GONE);
                                        img1.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img1.setVisibility(View.VISIBLE);
                                        img1.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })

                                .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
                                .crossFade()
                                .fitCenter()
                                .into(img1);


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
                                        //TODO: V2> On Error Loading Image , need to refresh button for any unloaded image
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
            TextView tvCount = (TextView) header.findViewById(headerShowStarred_countEntrance);
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

            String finalPath = username + "_" + entranceUniqueId;
            File f = new File(EntranceShowActivity.this.getFilesDir(), finalPath);
            if (!f.exists()) {
                finalPath = entranceUniqueId;
            }

            for (JsonElement item : jsonObjects) {
                String imageId = item.getAsJsonObject().get("unique_key").getAsString();
                if (!imageRepo.containsKey(imageId)) {
                    String filePath = finalPath + "/" + imageId;

                    File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
                    if (file.exists()) {
                        try {
                            byte[] buffer = new byte[(int) file.length()];
                            FileInputStream input = new FileInputStream(file);


                            input.read(buffer);

                            byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
                            byte[] i = new AES256JNCryptor(1023).decryptData(decoded, hashKey.toCharArray());

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
                        //   return true;
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

}
