package com.concough.android.settings

import android.util.Log

/**
 * Created by abolfazl on 7/2/17.
 */

// Application Version
val APP_VERSION = 1
val API_VERSION = "v2"


val ADD_CODE = "ncdjncdujb"
val P_CODE = "67mnnv^vs7&^v87YrV&hd8bw92bu9b%\$\$#b8728^%93y6==37yb&BBB6*njs*99__=="

// Host Urls
val BASE_URL = "http://192.168.0.21:8000/api/"
//val BASE_URL = "https://concough.zhycan.com/api/"
val MEDIA_CLASS_NAME = "media"

val ACTIVITY_CLASS_NAME = "activities"
val ARCHIVE_CLASS_NAME = "archive"
val OAUTH_CLASS_NAME = "oauth"
val JAUTH_CLASS_NAME = "jauth"
val AUTH_CLASS_NAME = "auth"
val PROFILE_CLASS_NAME = "profile"
val ENTRANCE_CLASS_NAME = "entrance"
val PURCHASED_CLASS_NAME = "purchased"
val PRODUCT_CLASS_NAME = "product"
val BASKET_CLASS_NAME = "basket"
val DEVICE_CLASS_NAME = "device"
val JWT_URL_PREFIX = "j"

// Connection settings
val CONNECT_TIMEOUT: Long = 30
val READ_TIMEOUT: Long = 30

// OAuth Settings
val OAUTH_METHOD = "jwt"        // can be "jwt" or "oauth"
val CLIENT_ID = "vKREqBOlXXVZNqWdAGTYio8W6Rhe4SpTAtCZb6Ra"
val CLIENT_PASSWORD = "uAnxNKjqK1b5i0Y3SYpCWnyjORQR14JIpOHchse0alsYpqIVrpy2C9Fu095anIrM6v3yft0pDjO8eGu5G8q5UDs7WjMEqpHUVwg9x6QHrIlW6NR2DZiUJD0njCaqkBaL"


// KeyChain Keys
val OAUTH_TOKEN_KEY = "oauthToken"
val OAUTH_REFRESH_TOKEN_KEY = "oauthRefreshToken"
val OAUTH_TOKEN_TYPE_KEY = "oauthTokenType"
val OAUTH_LAST_ACCESS_KEY = "oauthLastAccess"
val OAUTH_EXPIRES_IN_KEY = "oauthExpiresIn"
public val USERNAME_KEY: String = "authUsername"
public val PASSWORD_KEY: String = "authPassword"

// Validator Regex
val EMAIL_VALIDATOR_REGEX = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
val USERNAME_VALIDATOR_REGEX = "^[A-Za-z0-9][A-Za-z0-9@+-@._]{9,29}$"
val PHONE_NUMBER_VALIDATOR_REGEX = "^(9|09)[0-9]{9}$"


// UI Constants
val BLUE_COLOR_HEX: Int = 0x1007AFF
val RED_COLOR_HEX: Int = 0x960000
val RED_COLOR_HEX_2: Int = 0xDD0000
val GREEN_COLOR_HEX: Int = 0x1008000
val GRAY_COLOR_HEX_1: Int = 0xB7B7B7

val MEDIA_CACHE_SIZE = 1024 * 1024 * 10

val SECRET_KEY: String
    get() {
        val codeArray = intArrayOf(31, 29, 23, 19, 17, 15, 13, 11, 7)
        val codeArrayString = codeArray.joinToString("")
        val finalString = P_CODE + codeArrayString + ADD_CODE
        Log.d("rrr", finalString)
        return finalString
    }

// Downloader Settings
val DOWNLOAD_IMAGE_COUNT = 15
val CONNECTION_MAX_RETRY = 5
