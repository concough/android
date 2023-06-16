package com.concough.android.extensions

import android.os.Build
import android.util.LayoutDirection
import android.view.Gravity
import android.view.View
import android.widget.EditText

/**
 * Created by FaridM on 12/24/2017.
 */

fun EditText.DirectionFix() {
    val localText = this
    if (localText.text.isNotEmpty()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            localText.textDirection = View.TEXT_DIRECTION_LTR
            localText.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                localText.textDirection = View.TEXT_DIRECTION_LTR
                localText.gravity = Gravity.START
            } else {
                localText.gravity = Gravity.START
            }
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            localText.textDirection = View.TEXT_DIRECTION_RTL
            localText.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                localText.textDirection = View.TEXT_DIRECTION_LTR
                localText.gravity = Gravity.END
            } else {
                localText.gravity = Gravity.END
            }
        }
    }
}

