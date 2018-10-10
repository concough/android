package com.concough.android.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by abolfazl on 10/11/18.
 */

public class EntranceLastVisitInfoModel extends RealmObject {
    public String username = "";
    public String entranceUniqueId = "";
    public Date updated = new Date();
    public int bookletIndex = 0;
    public int lessonIndex = 0;
    public String index = "";
    public String showType = "";
}
