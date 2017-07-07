package com.concough.android.utils

import com.auth0.android.jwt.JWT


/**
 * Created by abolfazl on 7/5/17.
 */
class JwtHandler {
    companion object Factory {
        @JvmStatic
        fun getPeyloadData(data: String): JWT {

            val decoded = JWT(data)
            return decoded
        }
    }
}