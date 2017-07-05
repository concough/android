package com.concough.android.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT

/**
 * Created by abolfazl on 7/5/17.
 */
class JwtHandler {
    companion object Factory {
        @JvmStatic
        fun getPeyloadData(data: String): DecodedJWT {

            val decoded = JWT.decode(data)
            return decoded
        }
    }
}