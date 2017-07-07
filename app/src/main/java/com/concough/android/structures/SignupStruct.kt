package com.concough.android.structures

import java.io.Serializable
import java.util.*

/**
 * Created by abolfazl on 7/7/17.
 */
class SignupStruct(): Serializable {
    var username: String? = null
    var password: String? = null
    var preSignupId: Int? = null
}
data class SignupMoreInfoStruct(var firstname: String?, var lastname: String?, var grade: String?, var gender: String?, var birthday: Date?)

enum class Gender(value: String) {
    Male("M"), Female("F"), Other("O")
}

enum class GradeType(value: String) {
    BE("BE"), ME("ME");

    companion object Factory{
        val allValues = arrayOf(BE, ME)

        fun selectWithString(value: String): GradeType {
            when(value) {
                "ME" -> return ME
                else -> return BE
            }
        }
    }

    override fun toString(): String {
        when(this) {
            BE -> return "سراسری"
            ME -> return "کارشناسی ارشد"
        }
    }
}