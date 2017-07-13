package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton
import com.google.gson.JsonObject
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
                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
                RealmSingleton.getInstance(context).DefaultRealm.copyToRealm(log)
                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {}

            return false
        }
    }
}