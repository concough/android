package com.concough.android.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 7/16/17.
 */

public class EntranceLessonModel extends RealmObject {
    @PrimaryKey
    public String uniqueId = "";

    public String title = "";
    public String fullTitle = "";
    public int qStart = 0;
    public int qEnd = 0;
    public int qCount = 0;
    public int order = 0;
    public int duration = 0;

    @LinkingObjects("lessons")
    public final RealmResults<EntranceBookletModel> booklet = null;

    public RealmList<EntranceQuestionModel> questions = new RealmList<EntranceQuestionModel>();
}
