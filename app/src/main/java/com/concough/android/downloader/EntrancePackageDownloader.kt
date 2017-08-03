package com.concough.android.downloader

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import java.util.*

/**
 * Created by abolfazl on 7/30/17.
 */
class EntrancePackageDownloader : Service() {

    inner class LocalBinder : Binder() {
        internal val service: EntrancePackageDownloader
            get() = this@EntrancePackageDownloader
    }

    companion object {
        private val TAG = "EPDService"

        public fun newIntent(context: Context): Intent {
            return Intent(context, EntrancePackageDownloader::class.java)
        }
    }

    private val binder: IBinder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return this.binder
    }

    private var entranceUniqueId: String = ""
    private var imageList: HashMap<String, String> = HashMap()
    private var questionsList: HashMap<String, ArrayList<Pair<String, Boolean>>> = HashMap()
    private var vcType: String = ""
    private var username: String = ""
    private var indexPath: Int? = null

    public var DownloadCount: Number = 0
        private set

    fun onHandleIntent(intent: Intent?) {
    }

    public fun initialize(entranceUniqueId: String, vcType: String, username: String, index: Int) {
        this.entranceUniqueId = entranceUniqueId
        this.vcType = vcType
        this.username = username
        this.indexPath = index
    }

    public fun registerActivity(vcType: String, index: Int) {
        this.vcType = vcType
        this.indexPath = index
    }

    public fun downloadPackageImages(saveDirectory: String) {

    }

    public fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }
}