package com.concough.android.extensions

import com.concough.android.utils.timesAgoTranslate
import org.joda.time.Period
import java.util.*

fun Date.timeAgoSinceDate(lang: String, numericDates: Boolean = false): String {
    val nowD = Date()

    var finalDate = this
    if (this > nowD) {
        finalDate = nowD
    }

    val p = Period(finalDate.time, nowD.time)

    when {
        p.years >= 2 -> return timesAgoTranslate(lang, "d_year_ago", p.years)
        p.years >= 1 -> return if (numericDates) {
            timesAgoTranslate(lang, "1_year_ago", 0)
        } else {
            timesAgoTranslate(lang, "last_year", 0)
        }
        p.months >= 2 -> return timesAgoTranslate(lang, "d_month_ago", p.months)
        p.months >= 1 -> return if (numericDates) {
            timesAgoTranslate(lang, "1_month_ago", 0)
        } else {
            timesAgoTranslate(lang, "last_month", 0)
        }
        p.weeks >= 2 -> return timesAgoTranslate(lang, "d_week_ago", p.weeks)
        p.weeks >= 1 -> return if (numericDates) {
            timesAgoTranslate(lang, "1_week_ago", 0)
        } else {
            timesAgoTranslate(lang, "last_week", 0)
        }
        p.days >= 2 -> return timesAgoTranslate(lang, "d_day_ago", p.days)
        p.days >= 1 -> return if (numericDates) {
            timesAgoTranslate(lang, "1_day_ago", 0)
        } else {
            timesAgoTranslate(lang, "last_day", 0)
        }
        p.hours >= 2 -> return timesAgoTranslate(lang, "d_hour_ago", p.hours)
        p.hours >= 1 -> return if (numericDates) {
            timesAgoTranslate(lang, "1_hour_ago", 0)
        } else {
            timesAgoTranslate(lang, "last_hour", 0)
        }
        p.minutes >= 2 -> return timesAgoTranslate(lang, "d_minute_ago", p.minutes)
        p.minutes >= 1 -> return if (numericDates) {
            timesAgoTranslate(lang, "1_minute_ago", 0)
        } else {
            timesAgoTranslate(lang, "last_minute", 0)
        }
        p.seconds >= 10 -> return timesAgoTranslate(lang, "d_second_ago", p.seconds)
        else -> {
            return timesAgoTranslate(lang, "just_now", 0)
        }
    }
}