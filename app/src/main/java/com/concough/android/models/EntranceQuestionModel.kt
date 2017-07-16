package com.concough.android.models

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceQuestionModel: RealmObject() {
    @PrimaryKey
    var uniqueId: String = ""
    var number: Int = 0
    var answer: Int = 0
    var images: String = ""
    var isDownloaded: Boolean = false
    var entrance: EntranceModel? = null

    @LinkingObjects("questions")
    val lesson: RealmResults<EntranceLessonModel>? = null

}