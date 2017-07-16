package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton
import io.realm.RealmResults

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceQuestionStarredModelHandler {
    companion object Factory {
        val TAG: String = "EntranceQuestionStarredModelHandler"

        @JvmStatic
        fun add(context: Context, entranceUniqueId: String, questionId: String): Boolean {
            if (EntranceQuestionStarredModelHandler.get(context, entranceUniqueId, questionId) != null) return true

            val question = EntranceQuestionModelHandler.getQuestionById(context, entranceUniqueId, questionId)
            if (question != null) {
                val star = EntranceStarredQuestionModel()
                star.entranceUniqueId = entranceUniqueId
                star.question = question

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealm(star)
                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {}
            }
            return false
        }

        @JvmStatic
        fun getStarredQuestions(context: Context, entranceUniqueId: String): RealmResults<EntranceStarredQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceStarredQuestionModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .findAll()
        }

        @JvmStatic
        fun get(context: Context, entranceUniqueId: String, questionId: String): EntranceStarredQuestionModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceStarredQuestionModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId).equalTo("question.uniqueId", questionId).findFirst()
        }

        @JvmStatic
        fun remove(context: Context, entranceUniqueId: String, questionId: String): Boolean {
            val star = EntranceQuestionStarredModelHandler.get(context, entranceUniqueId, questionId)
            if (star != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
                    star.deleteFromRealm()
                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {}
            }
            return  false
        }

        @JvmStatic
        fun countByEntranceId(context: Context, entranceUniqueId: String): Int {
            return this.getStarredQuestions(context, entranceUniqueId)?.count()  ?: 0
        }

        @JvmStatic
        fun removeByEntranceId(context: Context, entranceUniqueId: String) : Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceStarredQuestionModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId).findAll()
            if (items != null) {
                try {
                    items.deleteAllFromRealm()
                } catch (exc: Exception) {
                    return false
                }
            }

            return true
        }
    }
}