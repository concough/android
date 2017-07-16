package com.concough.android.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 7/16/17.
 */

public class EntranceBookletModel extends RealmObject {
    @PrimaryKey
    public String uniqueId = "";
    public String title = "";
    public int lessonCount = 0;
    public int duration = 0;
    public Boolean isOptional = false;
    public int order = 0;

    @LinkingObjects("booklets")
    public RealmResults<EntranceModel> entrance = null;

    public RealmList<EntranceLessonModel> lessons;
}
