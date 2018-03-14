package com.concough.android.structures

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.Serializable
import java.util.*

/**
 * Created by abolfazl on 7/11/17.
 */
class EntranceStruct() : Serializable {
    var entranceTypeTitle: String? = null
    var entranceOrgTitle: String? = null
    var entranceGroupTitle: String? = null
    var entranceSetTitle: String? = null
    var entranceSetId: Int? = null
    var entranceExtraData: JsonElement? = null
    var entranceBookletCounts: Int? = null
    var entranceYear: Int? = null
    var entranceDuration: Int? = null
    var entranceUniqueId: String? = null
    var entranceLastPublished: Date? = null
}
