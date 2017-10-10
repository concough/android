package com.concough.android.utils

/**
 * Created by abolfazl on 10/10/17.
 */

val answers: HashMap<Int, String> = hashMapOf(
        0 to "هیچکدام",
        1 to "۱",
        2 to "۲",
        3 to "۳",
        4 to "۴",
        5 to "۱ و ۲",
        6 to "۱ و ۳",
        7 to "۱ و ۴",
        8 to "۲ و ۳",
        9 to "۲ و ۴",
        10 to "۳ و ۴"
)

fun questionAnswerToString(key: Int) : String {
    return answers.get(key)!!
}