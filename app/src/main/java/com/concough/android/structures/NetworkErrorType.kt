package com.concough.android.structures

import android.util.Log
import java.io.IOException
import java.net.ConnectException

/**
 * Created by abolfazl on 7/2/17.
 */
enum class NetworkErrorType(val code: String) {
    NoInternetAccess("NoInternetAccess"),
    HostUnreachable("HostUnreachable"),
    UnKnown("UnKnown");

    companion object FActory {
        val TAG = "NetworkErrorType"

        fun toType(error: Throwable?): NetworkErrorType {
            Log.d(TAG, error.toString())
            if (error is ConnectException) {
                return HostUnreachable
            }
            return NetworkErrorType.UnKnown
        }
    }
}