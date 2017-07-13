package com.concough.android.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceBookletModel: RealmObject() {
    @PrimaryKey
    var uniqueId: String = ""
    var title: String = ""
    var lessonCount: Int = 0
    var duration: Int = 0
    var isOptional: Boolean = false
    var order: Int = 0

    @LinkingObjects("booklets")
    lateinit var entrance: RealmResults<EntranceModel>

    var lessons: RealmList<EntranceLessonModel> = RealmList()
}