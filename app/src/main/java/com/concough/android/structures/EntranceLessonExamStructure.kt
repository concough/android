package com.concough.android.structures

import java.util.*
import kotlin.collections.HashMap

/**
 * Created by abolfazl on 11/22/18.
 */
data class EntranceLessonExamStructure(var title: String?,
                                       var order: Int?,
                                       var bookletOrder: Int?,
                                       var started: Date?,
                                       var finished: Date?,
                                       var qCount: Int?,
                                       var answers: HashMap<Int, Int> = hashMapOf(),
                                       var trueAnswer: Int = 0,
                                       var falseAnswer: Int = 0,
                                       var noAnswer: Int = 0,
                                       var withTime: Boolean = false,
                                       var duration: Int?,
                                       var percentage: Double = 0.0)