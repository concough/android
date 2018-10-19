package com.concough.android.concough

import android.app.Application
import android.content.res.Configuration
import net.danlew.android.joda.JodaTimeAndroid

/**
 * Created by abolfazl on 10/16/18.
 */
class MainApplication: Application() {
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}