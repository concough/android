package com.concough.android.utils


/**
 * Created by abolfazl on 8/6/17.
 */

class MD5Digester {
    companion object {

        @JvmStatic
        fun digest(s: String): String {
            try {
                // Create MD5 Hash
                val digest = java.security.MessageDigest.getInstance("MD5")
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                var hexString = ""
                for (i in messageDigest.indices)
                    hexString+=String.format("%02x",0xFF and messageDigest[i].toInt())
                return hexString.toString()

            } catch (e: Exception) {
            }

            return ""
        }
    }
}

