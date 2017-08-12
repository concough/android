package com.concough.android.models

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.concough.android.singletons.RealmSingleton
import com.concough.android.structures.EntranceStruct
import io.realm.RealmResults
import io.realm.Sort

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceBookletModelHandler {

    companion object Factory {
        val TAG: String = "EntranceBookletModelHandler"

        @SuppressLint("LongLogTag")
        @JvmStatic
        fun add(context: Context, uniqueId: String, title: String, lessonCount: Int, duration: Int, isOptional: Boolean, order: Int): EntranceBookletModel? {

            val booklet = EntranceBookletModel()
            booklet.duration = duration
            booklet.isOptional = isOptional
            booklet.lessonCount = lessonCount
            booklet.order = order
            booklet.title = title
            booklet.uniqueId = uniqueId

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(booklet)
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return booklet
            } catch (exc: Exception) {
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                Log.d(TAG, exc.printStackTrace().toString())
            }

            return null
        }

        @JvmStatic
        fun getBookletByEntranceId(context: Context, username: String, uniqueId: String): RealmResults<EntranceBookletModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceBookletModel::class.java)
                    .equalTo("entrance.username", username)
                    .equalTo("entrance.uniqueId", uniqueId)
                    .findAllSorted("order", Sort.ASCENDING)
        }
    }
}