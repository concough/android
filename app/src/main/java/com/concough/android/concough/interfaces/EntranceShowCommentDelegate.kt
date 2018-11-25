package com.concough.android.concough.interfaces

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Created by abolfazl on 11/17/18.
 */
interface EntranceShowCommentDelegate {
    fun addTextComment(questionId: String, questionNo: Int, position: Int, commentData: JsonObject): Boolean
    fun cancelComment()
    fun deleteComment(questionId: String, questionNo: Int, commentId: String, position: Int): Boolean
}