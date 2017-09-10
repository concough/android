package com.concough.android.concough;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;
import com.concough.android.utils.CustomTypefaceSpan;
import com.concough.android.utils.SoftKeyboard;

import java.util.ArrayList;

public class BottomNavigationActivity extends AppCompatActivity {


    private int selectedItemId;
    protected OnClickEventInterface clickEventInterface = null;
    protected BottomNavigationView navigation;
    SoftKeyboard softKeyboard;

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
                    Intent s = SettingActivity.newIntent(BottomNavigationActivity.this);
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


    protected void showOrHideNavigation(ViewGroup r) {
        navigation.setVisibility(View.VISIBLE);

        //  LinearLayout mainLayout = (LinearLayout) findViewById(R.id.masterLayout);
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

                    }
                }, 400);
            }

            @Override
            public void onSoftKeyboardShow() {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navigation.setVisibility(View.GONE);

                    }
                }, 400);
            }
        });
        softKeyboard.openSoftKeyboard();
        softKeyboard.closeSoftKeyboard();


    }


    protected void createActionBar(String titleName, @Nullable Boolean hasBackButton, @Nullable final ArrayList<ButtonDetail> buttonDetailList) {
        ActionBar mActionBar = getSupportActionBar();


        assert mActionBar != null;
        mActionBar.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setElevation(2);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_custom_view, null);

//        TextView abtext = (TextView) mCustomView.findViewById(R.id.archiveDetailActionBarA_title);

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 0);
        layoutParams.gravity = Gravity.CENTER;
        mActionBar.setCustomView(mCustomView, layoutParams);
        mActionBar.setDisplayShowCustomEnabled(true);


        TextView abtext = (TextView) mCustomView.findViewById(R.id.archiveDetailActionBarA_title);
        abtext.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
        abtext.setText(titleName);

        ImageButton icon0 = (ImageButton) mCustomView.findViewById(R.id.actionBarIcon0);
        ImageButton icon1 = (ImageButton) mCustomView.findViewById(R.id.actionBarIcon1);
        ImageButton icon2 = (ImageButton) mCustomView.findViewById(R.id.actionBarIcon2);

        ConstraintLayout constraintIcon0 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon0);
        ConstraintLayout constraintIcon1 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon1);
        ConstraintLayout constraintIcon2 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon2);

        final TextView badgeCountIcon0 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon0Badge);
        final TextView badgeCountIcon1 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon1Badge);
        final TextView badgeCountIcon2 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon2Badge);

        ImageButton backIcon = (ImageButton) mCustomView.findViewById(R.id.actionBarBack);
        backIcon.setVisibility(View.GONE);


        constraintIcon0.setVisibility(View.GONE);
        constraintIcon1.setVisibility(View.GONE);
        constraintIcon2.setVisibility(View.GONE);

        //badges
        badgeCountIcon0.setVisibility(View.INVISIBLE);
        badgeCountIcon1.setVisibility(View.INVISIBLE);
        badgeCountIcon2.setVisibility(View.INVISIBLE);

        badgeCountIcon0.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        badgeCountIcon1.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        badgeCountIcon2.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());


        icon0.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
        icon1.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));
        icon2.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorConcoughBlue));

        if (hasBackButton != null) {
            if (hasBackButton) {
                backIcon.setVisibility(View.VISIBLE);
                backIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (clickEventInterface != null) {
                            clickEventInterface.OnBackClicked();
                        }
                    }
                });
            }
        }

        icon0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickEventInterface != null) {
                    if (buttonDetailList != null && buttonDetailList.size() > 0) {
                        clickEventInterface.OnButtonClicked(buttonDetailList.get(0).imageSource);

                    }
                }
            }
        });

        icon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickEventInterface != null) {
                    if (buttonDetailList != null && buttonDetailList.size() > 1) {
                        clickEventInterface.OnButtonClicked(buttonDetailList.get(1).imageSource);
                    }
                }
            }
        });

        icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickEventInterface != null) {
                    if (buttonDetailList != null && buttonDetailList.size() > 2) {
                        clickEventInterface.OnButtonClicked(buttonDetailList.get(2).imageSource);
                    }
                }
            }
        });

        String badgeCount = "";
        if (buttonDetailList != null) {
            if (buttonDetailList.size() > 0) {
                for (int i = 0; i < buttonDetailList.size(); i++) {
                    if (i == 0) {
                        icon0.setBackground(ContextCompat.getDrawable(getApplicationContext(), buttonDetailList.get(i).imageSource));
                        constraintIcon0.setVisibility(View.VISIBLE);
                        if (buttonDetailList.get(0).hasBadge) {
                            if (buttonDetailList.get(0).badgeCount > 0) {
                                badgeCount = FormatterSingleton.getInstance().getNumberFormatter().format(buttonDetailList.get(0).badgeCount);
                                badgeCountIcon0.setText(badgeCount);
                                badgeCountIcon0.setVisibility(View.VISIBLE);
                            } else {
                                constraintIcon0.setVisibility(View.GONE);
                            }
                        }
                    } else if (i == 1) {
                        icon1.setBackground(ContextCompat.getDrawable(getApplicationContext(), buttonDetailList.get(i).imageSource));
                        constraintIcon1.setVisibility(View.VISIBLE);
                        if (buttonDetailList.get(1).hasBadge) {
                            if (buttonDetailList.get(1).badgeCount > 0) {
                                badgeCount = FormatterSingleton.getInstance().getNumberFormatter().format(buttonDetailList.get(1).badgeCount);
                                badgeCountIcon1.setText(badgeCount);
                                badgeCountIcon1.setVisibility(View.VISIBLE);
                            } else {
                                constraintIcon1.setVisibility(View.GONE);
                            }
                        }
                    } else if (i == 2) {
                        icon2.setBackground(ContextCompat.getDrawable(getApplicationContext(), buttonDetailList.get(i).imageSource));
                        constraintIcon2.setVisibility(View.VISIBLE);

                        if (buttonDetailList.get(2).hasBadge) {
                            if (buttonDetailList.get(2).badgeCount > 0) {
                                badgeCount = FormatterSingleton.getInstance().getNumberFormatter().format(buttonDetailList.get(2).badgeCount);
                                badgeCountIcon2.setText(badgeCount);
                                badgeCountIcon2.setVisibility(View.VISIBLE);
                            } else {
                                constraintIcon2.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
        }

    }

    protected interface OnClickEventInterface {
        void OnButtonClicked(int id);

        void OnBackClicked();
    }

    protected class ButtonDetail {
        protected int imageSource;

        protected boolean hasBadge = false;
        protected int badgeCount;
    }


}
