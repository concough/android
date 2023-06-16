package com.concough.android.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 11/22/18.
 */

public class EntranceLessonExamModel extends RealmObject {
    @PrimaryKey
    public String uniqueId = "";
    public String username = "";
    public String entranceUniqueId = "";
    public String lessonTitle = "";
    public int lessonOrder = 0;
    public int bookletOrder = 0;
    public Date startedDate = new Date();
    public Date finishedDate = new Date();
    public boolean withTime = false;
    public int questionCount = 0;
    public int trueAnswer = 0;
    public int falseAnswer = 0;
    public int noAnswer = 0;
    public Date created = new Date();
    public int examDuration = 0;
    public String examData = "";
    public double percentage = 0.0;
}
