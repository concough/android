package com.concough.android.models

import io.realm.RealmObject
import java.util.*

/**
 * Created by abolfazl on 7/12/17.
 */
class EntranceStarredQuestionModel: RealmObject() {

    var question: EntranceQuestionModel? = null
    var created: Date = Date()
    var entranceUniqueId: String? = null
}