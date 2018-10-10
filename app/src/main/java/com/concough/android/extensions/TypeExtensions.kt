package com.concough.android.extensions

import android.content.res.Configuration
import android.content.res.Resources
import android.text.format.DateUtils
import com.concough.android.utils.timesAgoTranslate
import java.util.*


fun Date.timeAgoSinceDate(lang: String, numericDates: Boolean = false): String {
    val nowD = Date()

    var finalDate = this
    if (this > nowD) {
        finalDate = nowD
    }

    if (lang == "fa") {
        val configuration = Configuration(Resources.getSystem().configuration)
        configuration.locale = Locale("fa", "IR") // or whichever locale you desire
        Resources.getSystem().updateConfiguration(configuration, null)
    }

    val diff = nowD.time - finalDate.time
    val ago = DateUtils.getRelativeTimeSpanString(finalDate.time, nowD.time, 0)
    return ago.toString()


//    var years = 0
//    var months = 0
//    var days = 0
//
//    //create calendar object for birth day
//    val birthDay = Calendar.getInstance()
//    birthDay.timeInMillis = finalDate.getTime()
//
//    //create calendar object for current day
//    val now = Calendar.getInstance()
//    now.time = nowD
//
//    //Get difference between years
//    years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR)
//    val currMonth = now.get(Calendar.MONTH) + 1
//    val birthMonth = birthDay.get(Calendar.MONTH) + 1
//
//    //Get difference between months
//    months = currMonth - birthMonth
//
//    //if month difference is in negative then reduce years by one
//    //and calculate the number of months.
//    if (months < 0) {
//        years--
//        months = 12 - birthMonth + currMonth
//        if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
//            months--
//    } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
//        years--
//        months = 11
//    }
//
//    //Calculate the days
//    if (now.get(Calendar.DATE) > birthDay.get(Calendar.DATE))
//        days = now.get(Calendar.DATE) - birthDay.get(Calendar.DATE)
//    else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
//        val today = now.get(Calendar.DAY_OF_MONTH)
//        now.add(Calendar.MONTH, -1)
//        days = now.getActualMaximum(Calendar.DAY_OF_MONTH) - birthDay.get(Calendar.DAY_OF_MONTH) + today
//    } else {
//        days = 0
//        if (months == 12) {
//            years++
//            months = 0
//        }
//    }
}