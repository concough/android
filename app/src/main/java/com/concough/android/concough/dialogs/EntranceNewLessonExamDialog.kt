package com.concough.android.concough.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.concough.android.concough.R
import com.concough.android.concough.interfaces.EntranceLessonExamDelegate
import com.concough.android.singletons.FontCacheSingleton
import kotlinx.android.synthetic.main.dialog_entrance_new_lesson_exam.*

/**
 * Created by abolfazl on 11/25/18.
 */
class EntranceNewLessonExamDialog(context: Context): Dialog(context) {
    companion object {
        const val TAG = "EntranceNewLessonExamDialog"
    }

    public var listener: EntranceLessonExamDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_entrance_new_lesson_exam)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)

        DENLExam_titleTextView.typeface = FontCacheSingleton.getInstance(context.applicationContext).Light
        DENLExam_startExamButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Bold
        DENLExam_cancelButton.typeface = FontCacheSingleton.getInstance(context.applicationContext).Regular
    }

    public fun setupDialog() {
        DENLExam_cancelButton.setOnClickListener{
            this.dismiss()
        }

        DENLExam_startExamButton.setOnClickListener {
            this.listener?.let {
                this.listener!!.startLessonExam()
            }
            this.dismiss()
        }
    }
}