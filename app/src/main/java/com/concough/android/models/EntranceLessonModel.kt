package com.concough.android.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceLessonModel: RealmObject() {
    @PrimaryKey
    var uniqueId: String = ""

    var title: String = ""
    var fullTitle: String = ""
    var qStart: Int = 0
    var qEnd: Int = 0
    var qCount: Int = 0
    var order: Int = 0
    var duration: Int = 0

    @LinkingObjects("lessons")
    lateinit var booklet: RealmResults<EntranceBookletModel>

    var questions: RealmList<EntranceQuestionModel> = RealmList()

}