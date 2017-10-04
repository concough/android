package com.concough.android.singletons

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.concough.android.downloader.EntrancePackageDownloader


/**
 * Created by abolfazl on 7/30/17.
 */
class DownloaderSingleton private constructor() {
    public enum class DownloaderState {
        Initialize, Started, Finished;
    }
    private data class DownloaderObject (var type: String, var obj: Any, var state: DownloaderState)

    interface DownloaderSingletonListener {
        fun onDownloadergetReady(downloader: Any, index: Int)
    }

    // variables
    private var downloaders: HashMap<String, DownloaderObject> = HashMap()
    private var isStarted = false
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

    public var IsInDownloadProgress: Boolean = false
        private set
        get() = isStarted

    public fun getMeDownloader(context: Context, type: String, uniqueId: String, index: Int = 0, completion: (downloader: Any, index: Int) -> Unit) {

        if (type == "Entrance") {
            if (this.downloaders.keys.contains(uniqueId)) {
                val downloader = this.downloaders.get(uniqueId)?.obj
                completion(downloader!!, index)
//                if (this.listener != null) {
////                    this.listener!!.onDownloadergetReady(downloader!!, index)
//                }
            } else {
                val intent = Intent(context.applicationContext, EntrancePackageDownloader::class.java)
                context.applicationContext.bindService(intent, object : ServiceConnection {
                    override fun onServiceConnected(className: ComponentName, service: IBinder) {
                        Log.d(TAG, "EntrancePackageDownloader created" )
                        val binder = service as EntrancePackageDownloader.LocalBinder
                        val service = binder.service

                        this@DownloaderSingleton.downloaders.put(uniqueId, DownloaderObject(type, service, DownloaderState.Initialize))
                        completion(service, index)

//                        if (this@DownloaderSingleton.listener != null) {
//                            this@DownloaderSingleton.listener?.onDownloadergetReady(service, index)
//                        }
                    }

                    override fun onServiceDisconnected(arg0: ComponentName) {
                    }
                }, Context.BIND_AUTO_CREATE)

            }
        }
    }

    public fun unbind(context: Context, uniqueId : String) {
        if (this.downloaders.keys.contains(uniqueId)) {
//            context.unbindService((this.downloaders.get(uniqueId)?.obj as EntrancePackageDownloader).un)
        }
    }

    public fun removeDownloader(uniqueId: String) {
        if (this.downloaders.keys.contains(uniqueId)) {
            this.downloaders.remove(uniqueId)
            isStarted = false
        }
    }

    public fun setDownloaderStarted(uniqueId: String) {
        if (this.downloaders.keys.contains(uniqueId)) {
            this.downloaders[uniqueId]?.state = DownloaderState.Started
            isStarted = true
        }
    }

    public fun setDownloaderFinished(uniqueId: String) {
        if (this.downloaders.keys.contains(uniqueId)) {
            this.downloaders[uniqueId]?.state = DownloaderState.Finished
            isStarted = false
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