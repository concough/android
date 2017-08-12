package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.extensions.RotateViewExtensions;
import com.concough.android.rest.ArchiveRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
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

public class ArchiveActivity extends AppCompatActivity {
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

    private Integer currentPositionDropDown;

    private CustomTabLayout tabLayout;

    private JsonElement cacheTypes;
    private ArrayList<JsonElement> cacheGroups;
    private ArrayList<JsonElement> cacheSets;

    private ArchiveEsetDetailStruct mArchiveEsetDetailStruct;
    private ArchiveEsetStructs mArchiveEsetStructs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        mArchiveEsetDetailStruct = new ArchiveEsetDetailStruct();
        mArchiveEsetStructs = new ArchiveEsetStructs();


        typeList = new ArrayList<String>();
        dropDownJsonElement = new ArrayList<JsonElement>();
        tabbarJsonElement = new ArrayList<JsonElement>();
        adapterTab = new ArrayAdapter<String>(this, R.layout.cc_archive_listitem_tabbar);

        recycleView = (RecyclerView) findViewById(R.id.archiveA_recycleDetail);
        adapterSet = new GetSetsAdapter(this, new ArrayList<JsonElement>());
        recycleView.setAdapter(adapterSet);
        recycleView.setLayoutManager(new LinearLayoutManager(this));

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
                int id = ArchiveActivity.this.tabbarJsonElement.get(index).getAsJsonObject().get("id").getAsInt();
                getSets(id);
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
        adapter = new DialogAdapter(getApplicationContext(), typeList);
        dialog = DialogPlus.newDialog(this)
                .setAdapter(adapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        currentPositionDropDown = position;
                        ArchiveActivity.this.changeTypeIndex(position);
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogPlus dialog) {
                        texButton.setText(buttonTextMaker(typeList.get(ArchiveActivity.this.currentPositionDropDown).toString(), false));
                    }
                })
                .setCancelable(true)
                .setGravity(Gravity.TOP)
                .setOutMostMargin(0, toolBarHeight, 0, 0)
                .create();


        final ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setElevation(2);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.cc_archive_actionbar, null);

        texButton = (Button) mCustomView
                .findViewById(R.id.actionBarL_dropDown);
        texButton.setText("");

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

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

        refreshButton = (Button) mCustomView.findViewById(R.id.actionBarL_refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTypes();
                RotateViewExtensions.buttonRotateStart(refreshButton, getApplicationContext());
            }
        });


        getTypes();


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
        ArchiveActivity.this.texButton.setText(buttonTextMaker((String) ArchiveActivity.this.adapter.getItem(index), false));
        new GetGroupsTask().execute(ArchiveActivity.this.dropDownJsonElement.get(index).getAsJsonObject().get("id").getAsInt());
    }

    private void changeGroupIndex(final int index) {
//        ArchiveActivity.this.mViewPager.setCurrentItem(0);
        ArchiveActivity.this.mViewPager.setCurrentItem(index);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        ArchiveActivity.this.tabLayout.getTabAt(index).select();
                        Integer i = ArchiveActivity.this.tabbarJsonElement.get(index).getAsJsonObject().get("id").getAsInt();
                       //ArchiveActivity.this.currentPositionDropDown = i;
                        getSets(i);

                    }
                }, 100);


    }


    // Asyncs

    private void getTypes() {

        new GetTypesTask().execute();
    }

    private void getTab(int typeId) {
        new GetGroupsTask().execute(typeId);
    }

    private void getSets(int groupId) {
        new GetSetsTask().execute(groupId);
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        RotateViewExtensions.buttonRotateStop(refreshButton, getApplicationContext());

                    }
                }, 1500);

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
                            if (httpErrorType == HTTPErrorType.Success) {
                                if (jsonObject != null) {

                                    try {
                                        HashMap<Integer, String> jsonHashMap = new HashMap<Integer, String>();
                                        JsonObject json = jsonObject.getAsJsonObject();
                                        JsonArray leaders = json.getAsJsonArray("record");


                                        ArrayList<JsonElement> localListJson = new ArrayList<JsonElement>();
                                        ArrayList<String> localList = new ArrayList<String>();
                                        for (JsonElement je : leaders) {
                                            String j = je.getAsJsonObject().get("title").getAsString();
                                            if (j.toString() != null) {
                                                int i = je.getAsJsonObject().get("id").getAsInt();
                                                jsonHashMap.put(i, j);
                                                localListJson.add(je);
                                                localList.add(j);
                                            }
                                        }

                                        ArchiveActivity.this.currentPositionDropDown = 0;

                                        ArchiveActivity.this.adapter.addAll(localList);
                                        ArchiveActivity.this.dropDownJsonElement.addAll(localListJson);
                                        ArchiveActivity.this.adapter.notifyDataSetChanged();
                                        ArchiveActivity.this.changeTypeIndex(0);


                                    } catch (Exception e) {

                                    }

                                }
                            } else if (httpErrorType == HTTPErrorType.Refresh) {
                                new GetTypesTask().execute(params);
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
                                    if (httpErrorType == HTTPErrorType.Success) {
                                        if (jsonObject != null) {

                                            try {
                                                HashMap<Integer, String> jsonHashMap = new HashMap<Integer, String>();
                                                JsonObject json = jsonObject.getAsJsonObject();
                                                JsonArray leaders = json.getAsJsonArray("record");


                                                ArrayList<JsonElement> localListJson = new ArrayList<JsonElement>();
                                                ArrayList<String> localList = new ArrayList<String>();
                                                for (JsonElement je : leaders) {
                                                    String j = je.getAsJsonObject().get("title").getAsString();
                                                    if (j.toString() != null) {
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

                                                ArchiveActivity.this.mSectionsPagerAdapter.notifyDataSetChanged();
                                                tabLayout.setupWithViewPager(ArchiveActivity.this.mViewPager);

                                                ArchiveActivity.this.changeGroupIndex(0);

                                            } catch (Exception e) {
                                                Log.d(TAG, "run: ");
                                            }

                                        }

                                    } else if (httpErrorType == HTTPErrorType.Refresh) {
                                        new GetGroupsTask().execute(firstIndexOfTypes);
                                    } else {
                                        // TODO: show error with msgType = "HTTPError" and error
                                    }
                                }
                            });
                            return null;
                        }
                    }
                    , new Function1<NetworkErrorType, Unit>() {
                        @Override
                        public Unit invoke(NetworkErrorType networkErrorType) {
                            return null;
                        }
                    });
            return null;
        }
    }


    private class GetSetsTask extends AsyncTask<Integer, Void, Void> {
        private Integer firstIndexOfGroups;

        @Override
        protected Void doInBackground(Integer... params) {
            if (params == null) {
                firstIndexOfGroups = ArchiveActivity.this.dropDownJsonElement.get(0).getAsJsonObject().get("id").getAsInt();
            } else {
                firstIndexOfGroups = params[0];
            }

            ArchiveRestAPIClass.getEntranceSets(getApplicationContext(), firstIndexOfGroups, new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HashMap<Integer, String> jsonHashMap = new HashMap<Integer, String>();
                                JsonObject json = jsonObject.getAsJsonObject();
                                JsonArray leaders = json.getAsJsonArray("record");


                                ArrayList<JsonElement> localListJson = new ArrayList<JsonElement>();
                                ArrayList<String> localList = new ArrayList<String>();
                                for (JsonElement je : leaders) {
                                    String j = je.getAsJsonObject().get("title").getAsString();
                                    if (j.toString() != null) {
                                        int i = je.getAsJsonObject().get("id").getAsInt();
                                        jsonHashMap.put(i, j);
                                        localListJson.add(je);
                                        localList.add(j);
                                    }
                                }

                                ArchiveActivity.this.adapterSet.setItems(localListJson);
                                ArchiveActivity.this.adapterSet.notifyDataSetChanged();


                            } catch (Exception e) {
                                Log.d(TAG, "run: ");
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


    private class GetSetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private Context context;
        private ArrayList<JsonElement> mArrayList = new ArrayList<>();

        public GetSetsAdapter(Context context, ArrayList<JsonElement> arrayList) {
            this.context = context;
            this.mArrayList = arrayList;
        }

        public void setItems(ArrayList<JsonElement> arrayList) {
            this.mArrayList = arrayList;
        }

        public void addItem(ArrayList<JsonElement> arrayList) {
            this.mArrayList.addAll(arrayList);
        }

        private class ItemHolder extends RecyclerView.ViewHolder {
            private ImageView entranceLogo;

            private TextView concourName;
            private TextView concourCode;
            private TextView concourCount;
            private View constraint;


            private JsonObject extraData;


            public ItemHolder(View itemView) {
                super(itemView);

                concourName = (TextView) itemView.findViewById(R.id.ccListitemArchiveL_concourName);
                concourCode = (TextView) itemView.findViewById(R.id.ccListitemArchiveL_concourCode);
                concourCount = (TextView) itemView.findViewById(R.id.ccListitemArchiveL_concourCount);
                entranceLogo = (ImageView) itemView.findViewById(R.id.settingUserInfoL_userImage);
                constraint = itemView.findViewById(R.id.archiveDetailA_constraitItem);


                concourName.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                concourCode.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                concourCount.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
            }


            public void setupHolder(final JsonElement jsonElement) {

                String t1 = jsonElement.getAsJsonObject().get("title").getAsString();
                concourName.setText(t1);

                String concourCodeInt = FormatterSingleton.getInstance().getNumberFormatter().format(jsonElement.getAsJsonObject().get("code").getAsInt());
                String t2 = "کد: " + concourCodeInt;
                concourCode.setText(t2);

                String concourCountInt = FormatterSingleton.getInstance().getNumberFormatter().format(jsonElement.getAsJsonObject().get("entrance_count").getAsInt());
                String t3 = concourCountInt + " کنکور";
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
                MediaRestAPIClass.downloadEsetImage(getApplicationContext(), imageId, entranceLogo, new Function2<JsonObject, HTTPErrorType, Unit>() {
                    @Override
                    public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (httpErrorType != HTTPErrorType.Success) {
//                                    entranceLogo.setImageDrawable(getResources().getDrawable(R.drawable.male_icon_100));
                                    entranceLogo.setBackgroundResource(R.drawable.male_icon_100);
                                }
                            }
                        });
                        Log.d(TAG, "invoke: " + jsonObject);
                        return null;
                    }
                }, new Function1<NetworkErrorType, Unit>() {
                    @Override
                    public Unit invoke(NetworkErrorType networkErrorType) {
                        return null;
                    }
                });


                ArchiveActivity.this.mArchiveEsetStructs.updated = jsonElement.getAsJsonObject().get("updated").getAsString();
                ArchiveActivity.this.mArchiveEsetStructs.code = jsonElement.getAsJsonObject().get("code").getAsString();
                ArchiveActivity.this.mArchiveEsetStructs.entrance_count = jsonElement.getAsJsonObject().get("entrance_count").getAsInt();
                ArchiveActivity.this.mArchiveEsetStructs.title = jsonElement.getAsJsonObject().get("title").getAsString();
                ArchiveActivity.this.mArchiveEsetStructs.id = jsonElement.getAsJsonObject().get("id").getAsInt();


                entranceLogo.setLayoutParams(entranceLogoLP);
                entranceLogo.setImageResource(R.drawable.male_icon_100);


                constraint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(getApplicationContext(), jsonElement.getAsJsonObject().get("title").getAsString(), Toast.LENGTH_SHORT).show();


                        ArchiveActivity.this.mArchiveEsetDetailStruct.esetStruct = ArchiveActivity.this.mArchiveEsetStructs;
                        ArchiveActivity.this.mArchiveEsetDetailStruct.groupTitle = ArchiveActivity.this.tabbarJsonElement.get(ArchiveActivity.this.tabLayout.getSelectedTabPosition()).getAsJsonObject().get("title").getAsString();

                        Integer dropdownPosition = ArchiveActivity.this.currentPositionDropDown;
                        ArchiveActivity.this.mArchiveEsetDetailStruct.typeTitle = ArchiveActivity.this.dropDownJsonElement.get(  dropdownPosition).getAsJsonObject().get("title").getAsString();

                        if(jsonElement.getAsJsonObject().get("entrance_count").getAsInt() != 0) {
                            Intent i = ArchiveDetailActivity.newIntent(ArchiveActivity.this, ArchiveActivity.this.mArchiveEsetDetailStruct);
                            startActivity(i);
                        } else {
                            Toast.makeText(context, "تعداد کنکور موجود 0 عدد است", Toast.LENGTH_SHORT).show();
                        }





                    }
                });


            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(context).inflate(R.layout.cc_archive_listitem_details, parent, false);
            return new ItemHolder(view);

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            JsonElement oneItem = this.mArrayList.get(position);

            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.setupHolder(oneItem);

        }


        @Override
        public int getItemCount() {
            return mArrayList.size();
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
            tv.setText(mArrayList.get(position));

            return v;
        }

        public void addAll(ArrayList<String> arrayList) {
            this.mArrayList.addAll(arrayList);
        }
    }

}



