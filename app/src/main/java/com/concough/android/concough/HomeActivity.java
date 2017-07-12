package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.concough.android.singletons.BasketSingleton;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "HomeActivity";

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toast.makeText(this, "HomeActivity", Toast.LENGTH_LONG).show();
        BasketSingleton.getInstance();
        BasketSingleton.getInstance().setListener(new BasketSingleton.BasketSingletonListener() {
            @Override
            public void onCheckout(int count, @NotNull HashMap<Integer, BasketSingleton.PurchasedItem> purchased) {

            }

            @Override
            public void onRemoveCompleted(int count) {

            }

            @Override
            public void onAddCompleted(int count) {

            }

            @Override
            public void onCreateCompleted() {

            }

            @Override
            public void onLoadItemCompleted(int count) {
                Log.d(TAG, "items = " + count);
            }
        });

        BasketSingleton.getInstance().loadBasketItems(this);
    }

}
