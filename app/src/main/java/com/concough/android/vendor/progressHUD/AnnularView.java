/*
 *    Copyright 2015 Kaopiz Software Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.concough.android.vendor.progressHUD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.concough.android.concough.R;
import com.concough.android.vendor.progressHUD.*;
import com.concough.android.vendor.progressHUD.Helper;

public class AnnularView extends View implements Determinate {

    private Paint mWhitePaint;
    private Paint mGreyPaint;
    private RectF mBound;
    private int mMax = 100;
    private int mProgress = 0;
    private int color = 0;

    public AnnularView(Context context) {
        super(context);
        init(context);
    }

    public AnnularView(Context context, int color) {
        super(context);
        this.color = color;
        init(context);
    }

    public AnnularView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnnularView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWhitePaint.setStyle(Paint.Style.STROKE);
        mWhitePaint.setStrokeWidth(Helper.dpToPixel(3, getContext()));
        if (this.color != 0)
            mWhitePaint.setColor(this.color);
        else
            mWhitePaint.setColor(Color.WHITE);

        mGreyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGreyPaint.setStyle(Paint.Style.STROKE);
        mGreyPaint.setStrokeWidth(Helper.dpToPixel(3, getContext()));

        if (this.color != 0) {
//            int tempColor = context.getResources().getColor(this.color);
            int actualColor = Color.argb(
                    40,
                    Color.red(this.color),
                    Color.green(this.color),
                    Color.blue(this.color)
            );
            mGreyPaint.setColor(actualColor);

        }
        else
            mGreyPaint.setColor(context.getResources().getColor(R.color.kprogresshud_grey_color));

        mBound = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int padding = Helper.dpToPixel(4, getContext());
        mBound.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float mAngle = mProgress * 360f / mMax;
        canvas.drawArc(mBound, 270, mAngle, false, mWhitePaint);
        canvas.drawArc(mBound, 270 + mAngle, 360 - mAngle, false, mGreyPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int dimension = Helper.dpToPixel(40, getContext());
        setMeasuredDimension(dimension, dimension);
    }

    @Override
    public void setMax(int max) {
        this.mMax = max;
    }

    @Override
    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }
}
