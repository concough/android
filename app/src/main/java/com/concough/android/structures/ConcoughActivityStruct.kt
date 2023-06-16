package com.concough.android.structures

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import java.io.Serializable
import java.util.*

/**
 * Created by abolfazl on 7/10/17.
 */
data class ConcoughActivityStruct (var created: Date,
                                   var createdStr: String,
                                   var activityType: String,
                                   var target: JsonObject): Serializable