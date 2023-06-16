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
        fun update(context: Context, username: String, entranceUniqueId: String, type: String) : Boolean {
            val record = EntranceOpenedCountModelHandler.getByType(context, username, entranceUniqueId, type)
            if (record != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        record.count += 1
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()

                    return true
                } catch (exc: Exception) {
//                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            } else {
                val record = EntranceOpenedCountModel()
                record.entranceUniqueId = entranceUniqueId
                record.type = type
                record.count = 1
                record.username = username

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealm(record)
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
//                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return false
        }

        @JvmStatic
        fun getByType(context: Context, username: String, entranceUniqueId: String, type: String) : EntranceOpenedCountModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceOpenedCountModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId).equalTo("type", type)
                    .equalTo("username", username)
                    .findFirst()
        }

        @JvmStatic
        fun countByEntranceId(context: Context, username: String, entranceUniqueId: String) : Int {
            val totalCount = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceOpenedCountModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("username", username)
                    .findAll().sum("count")

            return totalCount?.toInt() ?: 0

        }

        @JvmStatic
        fun removeByEntranceId(context: Context, username: String, entranceUniqueId: String): Boolean {
            val opened = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceOpenedCountModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId).findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    opened.deleteAllFromRealm()
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
            } catch (exc: Exception) {
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                return false
            }

            return true
        }
    }
}