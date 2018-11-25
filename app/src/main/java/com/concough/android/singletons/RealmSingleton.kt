package com.concough.android.singletons

import android.content.Context
import android.util.Log
import com.concough.android.models.ModelMigration
import com.concough.android.settings.SECRET_KEY
import io.realm.*
import io.realm.DynamicRealmObject
import io.realm.RealmObjectSchema
import io.realm.FieldAttribute




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
        Realm.init(context)
        val config = RealmConfiguration.Builder().schemaVersion(5).migration(ModelMigration()).encryptionKey(SECRET_KEY.toByteArray().copyOfRange(0,64)).build()

        try {
            this.realm = Realm.getInstance(config)

            val folderPath = this.realm?.configuration?.path
            Log.d(TAG, folderPath)

        } catch (exc: Exception) {
            Log.d(TAG, exc.toString())
        }
    }

    val DefaultRealm: Realm
        get() = this.realm!!


}