package com.concough.android.models

import android.content.Context
import android.util.Log
import com.concough.android.singletons.RealmSingleton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.realm.Realm
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap

/**
 * Created by abolfazl on 7/12/17.
 */
class EntrancePackageHandler {
    data class EntrancePackageResult(var status: Boolean, var images: LinkedHashMap<String, String>, var questionList: LinkedHashMap<String, ArrayList<Pair<String, Boolean>>>)

    companion object Factory {
        val TAG: String = "EntrancePackageHandler"

        @JvmStatic
        fun savePackage(context: Context, username: String, entranceUniqueId: String, initData: JsonElement): EntrancePackageResult {
            var localImage: LinkedHashMap<String, String> = LinkedHashMap()
            var localList: LinkedHashMap<String, ArrayList<Pair<String, Boolean>>> = LinkedHashMap()

            try {
                val entrance = EntranceModelHandler.getByUsernameAndId(context, username, entranceUniqueId)
                if (entrance != null) {
                    val bookletArray = initData.asJsonObject.get("entrance.booklets").asJsonArray
                    for (item in bookletArray) {
                        val count = item.asJsonObject.get("lessons.count").asInt
                        val title = item.asJsonObject.get("title").asString
                        val duration = item.asJsonObject.get("duration").asInt
                        val isOptional = item.asJsonObject.get("is_optional").asBoolean
                        val order = item.asJsonObject.get("order").asInt
                        val uniqueId = UUID.randomUUID().toString()

                        val booklet = EntranceBookletModelHandler.add(context, uniqueId, title, count, duration, isOptional, order)
                        if (booklet != null) {
//                            RealmSingleton.getInstance(context).DefaultRealm.refresh()
//                            RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                            RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()

                            val lessonArray = item.asJsonObject.get("lessons").asJsonArray
                            for (item2 in lessonArray) {
                                val fullTitle = item2.asJsonObject.get("full_title").asString
                                val qEnd = item2.asJsonObject.get("q_end").asInt
                                val ltitle = item2.asJsonObject.get("title").asString
                                val lduration = item2.asJsonObject.get("duration").asInt
                                val lorder = item2.asJsonObject.get("order").asInt
                                val qCount = item2.asJsonObject.get("q_count").asInt
                                val qStart = item2.asJsonObject.get("q_start").asInt
                                val lUniqueId = UUID.randomUUID().toString()

                                val lesson = EntranceLessonModelHandler.add(context, lUniqueId, ltitle, fullTitle, qStart, qEnd, qCount, lorder, lduration)
                                if (lesson != null) {
//                                    RealmSingleton.getInstance(context).DefaultRealm.refresh()

//                                    RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                                    RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()

                                    val questionsArray = item2.asJsonObject.get("questions").asJsonArray
                                    for (qitem in questionsArray) {
                                        val answer = qitem.asJsonObject.get("answer_key").asInt
                                        val number = qitem.asJsonObject.get("number").asInt
                                        val images = qitem.asJsonObject.get("images").toString()
                                        val qUniqueId = UUID.randomUUID().toString()

                                        val question = EntranceQuestionModelHandler.add(context, qUniqueId, number, answer, images, false, entrance)

                                        if (question != null) {
//                                            RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                                            RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()

                                            val imagesArray = JsonParser().parse(images).asJsonArray
                                            if (imagesArray != null) {
                                                for (item3 in imagesArray) {
                                                    val imageUniqueId = item3.asJsonObject.get("unique_key").asString
                                                    localImage.put(imageUniqueId, qUniqueId)
                                                    if (localList.get(qUniqueId) == null) {
                                                        localList.put(qUniqueId, ArrayList<Pair<String, Boolean>>())
                                                    }

                                                    localList.get(qUniqueId)?.add(Pair(imageUniqueId, false))
                                                }
                                            } else {
                                                return EntrancePackageResult(false, LinkedHashMap(), LinkedHashMap())
                                            }
                                            RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                                                lesson.questions.add(question)
                                            }

                                        } else {
                                            return EntrancePackageResult(false, LinkedHashMap(), LinkedHashMap())
                                        }
                                    }

                                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                                        booklet.lessons.add(lesson)
                                    }

                                } else {
                                    return EntrancePackageResult(false, LinkedHashMap(), LinkedHashMap())
                                }
                            }

                            RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                                entrance.booklets.add(booklet)
                            }
                        } else {
                            return EntrancePackageResult(false, LinkedHashMap(), LinkedHashMap())
                        }
                    }
                } else {
                    return EntrancePackageResult(false, LinkedHashMap(), LinkedHashMap())
                }
//                Log.d(TAG, entrance.toString())
//                Log.d(TAG, entrance.booklets[0].toString())
//
//                Log.d(TAG, entrance.booklets[0].lessons[0].toString())
//                Log.d(TAG, entrance.booklets[0].lessons[0].questions.toString())
            } catch (exc: Exception) {
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
//                Log.d(TAG, exc.message)
                return EntrancePackageResult(false, LinkedHashMap(), LinkedHashMap())
            }


            return EntrancePackageResult(true, localImage, localList)
        }

        @JvmStatic
        fun removePackage(context: Context, username: String, entranceUniqueId: String): Boolean {
            try {
                val entrance = EntranceModelHandler.getByUsernameAndId(context, username, entranceUniqueId)
                if (entrance != null) {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        val booklets = entrance.booklets
                        for (booklet in booklets) {

                            val lessons = booklet.lessons
                            for (lesson in lessons) {
                                val questions = lesson.questions

                                questions.deleteAllFromRealm()
//                            RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                            RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                            }

//                            RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                                lessons.deleteAllFromRealm()
//                            }
//                        RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                        RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                        }

//                        RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                            booklets.deleteAllFromRealm()
//                        }
                    }
                }
            } catch (exc: Exception) {
//                RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                Log.d(TAG, exc.message)
                return false
            }

            return true
        }
    }
}