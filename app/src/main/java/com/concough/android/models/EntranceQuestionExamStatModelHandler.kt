package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton
import java.util.*

/**
 * Created by abolfazl on 11/24/18.
 */
class EntranceQuestionExamStatModelHandler {
    companion object {
        const val TAG: String = "EntranceQuestionExamStatModelHandler"

        @JvmStatic
        fun update(context: Context, username: String, entranceUniqueId: String, questionNo: Int,
                   answerState: Int): Boolean {

            val stat = EntranceQuestionExamStatModelHandler.getByNo(context, username,
                    entranceUniqueId, questionNo)

            if (stat != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        stat.updated = Date()
                        stat.totalCount += 1

                        when(answerState) {
                            1 -> stat.trueCount += 1
                            0 -> stat.emptyCount += 1
                            -1 -> stat.falseCount += 1
                        }

                        stat.statData += ",$answerState"

                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(stat)
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            } else {
                val newStat = EntranceQuestionExamStatModel()
                newStat.uniqueId = UUID.randomUUID().toString()
                newStat.created = Date()
                newStat.updated = Date()
                newStat.totalCount = 1

                when(answerState) {
                    1 -> newStat.trueCount = 1
                    0 -> newStat.emptyCount = 1
                    -1 -> newStat.falseCount = 1
                }

                newStat.entranceUniqueId = entranceUniqueId
                newStat.questionNo = questionNo
                newStat.username = username
                newStat.statData = "$answerState"

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(newStat)
                    }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return false
        }

        @JvmStatic
        fun getByNo(context: Context, username: String, entranceUniqueId: String, questionNo: Int): EntranceQuestionExamStatModel? {
            return RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionExamStatModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("questionNo", questionNo)
                    .findFirst()
        }

        @JvmStatic
        fun deleteAllStats(context: Context) {
            val items = RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionExamStatModel::class.java).findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    items.deleteAllFromRealm()
                }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }
        }

        @JvmStatic
        fun removeAllStatsByEntranceId(context: Context, username: String, entranceUniqueId: String): Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionExamStatModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    items.deleteAllFromRealm()
                }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }

        @JvmStatic
        fun removeAllStatsByUsername(context: Context, username: String): Boolean {
            val items = RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionExamStatModel::class.java)
                    .equalTo("username", username)
                    .findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    items.deleteAllFromRealm()
                }
//                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                return true
            } catch (exc: Exception) {
                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
            }

            return false
        }
    }
}