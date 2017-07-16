package com.concough.android.models;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 7/16/17.
 */

public class EntranceQuestionModel extends RealmObject {
    @PrimaryKey
    public String uniqueId = "";
    public int number = 0;
    public int answer = 0;
    public String images = "";
    public Boolean isDownloaded = false;
    public EntranceModel entrance = null;

    @LinkingObjects("questions")
    public final RealmResults<EntranceLessonModel> lesson = null;
}
