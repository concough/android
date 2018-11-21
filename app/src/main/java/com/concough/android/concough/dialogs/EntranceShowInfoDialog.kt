package com.concough.android.concough.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bumptech.glide.Glide
import com.concough.android.concough.R
import com.concough.android.concough.interfaces.EntranceShowInfoDelegate
import com.concough.android.rest.MediaRestAPIClass
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.MediaCacheSingleton
import com.concough.android.structures.EntranceQuestionAnswerState
import com.concough.android.structures.EntranceStruct
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.utils.convertFileToByteArray
import com.concough.android.utils.monthToString
import kotlinx.android.synthetic.main.dialog_entrance_show_info.*
import java.io.File

/**
 * Created by abolfazl on 11/21/18.
 */
class EntranceShowInfoDialog(context: Context): Dialog(context) {
    companion object {
        const val TAG: String = "EntranceShowInfoDialog"
    }

    public var listener: EntranceShowInfoDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_entrance_show_info)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

        ESID_defaultShowLabelTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        ESID_entranceOrgTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        ESID_entranceSetTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        ESID_entranceTypeTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        ESID_entranceYearTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        ESID_examBriefTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        ESID_lessonExamTimerTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        ESID_markedCountTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        ESID_segmentAnswer.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        ESID_segmentComments.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        ESID_segmentNone.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        ESID_segmentStats.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        ESID_totalQuestionsTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
        ESID_showStarredQuestionButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular

        ESID_showStarredQuestionButton.setOnClickListener {
            this.dismiss()
            this.listener?.let {
                this.listener!!.showStarredQuestionButtonClicked()
            }
        }

        ESID_defaultShowSegmentGroup.setOnCheckedChangeListener { radioGroup, i ->
            this.dismiss()
            when (i) {
                ESID_segmentNone.id -> {
                    this.listener?.let {
                        this.listener!!.defaultShowSegmantChanged(EntranceQuestionAnswerState.None)
                    }
                }
                ESID_segmentAnswer.id -> {
                    this.listener?.let {
                        this.listener!!.defaultShowSegmantChanged(EntranceQuestionAnswerState.ANSWER)
                    }
                }
                ESID_segmentComments.id -> {
                    this.listener?.let {
                        this.listener!!.defaultShowSegmantChanged(EntranceQuestionAnswerState.COMMENTS)
                    }
                }
                ESID_segmentStats.id -> {
                    this.listener?.let {
                        this.listener!!.defaultShowSegmantChanged(EntranceQuestionAnswerState.STATS)
                    }
                }
            }
        }
    }

    fun setupDialog(entrance: EntranceStruct, starredCount: Int, segmentState: EntranceQuestionAnswerState,
                    showType: String, totalQuestion: Int, answeredQuestions: Int, lessonTitle: String,
                    lessonExamTime: Int) {

        if (entrance.entranceMonth!! > 0) {
            ESID_entranceYearTextView.text = "${monthToString(entrance.entranceMonth!!)} ${FormatterSingleton.getInstance().NumberFormatter.format(entrance.entranceYear)}"
        } else {
            ESID_entranceYearTextView.text = "${FormatterSingleton.getInstance().NumberFormatter.format(entrance.entranceYear)}"
        }

        if (showType == "Show" || showType == "Starred") {
            ESID_entranceTypeTextView.text = "آزمون ${entrance.entranceTypeTitle}"
            ESID_entranceSetTextView.text = "${entrance.entranceSetTitle} (${entrance.entranceGroupTitle})"
            ESID_entranceOrgTextView.text = entrance.entranceOrgTitle
            ESID_defaultShowSegmentGroup.check(3 % segmentState.ordinal)

        } else if (showType == "LessonExam" || showType == "LessonExamResult") {
            ESID_totalQuestionsTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(totalQuestion)
            ESID_lessonExamTimerTextView.text = "${FormatterSingleton.getInstance().NumberFormatter.format(lessonExamTime)} '"
            ESID_entranceTypeTextView.text = lessonTitle
            ESID_entranceSetTextView.text = "${entrance.entranceSetTitle} (${entrance.entranceGroupTitle})"
            ESID_entranceOrgTextView.visibility = View.GONE
        } else if (showType == "LessonExamHistory") {
            ESID_totalQuestionsTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(totalQuestion)
            ESID_lessonExamTimerTextView.text = "${FormatterSingleton.getInstance().NumberFormatter.format(lessonExamTime)} '"
            ESID_entranceTypeTextView.text = lessonTitle
            ESID_entranceSetTextView.text = "${entrance.entranceSetTitle} (${entrance.entranceGroupTitle})"
            ESID_entranceOrgTextView.visibility = View.GONE
        }

        this.downloadImage(entrance.entranceSetId!!)

        if (showType == "Show" || showType == "Starred") {
            ESID_defaultShowContainer.visibility = View.VISIBLE
            ESID_examBriefContainer.visibility = View.GONE
            ESID_showStarredQuestionButton.visibility = View.VISIBLE
            ESID_markedQuestionContainer.visibility = View.VISIBLE

            if (showType == "Show") {
                ESID_markedCountTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(starredCount)
                ESID_showStarredQuestionButton.text = context.resources.getString(R.string.entrance_info_default_show_starred_q)
            } else if (showType == "Starred") {
                ESID_showStarredQuestionButton.text = context.resources.getString(R.string.entrance_info_default_show_all_q)
            }
        } else if (showType == "LessonExam" || showType == "LessonExamResult") {
            ESID_defaultShowContainer.visibility = View.GONE
            ESID_examBriefContainer.visibility = View.VISIBLE
            ESID_showStarredQuestionButton.visibility = View.GONE
            ESID_markedQuestionContainer.visibility = View.GONE
        } else if (showType == "LessonExamHistory") {
            ESID_defaultShowContainer.visibility = View.GONE
            ESID_examBriefContainer.visibility = View.VISIBLE
            ESID_showStarredQuestionButton.visibility = View.GONE
            ESID_markedQuestionContainer.visibility = View.GONE
            ESID_examBriefTextView.visibility = View.GONE
        }
    }

    private fun downloadImage(imageId: Int) {
        val data: ByteArray?
        val url = MediaRestAPIClass.makeEsetImageUrl(imageId)

        val photo = File( "${context.filesDir}/images/eset", imageId.toString())
        data = if (photo.exists()) {
            convertFileToByteArray(photo)
            //Log.d(TAG, "downloadImage: From File")
        } else {
            MediaCacheSingleton.getInstance(context.applicationContext)[url!!]
        }

        if (data != null) {
            Glide.with(context)

                    .load(data)
                    .dontAnimate()
                    .into(ESID_entranceSetImageView)
                    .onLoadFailed(null,
                            ContextCompat.getDrawable(context, R.drawable.no_image))
        } else {
            MediaRestAPIClass.downloadEsetImage(context, imageId, { data, httpErrorType ->
                if (httpErrorType !== HTTPErrorType.Success) {
                    Log.d(TAG, "run: ")
                    if (httpErrorType === HTTPErrorType.Refresh) {
                        downloadImage(imageId)
                    } else {
                        ESID_entranceSetImageView.setImageResource(R.drawable.no_image)
                    }
                } else {
                    if (url != null) {
                        MediaCacheSingleton.getInstance(context.applicationContext)[url] = data!!
                    }

                    Glide.with(context)

                            .load(data)
                            .dontAnimate()
                            .into(ESID_entranceSetImageView)
                            .onLoadFailed(null, ContextCompat.getDrawable(context, R.drawable.no_image))
                }
            }) { }
        }
    }
}