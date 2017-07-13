package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceLessonModelHandler {
    companion object Factory {
        val TAG: String = "EntranceLessonModelHandler"

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
                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
                RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(lesson)
                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return lesson
            } catch (exc: Exception) {
            }

            return null
        }
    }
}
