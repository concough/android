package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton
import com.concough.android.structures.EntranceLessonExamStructure
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

/**
 * Created by abolfazl on 11/22/18.
 */
class EntranceLessonExamModelHandler {
    companion object {
        const val TAG: String = "EntranceLessonExamModelHandler"

        @JvmStatic
        fun add(context: Context, username: String, entranceUniqueId: String,
                examStructure: EntranceLessonExamStructure, created: Date, data: String): Boolean {

            val exam = EntranceLessonExamModel()
            exam.username = username
            exam.entranceUniqueId = entranceUniqueId
            exam.uniqueId = UUID.randomUUID().toString()
            exam.created = created
            exam.falseAnswer = examStructure.falseAnswer
            exam.trueAnswer = examStructure.trueAnswer
            exam.noAnswer = examStructure.noAnswer
            exam.startedDate = examStructure.started
            exam.finishedDate = examStructure.finished
            exam.lessonOrder = examStructure.order!!
            exam.lessonTitle = examStructure.title
            exam.questionCount = examStructure.qCount!!
            exam.withTime = examStructure.withTime
            exam.examDuration = examStructure.duration!!
            exam.examData = data
            exam.percentage = examStructure.percentage
            exam.bookletOrder = examStructure.bookletOrder!!

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(exam)
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }

        @JvmStatic
        fun getLastExam(context: Context, username: String, entranceUniqueId: String,
                        lessonTitle: String, lessonOrder: Int, bookletOrder: Int): EntranceLessonExamModel? {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLessonExamModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("lessonOrder", lessonOrder)
                    .equalTo("lessonTitle", lessonTitle)
                    .equalTo("bookletOrder", bookletOrder)
                    .findAllSorted("created", Sort.DESCENDING)
                    .firstOrNull()
        }

        @JvmStatic
        fun getAllExam(context: Context, username: String, entranceUniqueId: String,
                        lessonTitle: String, lessonOrder: Int, bookletOrder: Int): RealmResults<EntranceLessonExamModel> {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLessonExamModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("lessonOrder", lessonOrder)
                    .equalTo("lessonTitle", lessonTitle)
                    .equalTo("bookletOrder", bookletOrder)
                    .findAllSorted("created", Sort.DESCENDING)
        }

        @JvmStatic
        fun getExamCount(context: Context, username: String, entranceUniqueId: String,
                       lessonTitle: String, lessonOrder: Int, bookletOrder: Int): Long {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLessonExamModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("lessonOrder", lessonOrder)
                    .equalTo("lessonTitle", lessonTitle)
                    .equalTo("bookletOrder", bookletOrder)
                    .count()
        }

        @JvmStatic
        fun getPercentageSum(context: Context, username: String, entranceUniqueId: String,
                         lessonTitle: String, lessonOrder: Int, bookletOrder: Int): Number {
            return RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLessonExamModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("lessonOrder", lessonOrder)
                    .equalTo("lessonTitle", lessonTitle)
                    .equalTo("bookletOrder", bookletOrder)
                    .sum("percentage")
        }

        @JvmStatic
        fun deleteAllExams(context: Context) {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLessonExamModel::class.java).findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    items.deleteAllFromRealm()
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }
        }

        @JvmStatic
        fun removeAllExamsByEntranceId(context: Context, username: String, entranceUniqueId: String): Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLessonExamModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    items.deleteAllFromRealm()
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }

        @JvmStatic
        fun removeAllExamsByUsername(context: Context, username: String): Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(EntranceLessonExamModel::class.java)
                    .equalTo("username", username)
                    .findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    items.deleteAllFromRealm()
                }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }

    }
}