package com.concough.android.utils

import android.provider.SyncStateContract.Helpers.update
import java.security.NoSuchAlgorithmException


/**
 * Created by abolfazl on 8/6/17.
 */

class MD5Digester {
    companion object {
        fun digest(s: String): String {
            try {
                // Create MD5 Hash
                val digest = java.security.MessageDigest.getInstance("MD5")
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuffer()
                for (i in messageDigest.indices)
                    hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
                return hexString.toString()

            } catch (e: Exception) {
            }

            return ""
        }
    }
}

