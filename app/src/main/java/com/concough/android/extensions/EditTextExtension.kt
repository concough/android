package com.concough.android.extensions

import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.EditText

/**
 * Created by FaridM on 12/24/2017.
 */

fun EditText.DirectionFix(): Unit {
    val localText = this
    if (localText.text.toString().length > 0) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            localText.setTextDirection(View.TEXT_DIRECTION_LTR)
            localText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                localText.setGravity(Gravity.START)
            } else {
                localText.setGravity(Gravity.END)
            }
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            localText.setTextDirection(View.TEXT_DIRECTION_RTL)
            localText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                localText.setGravity(Gravity.END)
            } else {
                localText.setGravity(Gravity.START)
            }
        }
    }
}

