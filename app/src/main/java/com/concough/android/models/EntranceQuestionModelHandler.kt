package com.concough.android.models

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.concough.android.singletons.RealmSingleton
import io.realm.RealmResults
import io.realm.Sort

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceQuestionModelHandler {
    companion object Factory {
        val TAG: String = "EntranceQuestionModelHandler"

        @SuppressLint("LongLogTag")
        @JvmStatic
        fun add(context: Context, uniqueId: String, number: Int, answer: Int, images: String, isDownloaded: Boolean, entrance: EntranceModel): EntranceQuestionModel? {
            val question = EntranceQuestionModel()
            question.answer = answer
            question.entrance = entrance
            question.images = images
            question.isDownloaded = isDownloaded
            question.number = number
            question.uniqueId = uniqueId

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealm(question)
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return question
            } catch (exc: Exception) {
                Log.d(TAG, exc.message)
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return null
        }

        @JvmStatic
        fun bulkDelete(context: Context, username: String, list: RealmResults<EntranceQuestionModel>): Boolean {
            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    list.deleteAllFromRealm()
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return false
            } catch (exc: Exception) {
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }
            return true
        }

        @JvmStatic
        fun changeDownloadedToTrue(context: Context, username: String, uniqueId: String, entranceId: String) : Boolean {
            val question = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("uniqueId", uniqueId)
                    .equalTo("entrance.username", username)
                    .equalTo("entrance.uniqueId", entranceId).equalTo("isDownloaded", false).findFirst()

            if (question != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        question.isDownloaded = true
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

        @JvmStatic
        fun getQuestions(context: Context, username: String, entranceId: String): RealmResults<EntranceQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.username", username)
                    .equalTo("entrance.uniqueId", entranceId)
                    .findAllSorted("number", Sort.ASCENDING)
        }

        fun getStarredQuestions(context: Context, username: String, entranceId: String, questions: Array<String>): RealmResults<EntranceQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.uniqueId", entranceId)
                    .equalTo("entrance.username", username)
                    .`in`("uniqueId", questions).findAllSorted("number", Sort.ASCENDING)
        }

        @JvmStatic
        fun getQuestionsNotDownloaded(context: Context, username: String, entranceId: String): RealmResults<EntranceQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.username", username)
                    .equalTo("entrance.uniqueId", entranceId)
                    .equalTo("isDownloaded", false).findAllSorted("number", Sort.ASCENDING)
        }

        @JvmStatic
        fun getQuestionById(context: Context, username: String, entranceId: String, questionId: String): EntranceQuestionModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.uniqueId", entranceId)
                    .equalTo("entrance.username", username)
                    .equalTo("uniqueId", questionId).findFirst()
        }

        @JvmStatic
        fun countQuestions(context: Context, username: String, entranceId: String): Long {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.username", username)
                    .equalTo("entrance.uniqueId", entranceId)
                    .count()
        }

    }
}