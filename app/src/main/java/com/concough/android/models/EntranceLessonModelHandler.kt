package com.concough.android.models

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.concough.android.singletons.RealmSingleton
import io.realm.RealmResults

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceLessonModelHandler {
    companion object Factory {
        const val TAG: String = "EntranceLessonModelHandler"

        @SuppressLint("LongLogTag")
        @JvmStatic
        fun add(context: Context, uniqueId: String, title: String, fullTitle: String, qStart: Int, qEnd: Int, qCount: Int, order: Int, duration: Int): EntranceLessonModel? {

            val lesson = EntranceLessonModel()
            lesson.duration = duration
            lesson.fullTitle = fullTitle
            lesson.order = order
            lesson.qCount = qCount
            lesson.qStart = qStart
            lesson.qEnd = qEnd
            lesson.title = title
            lesson.uniqueId = uniqueId

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(lesson)
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return lesson
            } catch (exc: Exception) {
//                Log.d(TAG, exc.message)
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return null
        }

        @JvmStatic
        fun getAllLessons(context: Context, username: String): ArrayList<EntranceLessonModel> {
            var items = ArrayList<EntranceLessonModel>()

            val entrances = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceModel::class.java)
                    .equalTo("username", username).findAll()

            if (entrances.count() > 0) {
                for (ent in entrances) {
                    for (booklet in ent.booklets) {
                        for (lesson in booklet.lessons) {
                            items.add(lesson)
                        }
                    }
                }
            }
            return items
        }

        @JvmStatic
        fun getOneLessonByTitleAndOrder(context: Context, username: String, entranceUniqueId: String,
                                        lessonTitle: String, lessonOrder: Int): EntranceLessonModel? {

            val entrance = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceModel::class.java)
                    .equalTo("username", username)
                    .equalTo("uniqueId", entranceUniqueId)
                    .findFirst()

            entrance?.let {
                for (booklet in it.booklets) {
                    for (lesson in booklet.lessons) {
                        if (lesson.fullTitle == lessonTitle && lesson.order == lessonOrder) {
                            return lesson
                        }
                    }
                }
            }

            return null
        }
    }
}
