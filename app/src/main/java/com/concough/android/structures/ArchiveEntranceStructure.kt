package com.concough.android.structures

import com.google.gson.JsonElement
import java.util.*

/**
 * Created by abolfazl on 10/23/18.
 */
public data class ArchiveEntranceStructure(var organization: String, var year: Int, var month: Int,
                                           var extraData: JsonElement?, var buyCount: Int,
                                           var lastPublished: Date, var uniqueId: String,
                                           var bookletCount: Int, var entranceDuration: Int,
                                           var costBon: Int)