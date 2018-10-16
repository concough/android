package com.concough.android.settings

/**
 * Created by abolfazl on 10/11/18.
 */

val SUPPORT_ACTIVITY_TYPES = arrayOf("ENTRANCE_CREATE", "ENTRANCE_UPDATE", "ENTRANCE_MULTI")

fun isSupportActivityTypes(title: String): Boolean {
    if (SUPPORT_ACTIVITY_TYPES.contains(title)) {
        return true
    }

    return false
}