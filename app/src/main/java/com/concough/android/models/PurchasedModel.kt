package com.concough.android.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by abolfazl on 7/12/17.
 */

class PurchasedModel: RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var username: String = ""
    var downloadTimes: Int = 0
    var isDownloaded: Boolean = false
    var isImageDownloaded: Boolean = false
    var isLocalDBCreated: Boolean = false
    var productType: String = "Entrance"
    var productUniqueId: String = ""
    var created: Date = Date()
}