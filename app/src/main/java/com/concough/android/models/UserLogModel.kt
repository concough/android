package com.concough.android.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by abolfazl on 7/12/17.
 */
class UserLogModel: RealmObject() {
    @PrimaryKey
    var uniqueId: String = ""

    var username: String = ""
    var created: Date = Date()
    var logType: String = ""
    var extraData: String = ""
    var isSynced: Boolean = false
}