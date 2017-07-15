package com.concough.android.singletons

import android.content.Context
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by abolfazl on 7/12/17.
 */
class RealmSingleton {
    private var realm: Realm? = null

    companion object Factory {
        val TAG: String = "RealmSingleton"

        private var sharedInstance : RealmSingleton? = null

        @JvmStatic
        fun  getInstance(context: Context): RealmSingleton {
            if (sharedInstance == null)
                sharedInstance = RealmSingleton(context)

            return sharedInstance!!
        }
    }

    private constructor(context: Context) {
        val config = RealmConfiguration.Builder().build()
        try {
            this.realm = Realm.getInstance(config)

            val folderPath = this.realm?.configuration?.path
            Log.d(TAG, folderPath)

        } catch (exc: Exception) {
        }
    }

    val DefaultRealm: Realm
        get() = this.realm!!


}