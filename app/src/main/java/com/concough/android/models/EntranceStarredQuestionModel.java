package com.concough.android.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by abolfazl on 7/16/17.
 */

public class EntranceStarredQuestionModel extends RealmObject {
    public EntranceQuestionModel question = null;
    public Date created = new Date();
    public String entranceUniqueId = null;
}
