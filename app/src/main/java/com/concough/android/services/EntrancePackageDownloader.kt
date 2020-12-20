package com.concough.android.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import android.util.Base64
import android.util.Log
import com.concough.android.concough.FavoritesActivity
import com.concough.android.concough.R
import com.concough.android.general.AlertClass
import com.concough.android.models.EntranceModelHandler
import com.concough.android.models.EntrancePackageHandler
import com.concough.android.models.EntranceQuestionModelHandler
import com.concough.android.models.PurchasedModelHandler
import com.concough.android.rest.EntranceRestAPIClass
import com.concough.android.rest.MediaRestAPIClass
import com.concough.android.settings.CONNECTION_MAX_RETRY
import com.concough.android.settings.DOWNLOADER_QUEUE_COUNT
import com.concough.android.settings.DOWNLOAD_IMAGE_COUNT
import com.concough.android.settings.SECRET_KEY
import com.concough.android.singletons.DownloaderSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.NotificationSingleton
import com.concough.android.singletons.UserDefaultsSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.utils.MD5Digester
import com.concough.android.utils.monthToString
import com.google.gson.JsonParser
import org.cryptonode.jncryptor.AES256JNCryptor
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.locks.Lock
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


/**
 * Created by abolfazl on 7/30/17.
 */
class EntrancePackageDownloader : Service(), Handler.Callback {

    private enum class DownloadState {
        CREATED, STARTED, FINISHED, FAILED
    }
    private data class DownloadPartsStruct(var ids: HashMap<String, String>, var state: DownloadState)

    interface EntrancePackageDownloaderListener {
        fun onDownloadProgress(count: Int)
        fun onDownloadprogressForViewHolder(count: Int, totalCount: Int, index: Int)
        fun onDownloadPaused()
        fun onDownloadPausedForViewHolder(index: Int)
        fun onDismissActivity(boolean: Boolean)
        fun onDownloadImagesFinished(result: Boolean)
        fun onDownloadImagesFinishedForViewHolder(result: Boolean, index: Int)
    }

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

    private var context: Context? = null
    var listener: EntrancePackageDownloaderListener? = null

    private var entranceUniqueId: String = ""
    private var imageList: LinkedHashMap<String, String> = LinkedHashMap()
    private var questionsList: LinkedHashMap<String, ArrayList<Pair<String, Boolean>>> = LinkedHashMap()
    private var vcType: String = ""
    private var username: String = ""
    private var indexPath: Int? = null
    private var retryCounter: Int = 0
    private lateinit var saveDirectory: File

    private var downloadParts: ArrayList<ArrayList<DownloadPartsStruct>> = ArrayList()
    private var operationQueueArray: ArrayList<HandlerThread> = ArrayList()
    private var lock: Any = Any()

    public var DownloadCount: Int = 0
        get
        private set

    public var DownloadedCount: Int = 0
        get
        private set

    override fun handleMessage(message: Message?): Boolean {
        message?.let {
            when (message.what) {
                0 -> {
                    val data = message.data
                    val queueId = data.getInt("QUEUE_ID")

                    processNextMulti2(saveDirectory, queueId)
                }
            }
        }
        return true
    }


    public fun initialize(context: Context, entranceUniqueId: String, vcType: String, username: String, index: Int) {
        this.context = context
        this.entranceUniqueId = entranceUniqueId
        this.vcType = vcType
        this.username = username
        this.indexPath = index
    }

    public fun registerActivity(context: Context, vcType: String, index: Int) {
        this.context = context
        this.vcType = vcType
        this.indexPath = index
    }

    public fun fillImageArray(): Boolean {
        val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
        val questions = EntranceQuestionModelHandler.getQuestionsNotDownloaded(applicationContext, username, entranceUniqueId)
        if (questions != null) {
            if (questions.count() > 0) {
                questionsList.clear()
                imageList.clear()

                try {
                    for (q in questions) {
                        val imagesArray = JsonParser().parse(q.images).asJsonArray
                        if (imagesArray != null) {
                            for (item in imagesArray) {
                                val imageUniqueId = item.asJsonObject.get("unique_key").asString
                                this.imageList.put(imageUniqueId, q.uniqueId)

                                if (!this.questionsList.containsKey(q.uniqueId)) {
                                    this.questionsList.put(q.uniqueId, ArrayList())
                                }
                                this.questionsList.get(q.uniqueId)?.add(Pair(imageUniqueId, false))
                            }
                        }
                    }
                    this.DownloadCount = this.imageList.count()
                    this.DownloadedCount = 0
                    return true

                } catch (exc: Exception) {
                }

            }
        }

        return false
    }

    public fun downloadPackageImages() {
        for ( i in 0 until DOWNLOADER_QUEUE_COUNT) {
            val handler = Handler(this.operationQueueArray[i].looper, this)
            val msg = handler.obtainMessage(0)

            val bundle = Bundle()
            bundle.putInt("QUEUE_ID", i)
            msg.data = bundle

            handler.sendMessage(msg)
        }
    }

    public fun downloadPackageImages(saveDirectory: File) {
//        processNext(saveDirectory)
//        processNextMulti(saveDirectory)

        this.saveDirectory = saveDirectory

        for (item in this.operationQueueArray) {
            item.quit()
        }
        this.operationQueueArray.clear()
        for (i in 0 until DOWNLOADER_QUEUE_COUNT) {
            this.operationQueueArray.add(HandlerThread("Downloader$i"))
            this.operationQueueArray[i].start()
        }

        this.downloadParts.clear()
        for (i in 0 until DOWNLOADER_QUEUE_COUNT) {
            this.downloadParts.add(ArrayList())
        }

        val partsCount = (this.imageList.size / DOWNLOAD_IMAGE_COUNT) + 1

        var index = 0
        for (i in 0 until partsCount) {
            var ids = HashMap<String, String>()
            for (j in 1..DOWNLOAD_IMAGE_COUNT) {
                if (imageList.count() > 0) {
                    val itemKey = imageList.keys.toList()[0]
                    val itemValue = imageList[itemKey]
                    imageList.remove(itemKey)

                    ids[itemKey] = itemValue!!
                } else {
                    break
                }
            }

            if (ids.size > 0) {
                this.downloadParts[index].add(DownloadPartsStruct(ids, DownloadState.CREATED))

                index += 1
                if (index == DOWNLOADER_QUEUE_COUNT)
                    index = 0
            }
        }

        this.downloadPackageImages()
    }

    private fun processNext(saveDirectory: File) {
        if (imageList.count() > 0) {
            val itemKey = imageList.keys.toList().get(0)
            val itemValue = imageList.get(itemKey)
            imageList.remove(itemKey)

            downloadOneImage(saveDirectory, itemKey, itemValue!!)
        } else {
            runOnUiThread {
                if (verifyDownload()) {
                    // ok --> downloaded successfully
                    try {
                        PurchasedModelHandler.setIsDownloadedTrue(context!!.applicationContext, username, entranceUniqueId, "Entrance")
                        DownloaderSingleton.getInstance().setDownloaderFinished(entranceUniqueId)

                        val entrance = EntranceModelHandler.getByUsernameAndId(context!!.applicationContext, username, entranceUniqueId)
                        if (entrance != null) {
                            val year = FormatterSingleton.getInstance().NumberFormatter.format(entrance.year)
                            val message = "دانلود آزمون به اتمام رسید"
                            val subMessage = " ${entrance.type} سال ${year} " + "\n" +
                                    "${entrance.set} (${entrance.group})"

                            NotificationSingleton.getInstance(context!!).simpleNotification(message, subMessage)
                        }

                        if (vcType == "ED") {
                            if (listener != null) {
                                listener!!.onDownloadImagesFinished(true)
                            }
                        } else if (vcType == "F") {
                            if (listener != null) {
                                listener!!.onDownloadImagesFinishedForViewHolder(true, indexPath!!)
                            }
                        }
                    } catch (exc: Exception) {
                    }
                } else {
                    if (vcType == "ED") {
                        if (listener != null) {
                            listener!!.onDownloadImagesFinished(false)
                        }
                    } else if (vcType == "F") {
                        if (listener != null) {
                            listener!!.onDownloadImagesFinishedForViewHolder(false, indexPath!!)
                        }
                    }
                }
            }
        }
    }

//    private fun processNextMulti(saveDirectory: File) {
//        var ids: LinkedHashMap<String, String> = LinkedHashMap()
//        for(i in 1..DOWNLOAD_IMAGE_COUNT) {
//            if (imageList.count() > 0) {
//                val itemKey = imageList.keys.toList().get(0)
//                val itemValue = imageList.get(itemKey)
//                imageList.remove(itemKey)
//
//                ids[itemKey] = itemValue!!
//            } else {
//                break
//            }
//        }
//
//        if (ids.count() > 0) {
//            downloadMultiImage(saveDirectory, ids)
//        } else {
//            runOnUiThread {
//                if (verifyDownload()) {
//                    // ok --> downloaded successfully
//                    try {
//                        PurchasedModelHandler.setIsDownloadedTrue(context!!.applicationContext, username, entranceUniqueId, "Entrance")
//                        DownloaderSingleton.getInstance().setDownloaderFinished(entranceUniqueId)
//
//                        val entrance = EntranceModelHandler.getByUsernameAndId(context!!.applicationContext, username, entranceUniqueId)
//                        if (entrance != null) {
//                            val year = FormatterSingleton.getInstance().NumberFormatter.format(entrance.year)
//                            val month = monthToString(entrance.month)
//                            val message= "دانلود آزمون به اتمام رسید"
//                            val subMessage =" ${entrance.type} ${month} ${year} " +"\n" +
//                                    "${entrance.set} (${entrance.group})"
//
//                            simpleNotification(message,subMessage)
//                        }
//
//                        if (vcType == "ED") {
//                            if (listener != null) {
//                                listener!!.onDownloadImagesFinished(true)
//                            }
//                        } else if (vcType == "F") {
//                            if (listener != null) {
//                                listener!!.onDownloadImagesFinishedForViewHolder(true, indexPath!!)
//                            }
//                        }
//                    } catch (exc: Exception) {
//                    }
//                } else {
//                    if (vcType == "ED") {
//                        if (listener != null) {
//                            listener!!.onDownloadImagesFinished(false)
//                        }
//                    } else if (vcType == "F") {
//                        if (listener != null) {
//                            listener!!.onDownloadImagesFinishedForViewHolder(false, indexPath!!)
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun processNextMulti2(saveDirectory: File, queueId: Int) {
        for (i in 0 until this.downloadParts[queueId].size) {
            if (this.downloadParts[queueId][i].state == DownloadState.CREATED) {
                this.downloadMultiImage(saveDirectory, this.downloadParts[queueId][i].ids, queueId, i)
                break
            }
        }
    }

    public fun downloadOneImage(saveDirectory: File, imageId: String, questionId: String) {
        doAsync {
            MediaRestAPIClass.downloadEntranceQuestionImage(applicationContext, entranceUniqueId, imageId, completion = { data, error ->
                runOnUiThread {
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            downloadOneImage(saveDirectory, imageId, questionId)
                        }
                    } else {
                        if (data != null) {
                            val filePath = "$saveDirectory/$imageId"

                            try {
                                val file = File(saveDirectory, imageId)
                                if (!file.exists()) {
                                    var out = FileOutputStream(file)
                                    out.write(data)
                                    out.flush()
                                    out.close()

//                                    var stream: FileOutputStream = openFileOutput(filePath, Context.MODE_PRIVATE);
//                                    stream.write(data);
//                                    stream.close()
//                                    file.writeBytes(data)
                                }

                                var index: Int? = null

                                for (item in questionsList.get(questionId)!!.iterator()) {
                                    index = questionsList.get(questionId)!!.indexOfFirst { t ->
                                        if (t.first == item.first) {
                                            return@indexOfFirst true
                                        }
                                        return@indexOfFirst false
                                    }

                                    if (index != null) {
                                        val item1 = questionsList.get(questionId)!![index]
                                        questionsList.get(questionId)!![index] = Pair(item1.first, true)
                                    }
                                }

                                var downloadComplete = true
                                for (item in questionsList[questionId]!!) {
                                    if (!item.second) {
                                        downloadComplete = false
                                    }
                                }

                                if (downloadComplete) {
                                    EntranceQuestionModelHandler.changeDownloadedToTrue(applicationContext, username, questionId, entranceUniqueId)
                                }

                                if (vcType == "ED") {
                                    if (listener != null) {
                                        listener!!.onDownloadProgress(imageList.count())
                                    }
                                } else if (vcType == "F") {
                                    if (listener != null) {
                                        listener!!.onDownloadprogressForViewHolder(imageList.count(), DownloadCount as Int, indexPath!!)
                                    }
                                }
                            } catch (exc: Exception) {
                                Log.d(TAG, exc.message);
                            }
                        }
                    }

                    processNext(saveDirectory)
                }

            }, failure = { error ->
                runOnUiThread {
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                    AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "NetworkError", error.name, "error", null)
                                }
                            }
                            else -> {
                                if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                    AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "NetworkError", error.name, "", null)
                                }
                            }
                        }
                    }

                    if (vcType == "ED") {
                        if (listener != null) {
                            listener?.onDownloadPaused()
                        }
                    } else if (vcType == "F") {
                        if (listener != null) {
                            listener?.onDownloadPausedForViewHolder(indexPath!!)
                        }
                    }

                }
            })
        }

    }

    public fun downloadMultiImage(saveDirectory: File, ids: HashMap<String, String>,
                                  queueId: Int, itemId: Int) {
        this.downloadParts[queueId][itemId].state = DownloadState.STARTED

        doAsync {
            MediaRestAPIClass.downloadEntranceQuestionBulkImages(applicationContext, entranceUniqueId, ids.keys.toTypedArray(), completion = { data, error ->
                runOnUiThread {
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            this@EntrancePackageDownloader.downloadParts[queueId][itemId].state = DownloadState.CREATED
                            val handler = Handler(this@EntrancePackageDownloader.operationQueueArray[queueId].looper, this@EntrancePackageDownloader)
                            val msg = handler.obtainMessage(0)

                            val bundle = Bundle()
                            bundle.putInt("QUEUE_ID", queueId)
                            msg.data = bundle

                            handler.sendMessage(msg)

                        } else {
                            if (this@EntrancePackageDownloader.retryCounter < CONNECTION_MAX_RETRY) {
                                this@EntrancePackageDownloader.retryCounter += 1

                                this@EntrancePackageDownloader.downloadParts[queueId][itemId].state = DownloadState.CREATED
                                val handler = Handler(this@EntrancePackageDownloader.operationQueueArray[queueId].looper, this@EntrancePackageDownloader)
                                val msg = handler.obtainMessage(0)

                                val bundle = Bundle()
                                bundle.putInt("QUEUE_ID", queueId)
                                msg.data = bundle

                                handler.sendMessage(msg)
                            } else {
                                this@EntrancePackageDownloader.retryCounter = 0
//                                if (vcType == "ED") {
//                                    if (listener != null) {
//                                        listener?.onDownloadPaused()
//                                    }
//                                } else if (vcType == "F") {
//                                    if (listener != null) {
//                                        listener?.onDownloadPausedForViewHolder(indexPath!!)
//                                    }
//                                }
                                this@EntrancePackageDownloader.downloadParts[queueId][itemId].state = DownloadState.FAILED
                            }
                        }
                    } else {
                        this@EntrancePackageDownloader.retryCounter = 0
                        if (data != null) {
                            try {
                            var qs_strings: String = String(data,Charsets.UTF_8)
                            var qs = qs_strings.split("$$$$$$$#$$$$$$$$")
                            for(q in qs) {
                                val parts = q.split("@@@@@@@#@@@@@@@@")

                                if (ids.keys.contains(parts[0])) {
                                    val questionId = ids[parts[0]]!!

//                                    val filePath = "$saveDirectory/${parts[0]}"

                                        val file = File(saveDirectory, parts[0])
                                        if (!file.exists()) {
                                            var out = FileOutputStream(file)
                                            if(out!=null){
                                                out.write(parts[1].toByteArray(Charsets.UTF_8))
                                                out.flush()
                                                out.close()
                                            }

//                                    var stream: FileOutputStream = openFileOutput(filePath, Context.MODE_PRIVATE);
//                                    stream.write(data);
//                                    stream.close()
//                                    file.writeBytes(data)
                                        }

                                        var index: Int? = null

                                        for (item in questionsList.get(questionId)!!.iterator()) {
                                            index = questionsList.get(questionId)!!.indexOfFirst { t ->
                                                if (t.first == item.first) {
                                                    return@indexOfFirst true
                                                }
                                                return@indexOfFirst false
                                            }

                                            if (index != null) {
                                                val item1 = questionsList.get(questionId)!![index]
                                                questionsList.get(questionId)!![index] = Pair(item1.first, true)
                                            }
                                        }

                                        var downloadComplete = true
                                        for (item in questionsList[questionId]!!) {
                                            if (!item.second) {
                                                downloadComplete = false
                                            }
                                        }

                                        if (downloadComplete) {
                                            EntranceQuestionModelHandler.changeDownloadedToTrue(applicationContext, username, questionId, entranceUniqueId)
                                        }
                                }
                            }

                                this@EntrancePackageDownloader.downloadParts[queueId][itemId].state = DownloadState.FINISHED

                                synchronized(this@EntrancePackageDownloader.lock) {
                                    this@EntrancePackageDownloader.DownloadedCount += DOWNLOAD_IMAGE_COUNT
                                }

                            if (vcType == "ED") {
                                    if (listener != null) {
                                        listener!!.onDownloadProgress(this@EntrancePackageDownloader.DownloadCount - this@EntrancePackageDownloader.DownloadedCount)
                                    }
                                } else if (vcType == "F") {
                                    if (listener != null) {
                                        listener!!.onDownloadprogressForViewHolder(
                                                this@EntrancePackageDownloader.DownloadCount - this@EntrancePackageDownloader.DownloadedCount,
                                                DownloadCount, indexPath!!)
                                    }
                                }
                            qs_strings=""
                            qs= emptyList()

                            } catch (exc: Exception) {
//                                Log.d(TAG, exc.message)
                            }

                        }
                    }

                    this@EntrancePackageDownloader.checkDownloadStatus()

                    val handler = Handler(this@EntrancePackageDownloader.operationQueueArray[queueId].looper, this@EntrancePackageDownloader)
                    val msg = handler.obtainMessage(0)

                    val bundle = Bundle()
                    bundle.putInt("QUEUE_ID", queueId)
                    msg.data = bundle

                    handler.sendMessage(msg)

//                    processNextMulti(saveDirectory)
                }

            }, failure = { error ->
                runOnUiThread {
                    if (this@EntrancePackageDownloader.retryCounter < CONNECTION_MAX_RETRY) {
                        this@EntrancePackageDownloader.retryCounter += 1

                        this@EntrancePackageDownloader.downloadParts[queueId][itemId].state = DownloadState.CREATED
                        val handler = Handler(this@EntrancePackageDownloader.operationQueueArray[queueId].looper, this@EntrancePackageDownloader)
                        val msg = handler.obtainMessage(0)

                        val bundle = Bundle()
                        bundle.putInt("QUEUE_ID", queueId)
                        msg.data = bundle

                        handler.sendMessage(msg)

                    } else {
                        this@EntrancePackageDownloader.retryCounter = 0
                        if (error != null) {
                            when (error) {
                                NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                    if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                        AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "NetworkError", error.name, "error", null)
                                    }
                                }
                                else -> {
                                    if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                        AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "NetworkError", error.name, "", null)
                                    }
                                }
                            }
                        }

                        this@EntrancePackageDownloader.downloadParts[queueId][itemId].state = DownloadState.FAILED
                        if (vcType == "ED") {
                            if (listener != null) {
                                listener?.onDownloadPaused()
                            }
                        } else if (vcType == "F") {
                            if (listener != null) {
                                listener?.onDownloadPausedForViewHolder(indexPath!!)
                            }
                        }
                    }
                }
            })
        }

    }

    private fun checkDownloadStatus() {
        runOnUiThread {
            if (verifyDownload()) {
                // ok --> downloaded successfully
                try {
                    PurchasedModelHandler.setIsDownloadedTrue(context!!.applicationContext, username, entranceUniqueId, "Entrance")
                    DownloaderSingleton.getInstance().setDownloaderFinished(entranceUniqueId)

                    val entrance = EntranceModelHandler.getByUsernameAndId(context!!.applicationContext, username, entranceUniqueId)
                    if (entrance != null) {
                        val year = FormatterSingleton.getInstance().NumberFormatter.format(entrance.year)
                        val month = monthToString(entrance.month)
                        val message= "دانلود آزمون به اتمام رسید"
                        val subMessage =" ${entrance.type} $month $year " +"\n" +
                                "${entrance.set} (${entrance.group})"

                        NotificationSingleton.getInstance(applicationContext).simpleNotification(message, subMessage)
                    }

                    if (vcType == "ED") {
                        if (listener != null) {
                            listener!!.onDownloadImagesFinished(true)
                        }
                    } else if (vcType == "F") {
                        if (listener != null) {
                            listener!!.onDownloadImagesFinishedForViewHolder(true, indexPath!!)
                        }
                    }
                } catch (exc: Exception) {
                }
            } else {
                var status: ArrayList<DownloadState> = ArrayList()
                for (item in this@EntrancePackageDownloader.downloadParts) {
                    for (item2 in item) {
                        status.add(item2.state)
                    }
                }

                if (status.contains(DownloadState.CREATED) || status.contains(DownloadState.STARTED)) {
                } else if (status.contains(DownloadState.FAILED)) {
                    if (vcType == "ED") {
                        if (listener != null) {
                            listener!!.onDownloadPaused()
                            listener!!.onDownloadImagesFinished(false)
                        }
                    } else if (vcType == "F") {
                        if (listener != null) {
                            listener!!.onDownloadPausedForViewHolder(indexPath!!)
                            listener!!.onDownloadImagesFinishedForViewHolder(false, indexPath!!)
                        }
                    }

                }
            }

        }
    }

    fun verifyDownload(): Boolean {
        try {
            val questions = EntranceQuestionModelHandler.getQuestionsNotDownloaded(applicationContext, username, entranceUniqueId)
            if (questions?.count()!! > 0) {
                return false
            }
        } catch (exc: Exception) {
        }

        return true
    }

    fun downloadInitialData(completion: (result: Boolean, indexPath: Int?) -> Unit) {
        doAsync {
            EntranceRestAPIClass.getEntrancePackageDataInit(context?.applicationContext!!, entranceUniqueId, completion = { data, error ->
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            downloadInitialData(completion)
                        } else {
                            if (this@EntrancePackageDownloader.retryCounter < CONNECTION_MAX_RETRY) {
                                this@EntrancePackageDownloader.retryCounter += 1
                                downloadInitialData(completion)
                            } else {
                                this@EntrancePackageDownloader.retryCounter = 0
                                if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                    runOnUiThread {
                                        AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                                    }
                                }
                            }
                        }
                    } else {
                        this@EntrancePackageDownloader.retryCounter = 0
                        if (data != null) {
                            try {
                                val status = data.asJsonObject.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        try {
                                            val packageStr = data.asJsonObject.get("package").asString
                                            val decodedData = Base64.decode(packageStr, Base64.DEFAULT)
                                            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()

                                            val hashStr = "$username:$SECRET_KEY"
                                            val hashKey = MD5Digester.digest(hashStr)

                                            val originalTextBytes = AES256JNCryptor(1023).decryptData(decodedData, hashKey.toCharArray())
                                            val originalText = String(originalTextBytes)

                                            val content = JsonParser().parse(originalText)
                                            val initData = content.asJsonObject.get("init")

                                            runOnUiThread {
                                                val result = EntrancePackageHandler.savePackage(applicationContext, username!!, entranceUniqueId, initData)
                                                if (result.status) {
                                                    imageList = result.images
                                                    questionsList = result.questionList
                                                    DownloadCount = imageList.count()
                                                    DownloadedCount = 0

                                                    completion(true, indexPath)

                                                } else {
                                                    EntrancePackageHandler.removePackage(applicationContext, username!!, entranceUniqueId)
                                                }
                                            }

                                            Log.d(TAG, "testing")
                                        } catch (exc: Exception) {
                                            Log.d(TAG, exc.message)
                                        }
                                    }

                                    "Error" -> {
                                        val errorType = data.asJsonObject.get("error_type").asString
                                        when (errorType) {
                                            "PackageNotExist", "EntranceNotExist" -> {
                                                if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                                    runOnUiThread {
                                                        AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "EntranceResult", "EntranceNotExist", "error", null)
                                                    }
                                                }

                                                if (vcType == "ED") {
                                                    if (listener != null) {
                                                        listener?.onDismissActivity(true)
                                                    }
                                                }
                                            }
                                            else -> {
                                            }
                                        }
                                    }
                                }

                            } catch (exc: Exception) {

                            }
                        }

                    }

            }, failure = { error ->
                runOnUiThread {
                    if (this@EntrancePackageDownloader.retryCounter < CONNECTION_MAX_RETRY) {
                        this@EntrancePackageDownloader.retryCounter += 1
                        downloadInitialData(completion)
                    } else {
                        this@EntrancePackageDownloader.retryCounter = 0

                        if (error != null) {

                            when (error) {
                                NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                    if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                        AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "NetworkError", error.name, "error", null)
                                    }
                                }
                                else -> {
                                    if (this@EntrancePackageDownloader.context != null && this@EntrancePackageDownloader.context is Activity) {
                                        AlertClass.showTopMessage(this@EntrancePackageDownloader.context!!, (context as Activity).findViewById(R.id.container), "NetworkError", error.name, "", null)
                                    }
                                }
                            }
                        }

                        if (vcType == "ED") {
                            if (listener != null) {
                                listener?.onDownloadPaused()
                            }
                        } else if (vcType == "F") {
                            if (listener != null) {
                                listener?.onDownloadPausedForViewHolder(indexPath!!)
                            }
                        }
                    }
                }
            })
        }

        completion(false, null)
    }
}