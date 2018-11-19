package com.concough.android.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by abolfazl on 11/17/18.
 */

public fun hideKeyboard(view: View): Unit {
    val inputMethodManager: InputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE)
            as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}