package com.concough.android.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by abolfazl on 7/16/17.
 */

public class DeviceInformationModel extends RealmObject {

    @PrimaryKey
    public String username = "";
    public String device_name = "";
    public String device_model = "";
    public Boolean state = true;
    public Boolean is_me = false;

}
