package com.concough.android.models;

import io.realm.RealmObject;

/**
 * Created by abolfazl on 7/16/17.
 */

public class EntranceOpenedCountModel extends RealmObject {
    public String entranceUniqueId = "";
    public int count = 1;
    public String type = "";
}
