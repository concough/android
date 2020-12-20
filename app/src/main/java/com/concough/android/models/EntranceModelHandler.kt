package com.concough.android.models

import android.content.Context
import android.util.Log
import com.concough.android.singletons.RealmSingleton
import com.concough.android.structures.EntranceStruct
import java.util.*

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceModelHandler {
    companion object Factory {
        val TAG: String = "EntranceModelHandler"

        @JvmStatic
        fun add(context: Context, username: String, e: EntranceStruct): Boolean {

            val entrance = EntranceModel()
            entrance.bookletsCount = e.entranceBookletCounts!!
            entrance.duration = e.entranceDuration!!
            if (e.entranceExtraData != null)
                entrance.extraData = e.entranceExtraData?.asJsonObject.toString()
            entrance.group = e.entranceGroupTitle!!
            entrance.lastPublished = e.entranceLastPublished!!
            entrance.organization = e.entranceOrgTitle!!
            entrance.set = e.entranceSetTitle!!
            entrance.setId = e.entranceSetId!!
            entrance.type = e.entranceTypeTitle!!
            entrance.uniqueId = e.entranceUniqueId!!
            entrance.year = e.entranceYear!!
            entrance.month = e.entranceMonth!!
            entrance.username = username
            entrance.pUniqueId = "$username-${e.entranceUniqueId!!}"

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(entrance)
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
                Log.d("cscsdcds", exc.message)
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }

        @JvmStatic
        fun existById(context: Context, username: String, id: String): Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceModel::class.java)
                    .equalTo("username", username).equalTo("uniqueId", id).findAll()

            if (items.count() > 0) {
                return true
            }
            return false
        }

        @JvmStatic
        fun getByUsernameAndId(context: Context, username: String, id: String): EntranceModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceModel::class.java)
                    .equalTo("username", username).equalTo("uniqueId", id).findFirst()
        }

        @JvmStatic
        fun removeById(context: Context, username: String, id: String): Boolean {
            val entrance = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceModel::class.java)
                    .equalTo("username", username).equalTo("uniqueId", id).findFirst()

            if (entrance != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        entrance.deleteFromRealm()
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
//                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }

                return true
            }
            return false
        }

    }
}