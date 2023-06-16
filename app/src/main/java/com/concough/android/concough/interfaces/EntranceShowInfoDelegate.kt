package com.concough.android.concough.interfaces

import com.concough.android.structures.EntranceQuestionAnswerState

/**
 * Created by abolfazl on 11/22/18.
 */
interface EntranceShowInfoDelegate {
    fun showStarredQuestionButtonClicked()
    fun defaultShowSegmantChanged(state: EntranceQuestionAnswerState)
}