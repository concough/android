package com.concough.android.singletons

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.concough.android.concough.R
import com.concough.android.general.AlertClass
import com.concough.android.rest.SettingsRestAPIClass
import com.concough.android.services.SynchronizerService
import com.concough.android.settings.APP_VERSION
import com.concough.android.settings.CONNECTION_MAX_RETRY
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by abolfazl on 12/8/18.
 */
class SynchronizationSingleton private constructor(context: Context) {
    public enum class SynchronizationState {
        Started, Stopped;
    }

    private var shouldUnbind: Boolean = false
    private var context: Context? = context
    private var boundService: SynchronizerService? = null

    companion object {
        private const val TAG = "SynchronizationSingleton"
        private var sharedInstance: SynchronizationSingleton? = null

        @JvmStatic
        fun getInstance(context: Context): SynchronizationSingleton {
            if (sharedInstance == null) {
                sharedInstance = SynchronizationSingleton(context.applicationContext)
            }

            return sharedInstance!!
        }
    }

    private var connection: ServiceConnection? = null
//    private val connection: ServiceConnection = object: ServiceConnection {
//        override fun onServiceDisconnected(p0: ComponentName?) {
//            this@SynchronizationSingleton.boundService!!.stopSync()
//            this@SynchronizationSingleton.boundService = null
//        }
//
//        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
//            this@SynchronizationSingleton.boundService = (p1 as SynchronizerService.LocalBinder).service
//            this@SynchronizationSingleton.boundService!!.init()
//            this@SynchronizationSingleton.boundService!!.startSync()
//        }
//    }

    public fun startSynchronizer() {
        val intent = Intent(context, SynchronizerService::class.java)
        if (context!!.bindService(intent, object: ServiceConnection {
                    override fun onServiceDisconnected(p0: ComponentName?) {
                        if (this@SynchronizationSingleton.boundService != null) {
                            this@SynchronizationSingleton.boundService!!.stopSync()
                            this@SynchronizationSingleton.boundService = null
                        }
                    }

                    @SuppressLint("LongLogTag")
                    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                        //Log.d(TAG, "service connected")
                        if (this@SynchronizationSingleton.boundService == null) {
                            this@SynchronizationSingleton.connection = this
                            this@SynchronizationSingleton.boundService = (p1 as SynchronizerService.LocalBinder).service
                            this@SynchronizationSingleton.boundService!!.init()
                            this@SynchronizationSingleton.boundService!!.startSync()
                        }
                    }
                } , Context.BIND_AUTO_CREATE)) {
            shouldUnbind = true
        }
    }

    public fun stopSynchronizer() {
        if(this.shouldUnbind) {
            this.context!!.unbindService(this.connection!!)
            this.shouldUnbind = false
        }
    }
}