package com.concough.android.models

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.concough.android.singletons.RealmSingleton
import java.util.*

/**
 * Created by abolfazl on 10/11/18.
 */
class EntranceLastVisitInfoModelHandler {
    companion object {
        const val TAG: String = "EntranceLastVisitInfoModelHandler"

        @SuppressLint("LongLogTag")
        @JvmStatic
        fun update(context: Context, username: String, uniqueId: String, bookletIndex: Int,
                   lessonIndex: Int, index: String, updated: Date, showType: String): Boolean {
            val elv = EntranceLastVisitInfoModelHandler.get(context, username, uniqueId, showType)
            if (elv != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        elv.bookletIndex = bookletIndex
                        elv.lessonIndex = lessonIndex
                        elv.index = index
                        elv.updated = updated

                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(elv)
                    }
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            } else {
                val lvi = EntranceLastVisitInfoModel()
                lvi.username = username
                lvi.entranceUniqueId = uniqueId
                lvi.bookletIndex = bookletIndex
                lvi.lessonIndex = lessonIndex
                lvi.index = index
                lvi.updated = updated
                lvi.showType = showType

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(lvi)
                    }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                   Log.d(TAG, exc.printStackTrace().toString())
                }
            }

            return false
        }

        @JvmStatic
        fun get(context: Context, username: String, uniqueId: String, showType: String): EntranceLastVisitInfoModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLastVisitInfoModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", uniqueId)
                    .equalTo("showType", showType)
                    .findFirst()
        }
    }
}