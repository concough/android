package com.concough.android.concough;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.concough.android.general.AlertClass;
import com.concough.android.models.EntranceModel;
import com.concough.android.models.EntranceModelHandler;
import com.concough.android.models.PurchasedModel;
import com.concough.android.models.PurchasedModelHandler;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.BasketSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.singletons.MediaCacheSingleton;
import com.concough.android.singletons.UserDefaultsSingleton;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.vendor.progressHUD.KProgressHUD;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class BasketCheckoutActivity extends BottomNavigationActivity {
    // testing with Basket

    private static final String TAG = "BasketCheckoutActivity";
    private static final String CONTEXT_WHO_KEY = "CONTEXT_WHO";

    private PullRefreshLayout pullRefreshLayout = null;
    private Button checkoutButton;
    private TextView costLabelTextView;
    private TextView costValueTextView;
    private RecyclerView recycleView;
    private KProgressHUD loadingProgress;

    //    private BottomNavigationView bottomNavigationView;
    private BasketCheckoutActivity.BasketCheckoutAdapter basketCheckoutAdapter;

    private String contextFromWho = "";
    private String username = "";

    public static Intent newIntent(Context packageContext, String who) {
        Intent i = new Intent(packageContext, BasketCheckoutActivity.class);
        i.putExtra(CONTEXT_WHO_KEY, who);
        return i;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_basket_checkout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setMenuSelectedIndex(1);
        super.onCreate(savedInstanceState);
        this.contextFromWho = getIntent().getStringExtra(CONTEXT_WHO_KEY);

        // get username
        username = UserDefaultsSingleton.getInstance(BasketCheckoutActivity.this).getUsername();

        // Initialize controls
        checkoutButton = (Button) findViewById(R.id.basketCheckoutA_checkout);
        costLabelTextView = (TextView) findViewById(R.id.basketCheckoutA_cost_label);
        costValueTextView = (TextView) findViewById(R.id.basketCheckoutA_cost_value);

        checkoutButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
        costLabelTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        costValueTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        recycleView = (RecyclerView) findViewById(R.id.basketCheckoutA_recycle);
        basketCheckoutAdapter = new BasketCheckoutAdapter(this);

        if (BasketSingleton.getInstance().getSalesCount() == 0) {
            this.checkoutButton.setVisibility(View.GONE);
        }

        // Views Events
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BasketSingleton.getInstance().checkout(BasketCheckoutActivity.this);
            }
        });

        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.setAdapter(basketCheckoutAdapter);

        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.basketCheckoutA_swipeRefreshLayout);
        pullRefreshLayout.setColorSchemeColors(Color.TRANSPARENT, Color.GRAY, Color.GRAY, Color.GRAY);
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                BasketCheckoutActivity.this.refreshBasket();
            }
        });

        // Listeners
        BasketSingleton.getInstance().setListener(new BasketSingleton.BasketSingletonListener() {
            @Override
            public void onLoadItemCompleted(final int count) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BasketCheckoutActivity.this.pullRefreshLayout.setRefreshing(false);
                        BasketCheckoutActivity.this.basketCheckoutAdapter.notifyDataSetChanged();

                        if (count == 0) {
                            BasketCheckoutActivity.this.checkoutButton.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onCreateCompleted(Integer position) {

            }

            @Override
            public void onAddCompleted(int count) {

            }

            @Override
            public void onRemoveCompleted(final int count, final int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertClass.hideLoadingMessage(loadingProgress);
                        BasketCheckoutActivity.this.updateTotalCost();
                        BasketCheckoutActivity.this.basketCheckoutAdapter.notifyItemRemoved(position);
                        BasketCheckoutActivity.this.basketCheckoutAdapter.notifyItemRangeChanged(position, count);

                        AlertClass.showTopMessage(BasketCheckoutActivity.this, findViewById(R.id.container), "ActionResult", "BasketDeleteSuccess", "success", null);
                        if (count == 0) {
                            BasketCheckoutActivity.this.checkoutButton.setVisibility(View.GONE);
                        }

                    }
                });

            }

            @Override
            public void onCheckout(final int count, HashMap<Integer, BasketSingleton.PurchasedItem> purchased) {
                if (purchased != null) {
                    AlertClass.showAlertMessage(BasketCheckoutActivity.this, "ActionResult", "PurchasedSuccess", "success", null);

                    BasketCheckoutActivity.this.setMenuItemColor(1, R.color.colorConcoughRedLight);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BasketCheckoutActivity.this.basketCheckoutAdapter.notifyDataSetChanged();
                            BasketCheckoutActivity.this.updateTotalCost();

                            if (count == 0) {
                                BasketCheckoutActivity.this.checkoutButton.setVisibility(View.GONE);
                            }
                        }
                    });

                    if(purchased.size()>0)
                    purchasedIds(purchased);
                }
            }
        });

        updateTotalCost();
        actionBarSet();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri data = getIntent().getData();
        String scheme = data.getScheme(); // "http"
        String host = data.getHost(); // "twitter.com"
        List<String> params = data.getPathSegments();
        String first = params.get(0); // "status"
        String second = params.get(1); // "1234"

        Log.d(TAG, "External *** "+"F="+first+"-S="+second);
        if(first.equals("pay")) {
            if(second.equals("success")) {

            } else {

            }
        }
    }

    private void purchasedIds(HashMap<Integer, BasketSingleton.PurchasedItem> purchased) {
        Integer[] purchasedIds = new Integer[purchased.size()];

        int i = 0;
        for (BasketSingleton.PurchasedItem purchase : purchased.values()) {
            purchasedIds[i] = purchase.getPurchasedId();

            i++;
        }

        RealmResults<PurchasedModel> purchasedIn = PurchasedModelHandler.getAllPurchasedIn(BasketCheckoutActivity.this, username, purchasedIds);
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
            byte[]  data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
            if (data != null) {

                File folder = new File(getApplicationContext().getFilesDir(),"images");
                File folder2 = new File(getApplicationContext().getFilesDir()+"/images","eset");
                if (!folder.exists()) {
                    folder.mkdir();
                    folder2.mkdir();
                }

                File photo=new File(getApplicationContext().getFilesDir()+"/images/eset", String.valueOf(imageId));
                if (photo.exists()) {
                    photo.delete();
                }

                try {
                    FileOutputStream fos=new FileOutputStream(photo.getPath());

                    fos.write(data);
                    fos.close();
                }
                catch (java.io.IOException e) {
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }
            }
        }

    }


    private void actionBarSet() {

        super.clickEventInterface = new OnClickEventInterface() {
            @Override
            public void OnButtonClicked(int id) {
                switch (id) {

                }
            }

            @Override
            public void OnBackClicked() {
                onBackPressed();
            }
        };

        super.createActionBar("سبد محصولات", true, null);
    }


    private void refreshBasket() {
        BasketSingleton.getInstance().loadBasketItems(BasketCheckoutActivity.this);
    }

    private void updateTotalCost() {
        costValueTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(BasketSingleton.getInstance().getTotalCost()) + " تومان");
    }

    private enum CheckoutViewHolderType {
        ENTRANCE(1);

        private final int value;

        private CheckoutViewHolderType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private class BasketCheckoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;

        public BasketCheckoutAdapter(Context context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 50) {
                View view = LayoutInflater.from(context).inflate(R.layout.cc_recycle_not_item, parent, false);
                return new ItemEmptyHolder(view);

            } else if (viewType == CheckoutViewHolderType.ENTRANCE.getValue()) {
                View v = LayoutInflater.from(BasketCheckoutActivity.this).inflate(R.layout.item_checkout_entrance, parent, false);
                return new CKEntranceHolder(v);

            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getClass() == ItemEmptyHolder.class) {
                ItemEmptyHolder itemEmptyHolder = (ItemEmptyHolder) holder;
                itemEmptyHolder.setupHolder();
            } else {
                BasketSingleton.SaleItem sale = BasketSingleton.getInstance().getSaleByIndex(position);
                if (sale != null) {
                    if (sale.getType() == "Entrance") {
                        ((CKEntranceHolder) holder).setupHolder((EntranceStruct) sale.getTarget(), sale.getCost(), position);

                    }

                }
            }


        }

        @Override
        public int getItemCount() {
            if (BasketSingleton.getInstance().getSalesCount() == 0) {
                return 1;
            } else {
                return BasketSingleton.getInstance().getSalesCount();
            }

        }

        @Override
        public int getItemViewType(int position) {
            if (BasketSingleton.getInstance().getSalesCount() == 0) {
                return 50;
            } else {
                BasketSingleton.SaleItem sale = BasketSingleton.getInstance().getSaleByIndex(position);
                if (sale != null) {
                    if (sale.getType() == "Entrance") {
                        return CheckoutViewHolderType.ENTRANCE.getValue();
                    }
                }
            }
            return 0;
        }

        private class CKEntranceHolder extends RecyclerView.ViewHolder {
            private TextView orgTypeTextView;
            private TextView groupTextView;
            private ImageView esetImageView;
            private TextView costTextView;
            private TextView extraDataTextView;
            private LinearLayout deleteLayoyt;
            private TextView deleteLabel;

            public CKEntranceHolder(View itemView) {
                super(itemView);

                orgTypeTextView = (TextView) itemView.findViewById(R.id.CK_entrance_orgtype_label);
                groupTextView = (TextView) itemView.findViewById(R.id.CK_entrance_group_label);
                costTextView = (TextView) itemView.findViewById(R.id.CK_entrance_item_cost_TextView);
                extraDataTextView = (TextView) itemView.findViewById(R.id.CK_entrance_extradata_TextView);
                deleteLabel = (TextView) itemView.findViewById(R.id.CK_entrance_delete_label);

                esetImageView = (ImageView) itemView.findViewById(R.id.CK_entrance_image);
                deleteLayoyt = (LinearLayout) itemView.findViewById(R.id.CK_entrance_delete_layout);

                orgTypeTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                groupTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                costTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
                extraDataTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                deleteLabel.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());

            }

            @SuppressLint("SetTextI18n")
            public void setupHolder(EntranceStruct entrance, int cost, final int position) {
                orgTypeTextView.setText("آزمون " + entrance.getEntranceTypeTitle() + " " + entrance.getEntranceOrgTitle() + " " +
                        FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceYear()));
                groupTextView.setText(entrance.getEntranceSetTitle() + " (" + entrance.getEntranceGroupTitle() + ")");

                if (cost == 0) {
                    costTextView.setText("رایگان");
                    costTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughGray));
                } else {
                    costTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(cost) + " تومان");
                }

                ArrayList<String> extraArray = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : entrance.getEntranceExtraData().getAsJsonObject().entrySet()) {
                    extraArray.add(entry.getKey() + ": " + entry.getValue().getAsString());
                }

                String extra = TextUtils.join(" - ", extraArray);
                extraDataTextView.setText(extra);

                if (entrance.getEntranceSetId() != null) {
                    downloadImage(entrance.getEntranceSetId());
                }

                // Events
                deleteLayoyt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BasketSingleton.SaleItem sale = BasketSingleton.getInstance().getSaleByIndex(position);

                        if (sale != null) {
                            loadingProgress = AlertClass.showLoadingMessage(BasketCheckoutActivity.this);
                            loadingProgress.show();
                            BasketSingleton.getInstance().removeSaleById(BasketCheckoutActivity.this, sale.getId(), position);
                        }
                    }
                });
            }


            private void downloadImage(final int imageId) {
                final String url = MediaRestAPIClass.makeEsetImageUrl(imageId);
                byte[] data = MediaCacheSingleton.getInstance(getApplicationContext()).get(url);
                if (data != null) {

                    Glide.with(BasketCheckoutActivity.this)

                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(esetImageView)
                            .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));


                } else {
                    MediaRestAPIClass.downloadEsetImage(BasketCheckoutActivity.this, imageId, esetImageView, new Function2<byte[], HTTPErrorType, Unit>() {
                        @Override
                        public Unit invoke(final byte[] data, final HTTPErrorType httpErrorType) {
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
                                    } else {
                                        MediaCacheSingleton.getInstance(getApplicationContext()).set(url, data);

                                        Glide.with(BasketCheckoutActivity.this)

                                                .load(data)
                                                //.crossFade()
                                                .dontAnimate()
                                                .into(esetImageView)
                                                .onLoadFailed(null, ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image));

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
                emptyImage.setImageResource(R.drawable.shopping_cart_empty);
                emptyText.setText("سبد کالای شما خالیست");
            }

        }


    }
}
