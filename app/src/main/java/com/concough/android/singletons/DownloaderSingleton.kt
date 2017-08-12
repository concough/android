package com.concough.android.singletons

import com.concough.android.downloader.EntrancePackageDownloader
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.content.ServiceConnection
import android.util.Log


/**
 * Created by abolfazl on 7/30/17.
 */
class DownloaderSingleton private constructor() {
    public enum class DownloaderState {
        Initialize, Started, Finished;
    }
    private data class DownloaderObject (var type: String, var obj: Any, var state: DownloaderState)

    interface DownloaderSingletonListener {
        fun onDownloadergetReady(downloader: Any)
    }

    // variables
    private var downloaders: HashMap<String, DownloaderObject> = HashMap()
    var listener: DownloaderSingletonListener? = null

    companion object Factory {
        private val TAG = "DownloaderSingleton"
        private var sharedInstance : DownloaderSingleton? = null

        @JvmStatic
        fun  getInstance(): DownloaderSingleton {
            if (sharedInstance == null)
                sharedInstance = DownloaderSingleton()

            return sharedInstance!!
        }
    }

    public fun getMeDownloader(context: Context, type: String, uniqueId: String): Any? {
        if (type == "Entrance") {
            if (this.downloaders.keys.contains(uniqueId)) {
                val downloader = this.downloaders.get(uniqueId)?.obj
                if (this.listener != null) {
                    this.listener!!.onDownloadergetReady(downloader!!)
                }
                return downloader
            } else {
                val intent = Intent(context, EntrancePackageDownloader::class.java)
                context.bindService(intent, object : ServiceConnection {
                    override fun onServiceConnected(className: ComponentName, service: IBinder) {
                        Log.d(TAG, "EntrancePackageDownloader created" )
                        val binder = service as EntrancePackageDownloader.LocalBinder
                        val service = binder.service

                        this@DownloaderSingleton.downloaders.put(uniqueId, DownloaderObject(type, service, DownloaderState.Initialize))

                        if (this@DownloaderSingleton.listener != null) {
                            this@DownloaderSingleton.listener?.onDownloadergetReady(service)
                        }
                    }

                    override fun onServiceDisconnected(arg0: ComponentName) {
                    }
                }, Context.BIND_AUTO_CREATE)

            }
        }
        return null
    }

    public fun removeDownloader(uniqueId: String) {
        if (this.downloaders.keys.contains(uniqueId)) {
            this.downloaders.remove(uniqueId)
        }
    }

    public fun setDownloaderStarted(uniqueId: String) {
        if (this.downloaders.keys.contains(uniqueId)) {
            this.downloaders[uniqueId]?.state = DownloaderState.Started
        }
    }

    public fun setDownloaderFinished(uniqueId: String) {
        if (this.downloaders.keys.contains(uniqueId)) {
            this.downloaders[uniqueId]?.state = DownloaderState.Finished
        }
    }

    public fun getDownloaderState(uniqueId: String): DownloaderState? {
        if (this.downloaders.keys.contains(uniqueId)) {
            return this.downloaders[uniqueId]?.state
        }
        return null
    }

//    private val connection = object : ServiceConnection {
//        override fun onServiceConnected(className: ComponentName, service: IBinder) {
//            val binder = service as LocalBinder
//            if (className.className == EntrancePackageDownloader::class.java.toString()) {
//                Log.d(TAG, "EntrancePackageDownloader created" )
//            }
//        }
//
//        override fun onServiceDisconnected(arg0: ComponentName) {
//            isBound = false
//        }
//    }
}