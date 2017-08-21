package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.concough.android.models.EntranceBookletModel;
import com.concough.android.models.EntranceLessonModel;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.EntranceOpenedCountModelHandler;
import com.concough.android.models.EntranceQuestionModel;
import com.concough.android.models.EntranceQuestionStarredModelHandler;
import com.concough.android.models.EntranceStarredQuestionModel;
import com.concough.android.models.UserLogModelHandler;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.LogTypeEnum;
import com.concough.android.utils.MD5Digester;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.cryptonode.jncryptor.AES256JNCryptorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.concough.android.settings.ConstantsKt.getSECRET_KEY;

//import com.bumptech.glide.load.resource.drawable.GlideDrawable;


public class EntranceShowActivity extends AppCompatActivity implements Handler.Callback {
    private final static String TAG = "EntranceShowActivity";


    private final static String ENTRANCE_UNIQUE_ID_KEY = "entranceUniqueId";
    private final static String SHOW_TYPE_KEY = "showType";
    private static final String HANDLE_THREAD_NAME = "Concough-EntranceShowActivity";
    private static final int LOAD_IMAGE = 0;


    private HandlerThread handlerThread = null;
    private Handler handler = null;

    private HandlerThread handlerThread2 = null;
    private Handler handler2 = null;

    private HandlerThread handlerThread3 = null;
    private Handler handler3 = null;


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

    private Boolean showAllAnswers = false;
    private ArrayList<String> bookletList;
    private ArrayList<String> starredIds = new ArrayList<>();
    private ArrayList<String> showedAnsweresIds = new ArrayList<>();
    private ArrayAdapter<String> lessonAdapter;
    private DialogAdapter bookletAdapter;
    private EntranceShowAdapter entranceShowAdapter;


    private CustomTabLayout tabLayout;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private DialogPlus dialog;
    private Button texButton;
    private RecyclerView recyclerView;


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


        this.handlerThread2 = new HandlerThread(HANDLE_THREAD_NAME + "2");
        if (this.handlerThread2 != null) {
            this.handlerThread2.start();
        }

        Looper looper2 = this.handlerThread2.getLooper();
        if (looper2 != null) {
            this.handler2 = new Handler(looper2, this);
        }

        this.handlerThread3 = new HandlerThread(HANDLE_THREAD_NAME + "3");
        if (this.handlerThread3 != null) {
            this.handlerThread3.start();
        }

        Looper looper3 = this.handlerThread3.getLooper();
        if (looper3 != null) {
            this.handler3 = new Handler(looper3, this);
        }

        recyclerView = (RecyclerView) findViewById(R.id.entranceShowA_recycleEntranceShow);
        entranceShowAdapter = new EntranceShowAdapter(this);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,1));
        recyclerView.setAdapter(entranceShowAdapter);

        entranceUniqueId = getIntent().getStringExtra(ENTRANCE_UNIQUE_ID_KEY);
        showType = getIntent().getStringExtra(SHOW_TYPE_KEY);

        username = UserDefaultsSingleton.getInstance(getApplicationContext()).getUsername(getApplicationContext());

        if (entranceUniqueId.equals("")) {
            finish();
        }

        lessonAdapter = new ArrayAdapter<String>(this, R.layout.cc_archive_listitem_tabbar);

        bookletAdapter = new DialogAdapter(EntranceShowActivity.this, bookletList);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
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
                Toast.makeText(EntranceShowActivity.this, "index="+index+"", Toast.LENGTH_SHORT).show();
                loadQuestions(index);
                if (showType.equals("Show")) {
                    EntranceShowActivity.this.entranceShowAdapter.setItems(questionsDB);
                    EntranceShowActivity.this.entranceShowAdapter.notifyDataSetChanged();
                    // TODO : show data on recycle view
                }
//                entranceShowAdapter.notifyDataSetChanged();

                Log.d(TAG, "onTabSelected: ");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        int toolBarHeight = toolbar.getLayoutParams().height;

//        ArrayList<String> itemstest = new ArrayList<>();
//        itemstest.add("عنوان 1");
//        itemstest.add("عنوان 2");
//        itemstest.add("عنوان 21");
//

        bookletAdapter.setItems(new ArrayList<String>());

        dialog = DialogPlus.newDialog(this)
                .setAdapter(bookletAdapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {

                        dialog.dismiss();
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

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.cc_archive_actionbar, null);

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

        loadEntranceDB();
        loadBooklets();
        loadStarredQuestion();

        EntranceOpenedCountModelHandler.update(getApplicationContext(), username, entranceUniqueId, showType);

        JsonObject eData = new JsonObject();
        eData.addProperty("uniqueId", entranceUniqueId);
        if (showType.equals("Show")) {
            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceShowNormal.getTitle(), eData);
        } else if (showType.equals("Starred")) {
            EntranceShowActivity.this.createLog(LogTypeEnum.EntranceShowStarred.getTitle(), eData);

        }


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


            final Handler handler25 = new Handler();
            handler25.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    bookletAdapter.setItems(items);
                    bookletAdapter.notifyDataSetChanged();

                }
            }, 500);


            texButton.setText(buttonTextMaker(items.get(0), false));
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
            EntranceShowActivity.this.tabLayout.getTabAt(index).select();
            loadQuestions(index);
            if (showType.equals("Show")) {
                EntranceShowActivity.this.entranceShowAdapter.setItems(questionsDB);
                EntranceShowActivity.this.entranceShowAdapter.notifyDataSetChanged();
                // TODO : show data on recycle view
            }


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
//            for (EntranceQuestionModel item : questionModelList) {
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
//                            if (EntranceShowActivity.this.handler2 != null) {
//                                Message msg = EntranceShowActivity.this.handler2.obtainMessage(LOAD_IMAGE);
//                                msg.setTarget(new Handler(EntranceShowActivity.this.getMainLooper()));
//
//                                Bundle bundle = new Bundle();
//                                bundle.putString("IMAGES_STRING", item.images);
//                                msg.setData(bundle);
//
//                                EntranceShowActivity.this.handler2.sendMessage(msg);
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

            //((EntranceShowHolder) holder).setImages();
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
                           // byte[] i = new AES256JNCryptor().decryptData(decoded, hashKey.toCharArray());

                            ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
                            AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());

                            byte[] i = convertStreamToByteArray(cryptStream);


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

                img1.setScaleType(ImageView.ScaleType.FIT_XY);
                img1.setAdjustViewBounds(true);

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
                answer.setVisibility(View.INVISIBLE);
                answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughBlue));
                answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughBlue));
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

                            answerLabel.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGray4));
                            answerLabelCheckbox.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorConcoughGray4));
                        }
                    }
                });

                starImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        starred = !starred;
                        changeStarredState(starred);
                        addStarQuestionId(entranceQuestionModel.uniqueId, entranceQuestionModel.number, starred);
                    }
                });

                if (EntranceShowActivity.this.showedAnsweresIds.contains(entranceQuestionModel.uniqueId)) {
                    answer.setVisibility(View.VISIBLE);
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
//                img1.setImageDrawable(null);
//                img2.setImageDrawable(null);
//                img3.setImageDrawable(null);

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
                    if (!imageRepo.containsKey(imageId)) {
                        String filePath = entranceUniqueId + "/" + imageId;

                        File file = new File(EntranceShowActivity.this.getFilesDir(), filePath);
                        if (file.exists()) {
                            try {
                                byte[] buffer = new byte[(int) file.length()];
                                FileInputStream input = new FileInputStream(file);


                                input.read(buffer);

                                byte[] decoded = Base64.decode(buffer, Base64.DEFAULT);
                                //byte[] i = new AES256JNCryptor().decryptData(decoded, hashKey.toCharArray());

                                ByteArrayInputStream stream = new ByteArrayInputStream(decoded);
                                AES256JNCryptorInputStream cryptStream = new AES256JNCryptorInputStream(stream, hashKey.toCharArray());

                                byte[] i = convertStreamToByteArray(cryptStream);

//                                RNCryptorNative rncryptor = new RNCryptorNative();
//                                String data = rncryptor.decrypt(new String(buffer), hashKey);
//                                byte[] i = data.getBytes();

                                localBitmaps.add(i);
                                imageRepo.put(imageId, i);

                                i = null;
//                                data = null;
//                                rncryptor = null;
                                buffer = null;
                                //decoded = null;
                                input.close();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        localBitmaps.add(imageRepo.get(imageId));
                    }
                }

                //img1.setVisibility(View.INVISIBLE);
                //img2.setVisibility(View.INVISIBLE);
                //img3.setVisibility(View.INVISIBLE);

                if (localBitmaps.size() >= 1) {
                    try {

                        // Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(0), 0, localBitmaps.get(0).length);

                        Glide.with(EntranceShowActivity.this)

                                .load(localBitmaps.get(0))

//                                .placeholder(R.drawable.no_image)
                                .listener(new RequestListener<byte[], GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                        imgPreLoad.setVisibility(View.GONE);
//                                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        img1.setVisibility(View.VISIBLE);
                                        img1.setAdjustViewBounds(true);

                                        return false;
                                    }

                                })
                                //.error(R.drawable.ic_thumb_placeholder)
                                // .transform(new CircleTransform(this))
                                //.override(mWidth,mHeight)
                                .into(img1);


                        //img1.setImageBitmap(bitmap);
//                        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        img1.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img1.setVisibility(View.VISIBLE);
                        img1.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (localBitmaps.size() >= 2) {
                    try {
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(1), 0, localBitmaps.get(1).length);
//                        img2.setImageBitmap(localBitmaps.get(1));
                        img2.setVisibility(View.VISIBLE);
                        img2.setAdjustViewBounds(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (localBitmaps.size() >= 3) {
                    try {
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(localBitmaps.get(2), 0, localBitmaps.get(2).length);
//                        img3.setImageBitmap(localBitmaps.get(2));
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


}
