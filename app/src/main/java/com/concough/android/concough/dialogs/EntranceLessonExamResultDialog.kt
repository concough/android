package com.concough.android.concough.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.view.Window
import com.concough.android.concough.R
import com.concough.android.concough.interfaces.EntranceLessonExamDelegate
import com.concough.android.extensions.diffInHourMinSec
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.structures.EntranceLessonExamStructure
import kotlinx.android.synthetic.main.dialog_entrance_lesson_exam_result.*

/**
 * Created by abolfazl on 11/27/18.
 */
class EntranceLessonExamResultDialog(context: Context): Dialog(context) {
    companion object {
        const val TAG = "EntranceLessonExamResultDialog"
    }

    public var listener: EntranceLessonExamDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_entrance_lesson_exam_result)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        DELEResult_titleTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        DELEResult_subTitleTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        DELEResult_examTimeTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DELEResult_questionCountTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DELEResult_resultExamTimeLabelTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        DELEResult_resultExamTimeTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DELEResult_resultExamPercentageLabelTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        DELEResult_resultExamPercentageTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DELEResult_trueAnswerTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DELEResult_falseAnswerTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DELEResult_noAnswerTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DELEResult_closeButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        DELEResult_seeResultsButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold

        DELEResult_examTimeImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorConcoughBlue), PorterDuff.Mode.SRC_IN)
        DELEResult_questionCountImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorConcoughBlue), PorterDuff.Mode.SRC_IN)

        DELEResult_seeResultsButton.setOnClickListener {
            this.listener?.let {
                this.listener!!.showLessonExamResult()
            }
            this.dismiss()
        }

        DELEResult_closeButton.setOnClickListener {
            this.listener?.let {
                this.listener!!.cancelLessonExam(false)
            }
            this.dismiss()
        }
    }

    public fun setupDialog(entranceLessonExamStruct: EntranceLessonExamStructure) {
        DELEResult_subTitleTextView.text = entranceLessonExamStruct.title
        DELEResult_examTimeTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(entranceLessonExamStruct.duration) + "'"
        DELEResult_questionCountTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(entranceLessonExamStruct.qCount)

        val d = entranceLessonExamStruct.started!!.diffInHourMinSec(entranceLessonExamStruct.finished!!) // hour, minute, second

        var s = ""
        if (d[0] > 0) {
            s += FormatterSingleton.getInstance().NumberFormatter.format(d[0]) + " : "
        }

        var m = FormatterSingleton.getInstance().NumberFormatter.format(d[1])
        if (m.length == 1) {
            m = "۰$m"
        }
        s += m + " : "

        var se = FormatterSingleton.getInstance().NumberFormatter.format(d[2])
        if (se.length == 1) {
            se = "۰$se"
        }
        s += se

        DELEResult_resultExamTimeTextView.text = s

//        val avg = (Math.round((Math.round(entranceLessonExamStruct.percentage * 10000) / 100).toFloat()) * 10).toDouble() / 10
        DELEResult_resultExamPercentageTextView.text = "${FormatterSingleton.getInstance().DecimalNumberFormatter.format(entranceLessonExamStruct.percentage * 100)} %"

        DELEResult_trueAnswerTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(entranceLessonExamStruct.trueAnswer)
        DELEResult_falseAnswerTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(entranceLessonExamStruct.falseAnswer)
        DELEResult_noAnswerTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(entranceLessonExamStruct.noAnswer)
    }
}