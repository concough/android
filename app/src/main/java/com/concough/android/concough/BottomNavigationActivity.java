package com.concough.android.concough;

import android.app.Service;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.concough.android.extensions.BottomAction;
import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.utils.CustomTypefaceSpan;
import com.concough.android.utils.SoftKeyboard;

public class BottomNavigationActivity extends TopNavigationActivity {


    private int selectedItemId;
    protected BottomNavigationView navigation;
    SoftKeyboard softKeyboard;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            item.getIcon().clearColorFilter();
            CharSequence menuTitle = item.getTitle();
            SpannableString styledMenuTitle = new SpannableString(menuTitle);
            styledMenuTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorConcoughBlue)), 0, menuTitle.length(), 0);

            if(selectedItemId==item.getItemId()) return false;
            selectedItemId = item.getItemId();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent ih = HomeActivity.newIntent(BottomNavigationActivity.this);
                    ih.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(ih);


                    return true;
                case R.id.navigation_archive:
                    Intent i = ArchiveActivity.newIntent(BottomNavigationActivity.this);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);


                    return true;
                case R.id.navigation_favorites:
                    Intent f = FavoritesActivity.newIntent(BottomNavigationActivity.this);
                    f.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(f);


                    return true;
                case R.id.navigation_settings:
                    Intent s = SettingActivity.newIntent(BottomNavigationActivity.this);
                    s.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(s);


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
            case 1:
                this.selectedItemId = R.id.navigation_archive;
                break;
            case 2:
                this.selectedItemId = R.id.navigation_favorites;
                break;
            case 3:
                this.selectedItemId = R.id.navigation_settings;
                break;
            default:
                this.selectedItemId = R.id.navigation_home;
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomAction.removeShiftMode(navigation);



        CustomTypefaceSpan typefaceSpan = new CustomTypefaceSpan("", FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
        for (int i = 0; i < navigation.getMenu().size(); i++) {
            MenuItem m = navigation.getMenu().getItem(i);
            SpannableStringBuilder title = new SpannableStringBuilder(m.getTitle());
            title.setSpan(typefaceSpan, 0, title.length(), 0);
            m.setTitle(title);
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(this.selectedItemId).setChecked(true);
    }

    protected void setMenuItemColor(int item, int color) {
        MenuItem menuItem = navigation.getMenu().getItem(item);

        CharSequence menuTitle = menuItem.getTitle();
        SpannableString styledMenuTitle = new SpannableString(menuTitle);
        styledMenuTitle.setSpan(new ForegroundColorSpan(getResources().getColor(color)), 0, menuTitle.length(), 0);


        Drawable menuIcon = menuItem.getIcon();
        int tempColor = ResourcesCompat.getColor(getResources(), color, null);
        menuIcon.setColorFilter(tempColor, PorterDuff.Mode.SRC_IN);

        menuItem.setTitle(styledMenuTitle);
        menuItem.setIcon(menuIcon);

    }

    protected void showOrHideNavigation(ViewGroup r) {
        navigation.setVisibility(View.GONE);

        final Handler handler = new Handler();
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(r, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navigation.setVisibility(View.VISIBLE);
//                        showNavigation(true);
                    }
                }, 400);
            }

            @Override
            public void onSoftKeyboardShow() {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navigation.setVisibility(View.GONE);
//                        showNavigation(false);

                    }
                }, 400);
            }
        });

        softKeyboard.openSoftKeyboard();
        softKeyboard.closeSoftKeyboard();
    }








}
