package com.concough.android.models

import android.annotation.SuppressLint
import android.content.Context
import com.concough.android.singletons.RealmSingleton

/**
 * Created by Owner on 10/8/2017.
 */
class DeviceInformationModelHandler {
    companion object Factory {
        val TAG: String = "DeviceInformationModelHandler"

        @SuppressLint("LongLogTag")
        @JvmStatic

        fun findByUniqueId(context: Context, username: String): DeviceInformationModel? {
            val items = RealmSingleton.getInstance(context).DefaultRealm.where(DeviceInformationModel::class.java).equalTo("username", username).findAll()
            if (items.size > 0) {
                return items.first()
            }
            return null
        }

        fun removeDevicePerUser(context: Context, username: String): Boolean {
            val device = this.findByUniqueId(context, username)
            if (device != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        device.deleteFromRealm()
                    }
                    return true
                } catch (exc: Exception) {

                }
            }
            return false
        }

        fun update(context: Context, username: String, deviceName: String, deviceModel: String, isMe: Boolean, state: Boolean): Boolean {
            var device = this.findByUniqueId(context, username)
            if (device != null) {
                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {
                        device?.device_model = deviceModel
                        device?.device_name = deviceName
                        device?.is_me = isMe
                        device?.state = state

                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(device)
                    }
//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            } else {
                device = DeviceInformationModel()
                device?.device_model = deviceModel
                device?.device_name = deviceName
                device?.is_me = isMe
                device?.state = state
                device?.username = username

                try {
                    RealmSingleton.getInstance(context).DefaultRealm.executeTransaction {

                        RealmSingleton.getInstance(context).DefaultRealm.copyToRealmOrUpdate(device)
                    }

//                RealmSingleton.getInstance(context).DefaultRealm.beginTransaction()
//                RealmSingleton.getInstance(context).DefaultRealm.commitTransaction()
                    return true
                } catch (exc: Exception) {
                    RealmSingleton.getInstance(context).DefaultRealm.cancelTransaction()
                }
            }
            return false
        }

    }
}