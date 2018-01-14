package com.concough.android.concough;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.singletons.FontCacheSingleton;
import com.concough.android.singletons.FormatterSingleton;

import java.util.ArrayList;

public class TopNavigationActivity extends AppCompatActivity {

    protected OnClickEventInterface clickEventInterface = null;
    private Boolean backPressed = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (backPressed == false) {
                Toast.makeText(getApplicationContext(), "برای خروج دوباره دکمه بازگشت را لمس نمایید", Toast.LENGTH_LONG).show();
                backPressed = true;
                return false;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        ImageView icon0 = (ImageView) mCustomView.findViewById(R.id.actionBarIcon0);
        ImageView icon1 = (ImageView) mCustomView.findViewById(R.id.actionBarIcon1);
        ImageView icon2 = (ImageView) mCustomView.findViewById(R.id.actionBarIcon2);

        ConstraintLayout constraintIcon0 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon0);
        ConstraintLayout constraintIcon1 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon1);
        ConstraintLayout constraintIcon2 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon2);

        final TextView badgeCountIcon0 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon0Badge);
        final TextView badgeCountIcon1 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon1Badge);
        final TextView badgeCountIcon2 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon2Badge);

        LinearLayout backIcon = (LinearLayout) mCustomView.findViewById(R.id.linearBack);
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


    protected void updateBadge(int index, int count) {
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        View mCustomView = actionbar.getCustomView();


        switch (index) {
            case 0: {
                TextView badgeCountIcon0 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon0Badge);
                ConstraintLayout constraintIcon0 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon0);

                String badgeCount = FormatterSingleton.getInstance().getNumberFormatter().format(count);
                badgeCountIcon0.setText(badgeCount);
                constraintIcon0.setVisibility(View.VISIBLE);
                break;
            }
            case 1: {
                TextView badgeCountIcon1 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon1Badge);
                ConstraintLayout constraintIcon1 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon1);

                String badgeCount = FormatterSingleton.getInstance().getNumberFormatter().format(count);
                badgeCountIcon1.setText(badgeCount);
                constraintIcon1.setVisibility(View.VISIBLE);
                break;
            }
            case 2: {
                TextView badgeCountIcon2 = (TextView) mCustomView.findViewById(R.id.actionBar_constraintIcon2Badge);
                ConstraintLayout constraintIcon2 = (ConstraintLayout) mCustomView.findViewById(R.id.actionBar_constraintIcon2);

                String badgeCount = FormatterSingleton.getInstance().getNumberFormatter().format(count);
                badgeCountIcon2.setText(badgeCount);
                constraintIcon2.setVisibility(View.VISIBLE);
                break;
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
