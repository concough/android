package com.concough.android.structures

/**
 * Created by abolfazl on 8/6/17.
 */
enum class LogTypeEnum(val title: String) {
    EntranceDownload("ENTRANCE_DOWNLOAD"),
    EntranceShowNormal("ENTRANCE_SHOW_NORMAL"),
    EntranceShowStarred("ENTRANCE_SHOW_STARRED"),
    EntranceQuestionStar("ENTRANCE_QUESTION_STAR"),
    EntranceQuestionUnStar("ENTRANCE_QUESTION_UNSTAR"),
    EntranceLastVisitInfo("ENTRANCE_LAST_VISIT_INFO"),
    EntranceCommentCreate("ENTRANCE_COMMENT_CREATE"),
    EntranceCommentDelete("ENTRANCE_COMMENT_DELETE"),
    EntranceLessonExamCancel("ENTRANCE_LESSON_EXAM_CANCEL"),
    EntranceLessonExamFinished("ENTRANCE_LESSON_EXAM_FINISHED")
}