package com.concough.android.models

import android.content.Context
import com.concough.android.singletons.RealmSingleton
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

/**
 * Created by abolfazl on 11/16/18.
 */
class EntranceQuestionCommentModelHandler {
    companion object {
        val TAG: String = "EntranceQuestionCommentModelHandler"

        @JvmStatic
        fun add(context: Context, entranceUniqueId: String, username: String, questionId: String,
                commentType: String, commentData: String) : EntranceQuestionCommentModel? {

            val question = EntranceQuestionModelHandler.getQuestionById(context, username, entranceUniqueId,
                    questionId)
            question?.let {
                val comment = EntranceQuestionCommentModel()
                comment.commentData = commentData
                comment.commentType = commentType
                comment.created = Date()
                comment.username = username
                comment.entranceUniqueId = entranceUniqueId
                comment.question = question
                comment.uniqueId = UUID.randomUUID().toString()

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(comment)
                    }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return comment
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }

            return null
        }

        @JvmStatic
        fun getAllComments(context: Context, entranceUniqueId: String, username: String, questionId: String) : RealmResults<EntranceQuestionCommentModel> {
            return RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionCommentModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("question.uniqueId", questionId)
                    .findAllSorted("created", Sort.DESCENDING)
        }

        @JvmStatic
        fun getLastComment(context: Context, entranceUniqueId: String, username: String, questionId: String): EntranceQuestionCommentModel? {
            return RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionCommentModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("question.uniqueId", questionId)
                    .findAllSorted("created", Sort.DESCENDING).first()
        }

        @JvmStatic
        fun getCommentsCount(context: Context, entranceUniqueId: String, username: String, questionId: String): Long {
            return RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionCommentModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .equalTo("question.uniqueId", questionId)
                    .count()
        }

        @JvmStatic
        fun removeOneComment(context: Context, username: String, commentId: String): Boolean {
            val comment = RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionCommentModel::class.java)
                    .equalTo("username", username)
                    .equalTo("uniqueId", commentId)
                    .findFirst()

            if (comment != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        comment.deleteFromRealm()
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
        fun removeAllCommentOfEntrance(context: Context, username: String, entranceUniqueId: String): Boolean {
            val comments = RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionCommentModel::class.java)
                    .equalTo("username", username)
                    .equalTo("entranceUniqueId", entranceUniqueId)
                    .findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    comments.deleteAllFromRealm()
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
        fun removeAllCommentOfUsername(context: Context, username: String): Boolean {
            val comments = RealmSingleton.getInstance(context).DefaultRealm
                    .where(EntranceQuestionCommentModel::class.java)
                    .equalTo("username", username)
                    .findAll()

            try {
                RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                    comments.deleteAllFromRealm()
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