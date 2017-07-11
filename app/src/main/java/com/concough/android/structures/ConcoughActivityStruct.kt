package com.concough.android.structures

import com.google.gson.JsonObject
import java.util.*

/**
 * Created by abolfazl on 7/10/17.
 */
data class ConcoughActivityStruct (var created: Date, var createdStr: String, var activityType: String, var target: JsonObject)