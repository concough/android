package com.concough.android.extensions;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.concough.android.concough.R;

/**
 * Created by Owner on 7/30/2017.
 */

public class RotateViewExtensions {

    public static void buttonRotateStart(View view, final Context context) {
        Animation rotationAnim = AnimationUtils.loadAnimation(context, R.anim.concough_rotate);
        rotationAnim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(rotationAnim);
    }


    public static void buttonRotateStop(View view, final Context context) {

        Animation rotationAnim = AnimationUtils.loadAnimation(context, R.anim.concough_rotate);
        rotationAnim.setRepeatCount(Animation.INFINITE);
        view.clearAnimation();

    }
}
