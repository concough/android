package com.concough.android.structures

import android.util.Log
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * Created by abolfazl on 7/2/17.
 */
enum class NetworkErrorType(val code: String) {
    NoInternetAccess("NoInternetAccess"),
    HostUnreachable("HostUnreachable"),
    Timeout("Timeout"),
    UnKnown("UnKnown");

    companion object FActory {
        val TAG = "NetworkErrorType"

        fun toType(error: Throwable?): NetworkErrorType {
            Log.d(TAG, error.toString())
            if (error is ConnectException) {
                return HostUnreachable
            } else if (error is SocketTimeoutException) {
                return Timeout
            }
            return NetworkErrorType.UnKnown
        }
    }
}