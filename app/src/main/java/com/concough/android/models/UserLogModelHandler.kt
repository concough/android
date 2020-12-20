package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton
import com.google.gson.JsonObject
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

/**
 * Created by abolfazl on 7/12/17.
 */
class UserLogModelHandler {
    companion object Factory {
        val TAG: String = "UserLogModelHandler"

        @JvmStatic
        fun add(context: Context, username: String, uniqueId: String, created: Date, logType: String, extraData: JsonObject): Boolean {
            val log = UserLogModel()
            log.created = created
            log.extraData = extraData.toString()
            log.isSynced = false
            log.logType = logType
            log.uniqueId = uniqueId
            log.username = username

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealm(log)
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }

        @JvmStatic
        fun list(context: Context, username: String): RealmResults<UserLogModel> {
            return RealmSingleton.getInstance(context).DefaultRealm.where(UserLogModel::class.java)
                    .equalTo("username", username).sort("created", Sort.ASCENDING).findAll()
        }

        @JvmStatic
        fun removeByUniqueId(context: Context, username: String, uniqueId: String): Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(UserLogModel::class.java)
                    .equalTo("username", username)
                    .equalTo("uniqueId", uniqueId)
                    .findAll()

            if (items != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        items.deleteAllFromRealm()
                    }
                } catch (exc: Exception) {
                    return false
                }
            }

            return true
        }
    }
}