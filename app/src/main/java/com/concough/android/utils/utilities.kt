package com.concough.android.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import android.util.TypedValue



/**
 * Created by abolfazl on 11/17/18.
 */

public fun hideKeyboard(view: View): Unit {
    val inputMethodManager: InputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE)
            as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun convertFileToByteArray(f: File): ByteArray? {
    var byteArray: ByteArray? = null
    try {
        val inputStream = FileInputStream(f)
        val bos = ByteArrayOutputStream()
        val b = ByteArray(1024 * 8)
        var bytesRead = 0

        do {
            bytesRead = inputStream.read(b)
            if (bytesRead > 0)
                bos.write(b, 0, bytesRead)
        } while (bytesRead != -1)

        byteArray = bos.toByteArray()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return byteArray
}

fun dpToPx(dp: Float, context: Context): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
}

fun spToPx(sp: Float, context: Context): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()
}

fun dpToSp(dp: Float, context: Context): Int {
    return (dpToPx(dp, context) / context.resources.displayMetrics.scaledDensity).toInt()
}

fun spToDp(sp: Float, context: Context): Int {
    return (spToPx(sp, context) / context.resources.displayMetrics.densityDpi)
}