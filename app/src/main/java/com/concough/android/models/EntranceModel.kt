package com.concough.android.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceModel: RealmObject() {
    @PrimaryKey
    var uniqueId: String = ""

    var username: String = ""
    var type: String = ""
    var organization: String = ""
    var group: String = ""
    var set: String = ""
    var setId: Int = 0
    var extraData: String = ""
    var bookletsCount: Int = 0
    var year: Int = 0
    var duration: Int = 0
    var lastPublished: Date = Date()

    var booklets: RealmList<EntranceBookletModel> = RealmList()
}