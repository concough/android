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
        fun add(context: Context, username: String, entranceUniqueId: String, questionId: String): Boolean {
            if (EntranceQuestionStarredModelHandler.get(context, username, entranceUniqueId, questionId) != null) return true

            val question = EntranceQuestionModelHandler.getQuestionById(context, username, entranceUniqueId, questionId)
            if (question != null) {
                val star = EntranceStarredQuestionModel()
                star.entranceUniqueId = entranceUniqueId
                star.question = question
                star.username = username

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealm(star)
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
        fun getStarredQuestions(context: Context, username: String, entranceUniqueId: String): RealmResults<EntranceStarredQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceStarredQuestionModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("username", username)
                    .findAll()
        }

        @JvmStatic
        fun get(context: Context, username: String, entranceUniqueId: String, questionId: String): EntranceStarredQuestionModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceStarredQuestionModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("question.uniqueId", questionId)
                    .equalTo("username", username)
                    .findFirst()
        }

        @JvmStatic
        fun remove(context: Context, username: String, entranceUniqueId: String, questionId: String): Boolean {
            val star = EntranceQuestionStarredModelHandler.get(context, username, entranceUniqueId, questionId)
            if (star != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        star.deleteFromRealm()
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
//                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }
            return  false
        }

        @JvmStatic
        fun countByEntranceId(context: Context, username: String, entranceUniqueId: String): Int {
            return this.getStarredQuestions(context, username, entranceUniqueId)?.count()  ?: 0
        }

        @JvmStatic
        fun removeByEntranceId(context: Context, username: String, entranceUniqueId: String) : Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceStarredQuestionModel::class.java)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("username", username)
                    .findAll()
            if (items != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        items.deleteAllFromRealm()
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                } catch (exc: Exception) {
//                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                    return false
                }
            }

            return true
        }
    }
}