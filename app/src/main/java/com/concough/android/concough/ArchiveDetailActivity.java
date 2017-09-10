package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.concough.android.general.AlertClass;
import com.concough.android.rest.ArchiveRestAPIClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.BasketSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.structures.ArchiveEsetDetailStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class ArchiveDetailActivity extends BottomNavigationActivity {
    private static String TAG = "ArchiveDetailActivity";

    //private ArrayList<MyStruct> listDetail;
    private RecyclerView recyclerView;

    private TextView setNameText;

    private GetEntranceAdapter adapter;
    private ArrayList<JsonElement> setsList;

    private final static String Detail_Struct = "Detail_Struct";


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

//        ActionBar mActionBar = getSupportActionBar();
//        mActionBar.setDisplayShowTitleEnabled(false);
//        mActionBar.setElevation(2);

//        LayoutInflater mInflater = LayoutInflater.from(this);
//        View mCustomView = mInflater.inflate(R.layout.cc_archivedetail_actionbar, null);

//        TextView abtext = (TextView) mCustomView.findViewById(R.id.archiveDetailActionBarA_title);
//        abtext.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

//        mActionBar.setCustomView(mCustomView);
//        mActionBar.setDisplayShowCustomEnabled(true);


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

    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarSet();
    }

    private void actionBarSet() {
        final ArrayList<ButtonDetail> buttonDetailArrayList = new ArrayList<>();

        ButtonDetail buttonDetail;
        buttonDetail = new ButtonDetail();
        if(BasketSingleton.getInstance().getSalesCount()>0){
            buttonDetail.hasBadge = true;
            buttonDetail.badgeCount = BasketSingleton.getInstance().getSalesCount();
        }
        buttonDetail.imageSource = R.drawable.buy_icon;
        buttonDetailArrayList.add(buttonDetail);

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
        protected Void doInBackground(Integer... params) {

            ArchiveRestAPIClass.getEntrances(getApplicationContext(), params[0], new Function2<JsonObject, HTTPErrorType, Unit>() {
                @Override
                public Unit invoke(final JsonObject jsonObject, final HTTPErrorType httpErrorType) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (jsonObject != null) {
                                if (httpErrorType == HTTPErrorType.Success) {
//                                    try {
                                    //HashMap<Integer, String> jsonHashMap = new HashMap<Integer, String>();
                                    JsonObject json = jsonObject.getAsJsonObject();
                                    JsonArray leaders = json.getAsJsonArray("record");


                                    // ArrayList<JsonElement> localListJson = new ArrayList<JsonElement>();
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

//                                        ArchiveActivity.this.mSectionsPagerAdapter.notifyDataSetChanged();
//                                        tabLayout.setupWithViewPager(ArchiveActivity.this.mViewPager);

//                                        ArchiveActivity.this.changeGroupIndex(0);

//                                    } catch (Exception e) {
//                                        Log.d(TAG, "run: ");
//                                    }
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
                            switch (networkErrorType) {
                                case NoInternetAccess:
                                case HostUnreachable: {
                                    AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "error", null);
                                    break;
                                }
                                default: {
                                    AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "", null);
                                    break;
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
                code.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                count.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

            }

            public void setupHolder() {
                String t1 = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.typeTitle + " (" + ArchiveDetailActivity.this.mArchiveEsetDetailStruct.groupTitle + ")";
                setName.setText(t1);

                String t2 = "کد: " + ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.code;
                code.setText(t2);

                String t3 = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.entrance_count + " کنکور منتشر شده";
                count.setText(t3);


                Integer imageId = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.id;

                downloadImage(imageId);

            }

            private void downloadImage(final int imageId) {
                MediaRestAPIClass.downloadEsetImage(ArchiveDetailActivity.this, imageId, logoImage, new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                        logoImage.setImageResource(R.drawable.no_image);
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
                                        AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "", null);
                                        break;
                                    }

                                }
                            }
                        });

                        return null;
                    }
                });

            }


        }


        private class ItemsHolder extends RecyclerView.ViewHolder {
            private TextView extraDataText;
            private TextView countText;
            private TextView dateJalali;
            private TextView typeText;
            private TextView yearText;

            private ImageView logoImage;

            private JsonObject extraData;


            public ItemsHolder(View itemView) {
                super(itemView);

                extraDataText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_extraDataText);
                countText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_countText);
                dateJalali = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_dateJalali);
                typeText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_typeText);
                yearText = (TextView) itemView.findViewById(R.id.archiveDetailHolder2L_yearText);
                logoImage = (ImageView) itemView.findViewById(R.id.archiveDetailHolder2L_imageRight);


                extraDataText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                countText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                dateJalali.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                typeText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                yearText.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

            }

            public void setupHolder(JsonElement jsonElement) {
                String t1 = jsonElement.getAsJsonObject().get("organization").getAsJsonObject().get("title").getAsString();
                typeText.setText(t1);

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

                String s;
                s = jsonElement.getAsJsonObject().get("extra_data").getAsString();
                extraData = new JsonParser().parse(s).getAsJsonObject();

                String extra = "";
                ArrayList<String> extraArray = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : extraData.entrySet()) {
                    extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
                }

                extra = TextUtils.join(" - ", extraArray);
                extraDataText.setText(extra);


                Integer imageId = ArchiveDetailActivity.this.mArchiveEsetDetailStruct.esetStruct.id;

                downloadImage(imageId);
            }

            private void downloadImage(final int imageId) {
                MediaRestAPIClass.downloadEsetImage(ArchiveDetailActivity.this, imageId, logoImage, new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                        logoImage.setImageResource(R.drawable.no_image);
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
                                        AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(ArchiveDetailActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "", null);
                                        break;
                                    }

                                }
                            }
                        });

                        return null;
                    }
                });

            }

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
                itemHolder.setupHolder(oneItem);
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
