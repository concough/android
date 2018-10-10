package com.concough.android.singletons

import com.concough.android.utils.PersianCalendar
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by abolfazl on 7/5/17.
 */
class FormatterSingleton {
    private var _UTCDateFormatter: SimpleDateFormat
    private var _UTCShortDateFormatter: SimpleDateFormat
    private var _NumberFormatter: NumberFormat

    companion object Factory {
        private var sharedInstance: FormatterSingleton? = null
        private var _months = arrayOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")

        @JvmStatic
        fun getInstance(): FormatterSingleton {
            if (sharedInstance == null)
                sharedInstance = FormatterSingleton()

            return sharedInstance!!
        }
    }

    private constructor() {
        //this._UTCDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        this._UTCDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        this._UTCDateFormatter.timeZone = TimeZone.getTimeZone("UTS")

        this._UTCShortDateFormatter = SimpleDateFormat("yyyy-MM-dd")
        this._UTCShortDateFormatter.timeZone = TimeZone.getTimeZone("UTS")

        this._NumberFormatter = NumberFormat.getNumberInstance(Locale("fa", "IR"))
        this._NumberFormatter.isGroupingUsed = false;

    }

    public val UTCDateFormatter: SimpleDateFormat
        get() = this._UTCDateFormatter

    public val UTCShortDateFormatter: DateFormat
        get() = this._UTCShortDateFormatter

    public val NumberFormatter: NumberFormat
        get() = this._NumberFormatter

    public fun getPersianDateString(d: Date): String {
        val year = PersianCalendar.getPersianYear(d)
        val month = PersianCalendar.getPersianMonth(d)
        val day = PersianCalendar.getPersianDayOfMonth(d)

        var dayStr = FormatterSingleton.getInstance().NumberFormatter.format(day)
        var yearStr = FormatterSingleton.getInstance().NumberFormatter.format(year)

        val d = _months[month - 1]
        return "%s %s %s".format(dayStr, d, yearStr)
    }
}