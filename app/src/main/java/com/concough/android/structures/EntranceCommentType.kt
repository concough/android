package com.concough.android.structures

/**
 * Created by abolfazl on 11/16/18.
 */
enum class EntranceCommentType(val code: String) {
    TEXT("TEXT");

    companion object Factory {
        const val TAG = "EntranceCommentType"

        @JvmStatic
        fun toType(item: String): EntranceCommentType {
            return when (item) {
                "TEXT" -> TEXT
                else -> TEXT
            }
        }
    }
}