package com.concough.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.concough.android.settings.SECRET_KEY
import com.securepreferences.SecurePreferences


/**
 * Created by abolfazl on 7/4/17.
 */

class KeyChainAccessProxy {

    private var prefs: SharedPreferences

    private constructor(context: Context) {
        this.prefs = SecurePreferences(context, SECRET_KEY, FILENAME)
    }

    companion object Factory {
        val TAG: String = "KeyChainAccessProxy"
        val FILENAME = "usera.xml"

        private var sharedInstance : KeyChainAccessProxy? = null

        @JvmStatic
        fun  getInstance(context: Context): KeyChainAccessProxy {
            if (sharedInstance == null)
                sharedInstance = KeyChainAccessProxy(context)

            return sharedInstance!!
        }
    }

    public fun setValueAsString(key: String, value: String): Boolean {
        this.prefs.edit().putString(key, value).apply()
        return true
    }

    public fun getValueAsString(key: String): String {
        return this.prefs.getString(key, "")
    }

    public fun setValueAsInt(key: String, value: Int): Boolean {
        this.prefs.edit().putInt(key, value).apply()
        return true
    }

    public fun getValueAsInt(key: String): Int {
        return this.prefs.getInt(key, 0)
    }

    public fun removeValue(key: String): Boolean {
        this.prefs.edit().remove(key).apply()
        return true
    }

    public fun clearAllValue(): Boolean {
        this.prefs.edit().clear().apply()
        return true
    }
}