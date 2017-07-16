package com.concough.android.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 7/16/17.
 */

public class UserLogModel extends RealmObject {
    @PrimaryKey
    public String uniqueId = "";

    public String username = "";
    public Date created = new Date();
    public String logType = "";
    public String extraData = "";
    public Boolean isSynced = false;
}
