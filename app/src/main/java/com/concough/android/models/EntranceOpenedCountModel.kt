package com.concough.android.models

import io.realm.RealmObject

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceOpenedCountModel: RealmObject() {
    var entranceUniqueId: String = ""
    var count: Int = 1
    var type: String = ""
}