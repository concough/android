package com.concough.android.vendor.zhikanalertdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

//import com.gitonway.lee.niftymodaldialogeffects.lib.R;
import com.concough.android.concough.R;
import com.concough.android.vendor.zhikanalertdialog.effects.BaseEffects;


/**
 * Created by lee on 2014/7/30.
 */
public class ZhycanNiftyDialogBuilder extends Dialog implements DialogInterface {


    private Effectstype type=null;
    private LinearLayout mLinearLayoutView;
    private RelativeLayout mRelativeLayoutView;
    private LinearLayout mLinearLayoutMsgView;
    private LinearLayout mLinearLayoutTopView;
    private FrameLayout mFrameLayoutCustomView;
    private View mDialogView;
    private View mDivider;
    private TextView mTitle;
    private TextView mMessage;
    private ImageView mIcon;
    private Button mButton1;
    private Button mButton2;
    private int mDuration = -1;
    private static  int mOrientation=1;
    private boolean isCancelable=true;
    private static ZhycanNiftyDialogBuilder instance;

    public ZhycanNiftyDialogBuilder(Context context) {
        super(context);
        init(context);

    }
    public ZhycanNiftyDialogBuilder(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width  = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

    }

    public static ZhycanNiftyDialogBuilder getInstance(Context context) {
        instance = new ZhycanNiftyDialogBuilder(context,R.style.zhycan_dialog_untran);
        return instance;

    }

    private void init(Context context) {

        mDialogView = View.inflate(context, R.layout.zhycan_dialog_layout, null);

        mLinearLayoutView=(LinearLayout)mDialogView.findViewById(R.id.parentPanel);
        mRelativeLayoutView=(RelativeLayout)mDialogView.findViewById(R.id.main);
        mLinearLayoutTopView=(LinearLayout)mDialogView.findViewById(R.id.topPanel);
        mLinearLayoutMsgView=(LinearLayout)mDialogView.findViewById(R.id.contentPanel);
        mFrameLayoutCustomView=(FrameLayout)mDialogView.findViewById(R.id.customPanel);

        mTitle = (TextView) mDialogView.findViewById(R.id.alertTitle);
        mMessage = (TextView) mDialogView.findViewById(R.id.message);
        mIcon = (ImageView) mDialogView.findViewById(R.id.icon);
        mDivider = mDialogView.findViewById(R.id.titleDivider);
        mButton1=(Button)mDialogView.findViewById(R.id.button1);
        mButton2=(Button)mDialogView.findViewById(R.id.button2);

        setContentView(mDialogView);

        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                mLinearLayoutView.setVisibility(View.VISIBLE);
                if(type==null){
                    type= Effectstype.Slidetop;
                }
                start(type);


            }
        });
        mRelativeLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCancelable)dismiss();
            }
        });
    }

    public void toDefault(){
        mTitle.setTextColor(getContext().getResources().getColor(R.color.text_color));
        mDivider.setBackgroundColor(getContext().getResources().getColor(R.color.divider_color));
        mMessage.setTextColor(getContext().getResources().getColor(R.color.msg_color));
        mLinearLayoutView.setBackgroundColor(getContext().getResources().getColor(R.color.dialog_bg));
    }

    public ZhycanNiftyDialogBuilder withDividerColor(String colorString) {
        mDivider.setBackgroundColor(Color.parseColor(colorString));
        return this;
    }
    public ZhycanNiftyDialogBuilder withDividerColor(int color) {
        mDivider.setBackgroundColor(color);
        return this;
    }


    public ZhycanNiftyDialogBuilder withTitle(CharSequence title) {
        toggleView(mLinearLayoutTopView,title);
        mTitle.setText(title);
        return this;
    }

    public ZhycanNiftyDialogBuilder withTitleColor(String colorString) {
        mTitle.setTextColor(Color.parseColor(colorString));
        return this;
    }

    public ZhycanNiftyDialogBuilder withTitleColor(int color) {
        mTitle.setTextColor(color);
        return this;
    }

    public ZhycanNiftyDialogBuilder withMessage(int textResId) {
        toggleView(mLinearLayoutMsgView,textResId);
        mMessage.setText(textResId);
        return this;
    }

    public ZhycanNiftyDialogBuilder withMessage(CharSequence msg) {
        toggleView(mLinearLayoutMsgView,msg);
        mMessage.setText(msg);
        return this;
    }
    public ZhycanNiftyDialogBuilder withMessageColor(String colorString) {
        mMessage.setTextColor(Color.parseColor(colorString));
        return this;
    }
    public ZhycanNiftyDialogBuilder withMessageColor(int color) {
        mMessage.setTextColor(color);
        return this;
    }

    public ZhycanNiftyDialogBuilder withDialogColor(String colorString) {
        mLinearLayoutView.getBackground().setColorFilter(ColorUtils.getColorFilter(Color.parseColor(colorString)));
        return this;
    }

    public ZhycanNiftyDialogBuilder withDialogColor(int color) {
        mLinearLayoutView.getBackground().setColorFilter(ColorUtils.getColorFilter(color));
        return this;
    }

    public ZhycanNiftyDialogBuilder withIcon(int drawableResId) {
        mIcon.setImageResource(drawableResId);
        return this;
    }

    public ZhycanNiftyDialogBuilder withIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
        return this;
    }

    public ZhycanNiftyDialogBuilder withDuration(int duration) {
        this.mDuration=duration;
        return this;
    }

    public ZhycanNiftyDialogBuilder withEffect(Effectstype type) {
        this.type=type;
        return this;
    }
    
    public ZhycanNiftyDialogBuilder withButtonDrawable(int resid) {
        mButton1.setBackgroundResource(resid);
        mButton2.setBackgroundResource(resid);
        return this;
    }
    public ZhycanNiftyDialogBuilder withButton1Text(CharSequence text) {
        mButton1.setVisibility(View.VISIBLE);
        mButton1.setText(text);
        return this;
    }
    public ZhycanNiftyDialogBuilder withButton2Text(CharSequence text) {
        mButton2.setVisibility(View.VISIBLE);
        mButton2.setText(text);
        return this;
    }

    public ZhycanNiftyDialogBuilder withTypeface(Typeface typeface) {
        mTitle.setTypeface(typeface);
        mMessage.setTypeface(typeface);
        mButton1.setTypeface(typeface);
        mButton2.setTypeface(typeface);

        return this;
    }

    public ZhycanNiftyDialogBuilder withMessageType(String type) {
        switch (type) {
            case "success":
                mButton1.setBackground(getContext().getResources().getDrawable(R.drawable.btn_selector_success));
                mButton2.setTextColor(getContext().getResources().getColor(R.color.btn_success_unpress_color));
                break;
            case "error":
                mButton1.setBackground(getContext().getResources().getDrawable(R.drawable.btn_selector_error));
                mButton2.setTextColor(getContext().getResources().getColor(R.color.btn_error_unpress_color));
                break;
            default:
                mButton1.setBackground(getContext().getResources().getDrawable(R.drawable.btn_selector));
                mButton2.setTextColor(getContext().getResources().getColor(R.color.btn_unpress_color));
                break;
        }

        return this;
    }

    public ZhycanNiftyDialogBuilder setButton1Click(View.OnClickListener click) {
        mButton1.setOnClickListener(click);
        return this;
    }
    
    public ZhycanNiftyDialogBuilder setButton2Click(View.OnClickListener click) {
        mButton2.setOnClickListener(click);
        return this;
    }

    public ZhycanNiftyDialogBuilder setTitleTypeface(Typeface typeface) {
        mTitle.setTypeface(typeface);
        return this;
    }

    public ZhycanNiftyDialogBuilder setMessageTypeface(Typeface typeface) {
        mMessage.setTypeface(typeface);
        return this;
    }

    public ZhycanNiftyDialogBuilder setButtonsTypeface(Typeface typeface) {
        mButton1.setTypeface(typeface);
        mButton2.setTypeface(typeface);
        return this;
    }

    public ZhycanNiftyDialogBuilder setCustomView(int resId, Context context) {
        View customView = View.inflate(context, resId, null);
        if (mFrameLayoutCustomView.getChildCount()>0){
            mFrameLayoutCustomView.removeAllViews();
        }
        mFrameLayoutCustomView.addView(customView);
        return this;
    }
    
    public ZhycanNiftyDialogBuilder setCustomView(View view, Context context) {
        if (mFrameLayoutCustomView.getChildCount()>0){
            mFrameLayoutCustomView.removeAllViews();
        }
        mFrameLayoutCustomView.addView(view);
        
        return this;
    }
    public ZhycanNiftyDialogBuilder isCancelableOnTouchOutside(boolean cancelable) {
        this.isCancelable=cancelable;
        this.setCanceledOnTouchOutside(cancelable);
        return this;
    }
    
    public ZhycanNiftyDialogBuilder isCancelable(boolean cancelable) {
        this.isCancelable=cancelable;
        this.setCancelable(cancelable);
        return this;
    }
    
    private void toggleView(View view,Object obj){
        if (obj==null){
            view.setVisibility(View.GONE);
        }else {
            view.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void show() {
        super.show();
    }
    
    private void start(Effectstype type){
        BaseEffects animator = type.getAnimator();
        if(mDuration != -1){
            animator.setDuration(Math.abs(mDuration));
        }
        animator.start(mRelativeLayoutView);
    }
    
    @Override
    public void dismiss() {
        super.dismiss();
        mButton1.setVisibility(View.GONE);
        mButton2.setVisibility(View.GONE);
    }
}
