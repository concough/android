package com.concough.android.utils

import com.concough.android.singletons.FormatterSingleton

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

val months: HashMap<Int, String> = hashMapOf(
        1 to "فروردین",
        2 to "اردیبهشت",
        3 to "خرداد",
        4 to "تیر",
        5 to "مرداد",
        6 to "شهریور",
        7 to "مهر",
        8 to "آبان",
        9 to "آذر",
        10 to "دی",
        11 to "بهمن",
        12 to "اسفند"
)

fun questionAnswerToString(key: Int) : String {
    return answers.get(key)!!
}

fun monthToString(key: Int): String {
    return months.get(key)!!
}

fun timesAgoTranslate(lang: String, key: String, vararg params: Int): String {
    if (lang == "fa") {
        var result = "چند لحظه پیش"

        when(key) {
            "1_year_ago" -> result = "یک سال پیش"
            "last_year" -> result = "پارسال"
            "d_year_ago" -> result = FormatterSingleton.getInstance().NumberFormatter.format(params[0]) + " سال پیش"
            "1_month_ago" -> result = "یک ماه پیش"
            "last_month" -> result = "ماه پیش"
            "d_month_ago" -> result = FormatterSingleton.getInstance().NumberFormatter.format(params[0]) + " ماه پیش"
            "1_week_ago" -> result = "یک هفته پیش"
            "last_week" -> result = "هفته پیش"
            "d_weak_ago" -> result = FormatterSingleton.getInstance().NumberFormatter.format(params[0]) + " هفته پیش"
            "1_day_ago" -> result = "یک روز پیش"
            "last_day" -> result = "دیروز"
            "d_day_ago" -> result = FormatterSingleton.getInstance().NumberFormatter.format(params[0]) + " روز پیش"
            "1_hour_ago" -> result = "یک ساعت پیش"
            "last_hour" -> result = "یک ساعت پیش"
            "d_hour_ago" -> result = FormatterSingleton.getInstance().NumberFormatter.format(params[0]) + " ساعت پیش"
            "1_minute_ago" -> result = "یک دقیقه پیش"
            "last_minute" -> result = "یک دقیقه پیش"
            "d_minute_ago" -> result = FormatterSingleton.getInstance().NumberFormatter.format(params[0]) + " دقیقه پیش"
            "d_second_ago" -> result = FormatterSingleton.getInstance().NumberFormatter.format(params[0]) + " ثانیه پیش"
        }

        return result
    }

    return ""
}