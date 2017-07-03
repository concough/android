package com.concough.android.extensions

import com.concough.android.settings.PHONE_NUMBER_VALIDATOR_REGEX

val String.isValidPhoneNumber: Boolean
    get() {
        val phoneRegex = PHONE_NUMBER_VALIDATOR_REGEX.toRegex()
        val result = phoneRegex.matchEntire(this)
        result?.let {
                return true
        }
        return false
    }
