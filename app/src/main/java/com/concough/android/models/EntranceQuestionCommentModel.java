package com.concough.android.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 11/16/18.
 */

public class EntranceQuestionCommentModel extends RealmObject {
    @PrimaryKey
    public String uniqueId = "";

    public EntranceQuestionModel question = null;
    public Date created = new Date();
    public String entranceUniqueId = null;
    public String username = "";
    public String commentType = "";
    public String commentData = "";
}
