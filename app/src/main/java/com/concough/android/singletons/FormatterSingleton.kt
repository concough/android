package com.concough.android.singletons

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by abolfazl on 7/5/17.
 */
class FormatterSingleton {
    private var _UTCDateFormatter: DateFormat

    companion object Factory {
        private var sharedInstance : FormatterSingleton? = null

        @JvmStatic
        fun  getInstance(): FormatterSingleton {
            if (sharedInstance == null)
                sharedInstance = FormatterSingleton()

            return sharedInstance!!
        }
    }

    private constructor() {
        this._UTCDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        this._UTCDateFormatter.timeZone = TimeZone.getTimeZone("UTS")
    }

    public val UTCDateFormatter: DateFormat
        get() = this._UTCDateFormatter
}