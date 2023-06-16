package com.concough.android.models;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 7/16/17.
 */

public class EntranceModel extends RealmObject {
    @PrimaryKey
    public String pUniqueId = "";

    public String uniqueId = "";
    public String username = "";
    public String type = "";
    public String organization = "";
    public String group = "";
    public String set = "";
    public int setId = 0;
    public String extraData = "";
    public int bookletsCount = 0;
    public int year = 0;
    public int month = 0;
    public int duration = 0;
    public Date lastPublished = new Date();

    public RealmList<EntranceBookletModel> booklets = new RealmList<>();

}
