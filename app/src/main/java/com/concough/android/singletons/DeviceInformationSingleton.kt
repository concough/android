package com.concough.android.singletons

import android.content.Context
import android.content.SharedPreferences
import com.concough.android.models.DeviceInformationModelHandler

/**
 * Created by Owner on 10/8/2017.
 */

class DeviceInformationSingleton {

    data class DeviceStateStruct(var deviceName: String, var deviceModel: String, var deviceState: Boolean)

    private var prefs: SharedPreferences
    private var context: Context

    private constructor(context: Context) {
        this.context = context
        this.prefs = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
    }

    companion object Factory {
        val TAG: String = "DeviceInformationSingleton"
        val FILENAME = "device"

        private var sharedInstance: DeviceInformationSingleton? = null

        @JvmStatic
        fun getInstance(context: Context): DeviceInformationSingleton {
            if (sharedInstance == null)
                sharedInstance = DeviceInformationSingleton(context)

            return sharedInstance!!
        }
    }

    private fun setValueAsString(key: String, value: String) {
        this.prefs.edit().putString(key, value).apply()
    }

    private fun getValueAsString(key: String): String? {
        return this.prefs.getString(key, null)
    }

    private fun setValueAsBoolean(key: String, value: Boolean) {
        this.prefs.edit().putBoolean(key, value).apply()
    }

    private fun getValueAsBoolean(key: String): Boolean {
        return this.prefs.getBoolean(key, false)
    }


    private fun setValueAsInteger(key: String, value: Int) {
        this.prefs.edit().putInt(key, value).apply()
    }

    private fun getValueAsInteger(key: String): Int {
        return this.prefs.getInt(key, 0)
    }


    public fun clearAll(username: String): Boolean {
        this.prefs.edit().clear().apply()
        DeviceInformationModelHandler.removeDevicePerUser(context, username)
        return true
    }

    fun getLastAppVersion(): Int {
        return this.getValueAsInteger("AppVersion")
    }

    fun getLastAppVersionCount(version: Int): Int {
        return this.getValueAsInteger("AppVersion.${version}_count")
    }

    fun putLastAppVersion(version: Int) {
        this.setValueAsInteger("AppVersion", version)
        val count = this.getLastAppVersionCount(version)
        if (count == 0) {
            setValueAsInteger("AppVersion.${version}_count", 1)
        } else {
            setValueAsInteger("AppVersion.${version}_count", count + 1)
        }

    }


    fun setDeviceState(username: String, deviceName: String, deviceModel: String, state: Boolean, isMe: Boolean): Boolean {
        return DeviceInformationModelHandler.update(context, username, deviceName, deviceModel, isMe, state)
    }

    fun getDeviceState(username: String): DeviceStateStruct? {
        val device = DeviceInformationModelHandler.findByUniqueId(context,username)
        if(device!=null){
            val deviceStruct = DeviceStateStruct(deviceName = device.device_name, deviceModel = device.device_model,deviceState = device.state)

        }
        return null
    }
}