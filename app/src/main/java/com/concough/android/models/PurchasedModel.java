package com.concough.android.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 7/16/17.
 */

public class PurchasedModel extends RealmObject {
    @PrimaryKey
    public int id = 0;
    public String username = "";
    public int downloadTimes = 0;
    public Boolean isDownloaded = false;
    public Boolean isImageDownloaded = false;
    public Boolean isLocalDBCreated = false;
    public String productType = "Entrance";
    public String productUniqueId = "";
    public Date created = new Date();
}
