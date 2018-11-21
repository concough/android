package com.concough.android.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

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