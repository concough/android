package com.concough.android.concough;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.concough.android.general.AlertClass;
import com.concough.android.rest.MediaRestAPIClass;
import com.concough.android.singletons.BasketSingleton;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.structures.EntranceStruct;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    //    private BottomNavigationView bottomNavigationView;
    private BasketCheckoutActivity.BasketCheckoutAdapter basketCheckoutAdapter;

    private String contextFromWho = "";

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

//        setContentView(R.layout.activity_basket_checkout);

//        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
//        bottomNavigationView.setVisibility(View.GONE);

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
            public void onCreateCompleted() {

            }

            @Override
            public void onAddCompleted(int count) {

            }

            @Override
            public void onRemoveCompleted(final int count, final int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BasketCheckoutActivity.this.updateTotalCost();
                        BasketCheckoutActivity.this.basketCheckoutAdapter.notifyItemRemoved(position);

                        // TODO; show top message with "ActionResult" and sub message "BasketDeleteSuccess"
                        if (count == 0) {
                            BasketCheckoutActivity.this.checkoutButton.setVisibility(View.GONE);
                        }

                    }
                });

            }

            @Override
            public void onCheckout(final int count, HashMap<Integer, BasketSingleton.PurchasedItem> purchased) {
                if (purchased != null) {
                    // TODO: show alert message with ActionResult and sub message of PurchasedSuccess
                    // TODO: update badge of navigation bar

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
                }
            }
        });

        updateTotalCost();
        actionBarSet();
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
            if (viewType == CheckoutViewHolderType.ENTRANCE.getValue()) {
                View v = LayoutInflater.from(BasketCheckoutActivity.this).inflate(R.layout.item_checkout_entrance, parent, false);
                return new CKEntranceHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            BasketSingleton.SaleItem sale = BasketSingleton.getInstance().getSaleByIndex(position);
            if (sale != null) {
                if (sale.getType() == "Entrance") {
                    ((CKEntranceHolder) holder).setupHolder((EntranceStruct) sale.getTarget(), sale.getCost(), position);
                }
            }

        }

        @Override
        public int getItemCount() {
            return BasketSingleton.getInstance().getSalesCount();
        }

        @Override
        public int getItemViewType(int position) {
            BasketSingleton.SaleItem sale = BasketSingleton.getInstance().getSaleByIndex(position);
            if (sale != null) {
                if (sale.getType() == "Entrance") {
                    return CheckoutViewHolderType.ENTRANCE.getValue();
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
                orgTypeTextView.setText("کنکور " + entrance.getEntranceTypeTitle() + " " + entrance.getEntranceOrgTitle() + " " +
                        FormatterSingleton.getInstance().getNumberFormatter().format(entrance.getEntranceYear()));
                groupTextView.setText(entrance.getEntranceSetTitle() + " (" + entrance.getEntranceGroupTitle() + ")");
                costTextView.setText(FormatterSingleton.getInstance().getNumberFormatter().format(cost) + " تومان");

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
                            // TODO: show loading message
                            BasketSingleton.getInstance().removeSaleById(BasketCheckoutActivity.this, sale.getId(), position);
                        }
                    }
                });
            }

            private void downloadImage(final int imageId) {
                MediaRestAPIClass.downloadEsetImage(BasketCheckoutActivity.this, imageId, esetImageView, new Function2<JsonObject, HTTPErrorType, Unit>() {
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
                                        AlertClass.showTopMessage(BasketCheckoutActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "error", null);
                                        break;
                                    }
                                    default: {
                                        AlertClass.showTopMessage(BasketCheckoutActivity.this, findViewById(R.id.activity_home), "NetworkError", networkErrorType.name(), "", null);
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

    }
}
