package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceOpenedCountModelHandler {
    companion object Factory {
        val TAG: String = "EntranceOpenedCountModelHandler"

        @JvmStatic
        fun update(context: Context, entranceUniqueId: String, type: String) : Boolean {
            val record = EntranceOpenedCountModelHandler.getByType(context, entranceUniqueId, type)
            if (record != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
                    record.count += 1
                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()

                    return true
                } catch (exc: Exception) {}
            } else {
                val record = EntranceOpenedCountModel()
                record.entranceUniqueId = entranceUniqueId
                record.type = type
                record.count = 1

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealm(record)
                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {}
            }

            return false
        }

        @JvmStatic
        fun getByType(context: Context, entranceUniqueId: String, type: String) : EntranceOpenedCountModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceOpenedCountModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId).equalTo("type", type).findFirst()
        }

        @JvmStatic
        fun countByEntranceId(context: Context, entranceUniqueId: String) : Int {
            val totalCount = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceOpenedCountModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId).findAll().sum("count")

            return totalCount?.toInt() ?: 0

        }

        fun removeByEntranceId(context: Context, entranceUniqueId: String): Boolean {
            val opened = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceOpenedCountModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId).findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
                opened.deleteAllFromRealm()
                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
            } catch (exc: Exception) {
                return false
            }

            return true
        }
    }
}