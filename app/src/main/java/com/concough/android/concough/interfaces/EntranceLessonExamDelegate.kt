package com.concough.android.concough.interfaces

/**
 * Created by abolfazl on 11/25/18.
 */
interface EntranceLessonExamDelegate {
    fun startLessonExam()
    fun cancelLessonExam(withLog: Boolean)
    fun showLessonExamResult()
}