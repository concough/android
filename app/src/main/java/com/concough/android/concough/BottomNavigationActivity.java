package com.concough.android.concough;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.utils.CustomTypefaceSpan;

public class BottomNavigationActivity extends AppCompatActivity {


    private int selectedItemId;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectedItemId = item.getItemId();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent ih = HomeActivity.newIntent(BottomNavigationActivity.this);
                    startActivity(ih);
                    return true;
                case R.id.navigation_archive:
                    Intent i = ArchiveActivity.newIntent(BottomNavigationActivity.this);
                    startActivity(i);
                    return true;
                case R.id.navigation_favorites:
                    Intent f = FavoritesActivity.newIntent(BottomNavigationActivity.this);
                    startActivity(f);
                    return true;
                case R.id.navigation_settings:
                    return true;
            }
            return false;
        }

    };

    @LayoutRes
    protected int getLayoutResourceId() {
        return R.layout.activity_bottom_navigation;
    }

    protected void setMenuSelectedIndex(int index) {
        switch (index) {
            case 1: this.selectedItemId = R.id.navigation_archive; break;
            case 2: this.selectedItemId = R.id.navigation_favorites; break;
            case 3: this.selectedItemId = R.id.navigation_settings; break;
            default: this.selectedItemId = R.id.navigation_home; break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan("", FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
        for (int i =0; i < navigation.getMenu().size(); i++) {
            MenuItem m = navigation.getMenu().getItem(i);
            SpannableStringBuilder title = new SpannableStringBuilder(m.getTitle());
            title.setSpan(typefaceSpan, 0, title.length(), 0);
            m.setTitle(title);
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(this.selectedItemId).setChecked(true);

    }

}
