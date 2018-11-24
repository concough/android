package com.concough.android.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by abolfazl on 11/22/18.
 */

public class EntranceQuestionExamStatModel extends RealmObject {
    public String username = "";
    public String entranceUniqueId = "";
    public int questionNo = 0;
    public int totalCount = 0;
    public int trueCount = 0;
    public int falseCount = 0;
    public int emptyCount = 0;
    public Date created = new Date();
    public Date updated = new Date();
    public String statData = "";
}
