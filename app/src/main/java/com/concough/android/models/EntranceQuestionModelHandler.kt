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

        fun bulkDelete(context: Context, list: RealmResults<EntranceQuestionModel>): Boolean {
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

        fun changeDownloadedToTrue(context: Context, uniqueId: String, entranceId: String) : Boolean {
            val question = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("uniqueId", uniqueId).equalTo("entrance.uniqueId", entranceId).equalTo("isDownloaded", false).findFirst()

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

        fun getQuestions(context: Context, entranceId: String): RealmResults<EntranceQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.uniqueId", entranceId).findAllSorted("number", Sort.ASCENDING)
        }

        fun getStarredQuestions(context: Context, entranceId: String, questions: Array<String>): RealmResults<EntranceQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.uniqueId", entranceId)
                    .`in`("uniqueId", questions).findAllSorted("number", Sort.ASCENDING)
        }

        fun getQuestionsNotDownloaded(context: Context, entranceId: String): RealmResults<EntranceQuestionModel>? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.uniqueId", entranceId).equalTo("isDownloaded", false).findAllSorted("number", Sort.ASCENDING)
        }

        fun getQuestionById(context: Context, entranceId: String, questionId: String): EntranceQuestionModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.uniqueId", entranceId)
                    .equalTo("uniqueId", questionId).findFirst()
        }

        fun countQuestions(context: Context, entranceId: String): Long {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceQuestionModel::class.java)
                    .equalTo("entrance.uniqueId", entranceId).count()
        }

    }
}