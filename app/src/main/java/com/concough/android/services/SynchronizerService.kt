package com.concough.android.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import com.concough.android.concough.*
import com.concough.android.general.AlertClass
import com.concough.android.models.*
import com.concough.android.rest.*
import com.concough.android.settings.*
import com.concough.android.singletons.*
import com.concough.android.structures.EntranceStruct
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by abolfazl on 12/8/18.
 */
class SynchronizerService: Service(), Handler.Callback {
    inner class LocalBinder : Binder() {
        internal val service: SynchronizerService
            get() = this@SynchronizerService
    }

    companion object {
        private val TAG = "SynchronizerService"
        private val HANDLE_THREAD_NAME = "Concough-SynchronizerService"

        public fun newIntent(context: Context): Intent {
            return Intent(context, SynchronizerService::class.java)
        }
    }

    private val binder: IBinder = LocalBinder()

    private val delayTimerDown = SYNC_INTERVAL * 1000
    private val delayTimerLog = SYNC_LOG_INTERVAL * 1000
    private var retryCounterMap: HashMap<String, Int> = hashMapOf()
    private var retryCounterLog = 0
    private var direction = "UP"
    private var sessionCounter = 0

    private lateinit var queue: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var syncDownTimer: Timer
    private lateinit var syncLogTimer: Timer

    override fun onBind(p0: Intent?): IBinder {
        return this.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    public fun init() {
        this.handlerThread = HandlerThread(HANDLE_THREAD_NAME)
        this.handlerThread.start()

        val looper: Looper? = this.handlerThread.looper
        if (looper != null) {
            this.queue = Handler(looper, this)
        }

        for(item in SYNC_LIST) {
            this.retryCounterMap[item] = 0
        }
    }

    public fun startSync() {
        val msg = this.queue.obtainMessage(SYNC_LIST.indexOf("CHECK_VERSION"))
        this.queue.sendMessage(msg)

        this.syncDownTimerTick()
        this.syncLogTimerTick()

        this.syncDownTimer = Timer(false)
        this.syncDownTimer.schedule(object: TimerTask(){
            override fun run() {
                this@SynchronizerService.syncDownTimerTick()
            }
        }, this.delayTimerDown.toLong() , this.delayTimerDown.toLong())

        this.syncLogTimer = Timer(false)
        this.syncLogTimer.schedule(object: TimerTask(){
            override fun run() {
                this@SynchronizerService.syncLogTimerTick()
            }
        }, this.delayTimerLog.toLong(), this.delayTimerLog.toLong())
    }

    public fun stopSync() {
        this.syncDownTimer.cancel()
        this.syncLogTimer.cancel()
    }

    // Delegates
    override fun handleMessage(msg: Message?): Boolean {
        when (msg?.what) {
            SYNC_LIST.indexOf("FAVOURITES") -> this.syncWithServer(msg)
            SYNC_LIST.indexOf("LOCK") -> this.checkDeviceStateWithServer(msg)
            SYNC_LIST.indexOf("WALLET") -> this.createWallet(msg)
            SYNC_LIST.indexOf("CHECK_VERSION") -> this.checkVersion(msg)
        }

        return true
    }

    private fun syncDownTimerTick() {
        for (item in SYNC_LIST) {
            when (item) {
                "FAVOURITES", "LOCK", "WALLET" -> {
                    val msg = this.queue.obtainMessage(SYNC_LIST.indexOf(item))
                    this.queue.sendMessage(msg)
                }
            }
        }
    }

    private fun syncLogTimerTick() {
        if (this.direction == "UP") {
           this.syncLogWithServerUP()
        }
    }

    private fun syncLogWithServerUP() {
        runOnUiThread {
            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
            val items = UserLogModelHandler.list(applicationContext, username)
            val count = items.count()

            var limit = SYNC_LOG_LIMIT
            if (count < limit) {
                limit = count
            }

            var jsonArray = ArrayList<HashMap<String, Any>>()
            for (i in 0 until limit) {
                val item = items[i]

                val parser = JsonParser()
                val extraData = parser.parse(item!!.extraData).asJsonObject

                val d = hashMapOf("uniqueId" to item.uniqueId,
                        "logType" to item.logType,
                        "time" to item.created.time / 1000,
                        "extra" to extraData)

                jsonArray.add(d)
            }

            if (jsonArray.count() == 0) return@runOnUiThread

            doAsync {
                UserLogRestAPIClass.syncUp(applicationContext, jsonArray, completion = { data, httpError ->
                    uiThread {
                        if (httpError != HTTPErrorType.Success) {
                            if (httpError == HTTPErrorType.Refresh) {
                                this@SynchronizerService.syncLogWithServerUP()
                            } else {
                                if (this@SynchronizerService.retryCounterLog < CONNECTION_MAX_RETRY) {
                                    this@SynchronizerService.retryCounterLog += 1
                                    this@SynchronizerService.syncLogWithServerUP()
                                } else {
                                    this@SynchronizerService.retryCounterLog = 0
                                }
                            }
                        } else {
                            this@SynchronizerService.retryCounterLog = 0

                            data?.let {
                                try {
                                    val status = data.asJsonObject.get("status").asString
                                    when (status) {
                                        "OK" -> {
                                            val records = data.asJsonObject.get("records").asJsonArray
                                            if (records != null) {
//                                            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
                                                for (item in records) {
                                                    val id = item.asString
                                                    UserLogModelHandler.removeByUniqueId(applicationContext, username, id)
                                                }
                                            }
                                        }
                                        "Error" -> {
                                        }
                                    }
                                } catch (exc: Exception) {
                                }
                            }
                        }
                    }
                }, failure = { networkError ->
                    if (this@SynchronizerService.retryCounterLog < CONNECTION_MAX_RETRY) {
                        this@SynchronizerService.retryCounterLog += 1
                        this@SynchronizerService.syncLogWithServerUP()
                    } else {
                        this@SynchronizerService.retryCounterLog = 0
                    }
                })
            }
        }
    }

    private fun checkVersion(message: Message?) {
        doAsync {
            SettingsRestAPIClass.appLastVersion(applicationContext, { data, error ->
                uiThread {
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            val msg1 = this@SynchronizerService.queue.obtainMessage(SYNC_LIST.indexOf("CHECK_VERSION"))
                            this@SynchronizerService.queue.sendMessage(msg1)
                        }
                    } else {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val version = data.get("version").asInt
                                        val released = data.get("released").asString
                                        val link = data.get("link").asString

                                        var showMsg = false

                                        if (version > APP_VERSION) {
                                            val existVer = DeviceInformationSingleton.getInstance(applicationContext).getLastAppVersion()
                                            if (existVer > 0) {
                                                if (version > existVer) {
                                                    showMsg = true
                                                } else {
                                                    val count = DeviceInformationSingleton.getInstance(applicationContext).getLastAppVersionCount(version)
                                                    if (count <= 2)
                                                        showMsg = true
                                                }
                                            } else {
                                                showMsg = true
                                            }

                                            DeviceInformationSingleton.getInstance(applicationContext).putLastAppVersion(version)
                                        }

                                        if (showMsg) {
                                            val amsg = AlertClass.convertMessage("DeviceAction", "UpdateApp")
                                            val date = FormatterSingleton.getInstance().UTCDateFormatter.parse(released)
                                            val versionInPersian = FormatterSingleton.getInstance().NumberFormatter.format(version)
                                            val dateInPersian = FormatterSingleton.getInstance().getPersianDateString(date)

                                            val message = "نسخه $versionInPersian منتشر شده است\nتاریخ: $dateInPersian"
                                            if ((application as MainApplication).getActiveActivity() != null) {
                                                AlertClass.showSucceessMessageCustom((application as MainApplication).getActiveActivity()!!, amsg.title, message, "دانلود", "بعدا", completion = {
                                                    val intentUri = Uri.parse(link)

                                                    val intent = Intent()
                                                    intent.action = Intent.ACTION_VIEW
                                                    intent.data = intentUri
                                                    applicationContext.startActivity(intent)


                                                }, noCompletion = {
                                                })
                                            }
                                        }
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "EmptyArray" -> {
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
                }
            }, { })
        }
    }

    private fun syncWithServer(message: Message?) {
        val msgIndex = SYNC_LIST.indexOf("FAVOURITES")
        doAsync {
            PurchasedRestAPIClass.getPurchasedList(applicationContext, { jsonElement, httpErrorType ->
                uiThread {
                    if (httpErrorType !== HTTPErrorType.Success) {
                        if (httpErrorType === HTTPErrorType.Refresh) {
                            val msg1 = this@SynchronizerService.queue.obtainMessage(msgIndex)
                            this@SynchronizerService.queue.sendMessage(msg1)
                        } else {
                            if (this@SynchronizerService.retryCounterMap["FAVOURITES"]!! < CONNECTION_MAX_RETRY) {
                                this@SynchronizerService.retryCounterMap["FAVOURITES"] = this@SynchronizerService.retryCounterMap["FAVOURITES"]!! + 1

                                val msg1 = this@SynchronizerService.queue.obtainMessage(msgIndex)
                                this@SynchronizerService.queue.sendMessage(msg1)
                            } else {
                                this@SynchronizerService.retryCounterMap["FAVOURITES"] = 0
                            }
                        }
                    } else {
                        this@SynchronizerService.retryCounterMap["FAVOURITES"] = 0
                        if (jsonElement != null) {
                            val status = jsonElement.asJsonObject.get("status").asString
                            when (status) {
                                "OK" -> try {
                                    val purchasedId = ArrayList<Int>()
                                    val records = jsonElement.asJsonObject.get("records").asJsonArray
                                    val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                    if (username != null) {
                                        for (record in records) {
                                            val id = record.asJsonObject.get("id").asInt
                                            val downloaded = record.asJsonObject.get("downloaded").asInt
                                            val createdStr = record.asJsonObject.get("created").asString
                                            val created = FormatterSingleton.getInstance().UTCDateFormatter.parse(createdStr)

                                            val target = record.asJsonObject.get("target")
                                            val targetType = target.asJsonObject.get("product_type").asString

                                            if (PurchasedModelHandler.getByUsernameAndId(applicationContext, username, id) != null) {
                                                PurchasedModelHandler.updateDownloadTimes(applicationContext, username, id, downloaded)

                                                if ("Entrance" == targetType) {
                                                    val uniqueId = target.asJsonObject.get("unique_key").asString
                                                    if (EntranceModelHandler.getByUsernameAndId(applicationContext, username, uniqueId) == null) {
                                                        val org = target.asJsonObject.get("organization").asJsonObject.get("title").asString
                                                        val type = target.asJsonObject.get("entrance_type").asJsonObject.get("title").asString
                                                        val setName = target.asJsonObject.get("entrance_set").asJsonObject.get("title").asString
                                                        val group = target.asJsonObject.get("entrance_set").asJsonObject.get("group").asJsonObject.get("title").asString
                                                        val setId = target.asJsonObject.get("entrance_set").asJsonObject.get("id").asInt
                                                        val bookletsCount = target.asJsonObject.get("booklets_count").asInt
                                                        val duration = target.asJsonObject.get("duration").asInt
                                                        val year = target.asJsonObject.get("year").asInt
                                                        val month = target.asJsonObject.get("month").asInt

                                                        val extraStr = target.asJsonObject.get("extra_data").asString
                                                        var extraData: JsonElement? = null
                                                        if (extraStr != null && "" != extraStr) {
                                                            try {
                                                                extraData = JsonParser().parse(extraStr)
                                                            } catch (exc: Exception) {
                                                                extraData = JsonParser().parse("[]")
                                                            }

                                                        }

                                                        val lastPublishedStr = target.asJsonObject.get("last_published").asString
                                                        val lastPublished = FormatterSingleton.getInstance().UTCDateFormatter.parse(lastPublishedStr)

                                                        val entrance = EntranceStruct()
                                                        entrance.entranceSetId = setId
                                                        entrance.entranceSetTitle = setName
                                                        entrance.entranceOrgTitle = org
                                                        entrance.entranceLastPublished = lastPublished
                                                        entrance.entranceBookletCounts = bookletsCount
                                                        entrance.entranceDuration = duration
                                                        entrance.entranceExtraData = extraData
                                                        entrance.entranceGroupTitle = group
                                                        entrance.entranceTypeTitle = type
                                                        entrance.entranceUniqueId = uniqueId
                                                        entrance.entranceYear = year
                                                        entrance.entranceMonth = month

                                                        EntranceModelHandler.add(applicationContext, username, entrance)
                                                    }
                                                }
                                            } else {

                                                if ("Entrance" == targetType) {
                                                    val uniqueId = target.asJsonObject.get("unique_key").asString

                                                    if (PurchasedModelHandler.add(applicationContext, id, username, false, downloaded, false, targetType, uniqueId, created)) {
                                                        val org = target.asJsonObject.get("organization").asJsonObject.get("title").asString
                                                        val type = target.asJsonObject.get("entrance_type").asJsonObject.get("title").asString
                                                        val setName = target.asJsonObject.get("entrance_set").asJsonObject.get("title").asString
                                                        val group = target.asJsonObject.get("entrance_set").asJsonObject.get("group").asJsonObject.get("title").asString
                                                        val setId = target.asJsonObject.get("entrance_set").asJsonObject.get("id").asInt
                                                        val bookletsCount = target.asJsonObject.get("booklets_count").asInt
                                                        val duration = target.asJsonObject.get("duration").asInt
                                                        val year = target.asJsonObject.get("year").asInt
                                                        val month = target.asJsonObject.get("month").asInt

                                                        val extraStr = target.asJsonObject.get("extra_data").asString
                                                        var extraData: JsonElement? = null
                                                        if (extraStr != null && "" != extraStr) {
                                                            try {
                                                                extraData = JsonParser().parse(extraStr)
                                                            } catch (exc: Exception) {
                                                                extraData = JsonParser().parse("[]")
                                                            }

                                                        }

                                                        val lastPublishedStr = target.asJsonObject.get("last_published").asString
                                                        val lastPublished = FormatterSingleton.getInstance().UTCDateFormatter.parse(lastPublishedStr)

                                                        if (EntranceModelHandler.getByUsernameAndId(applicationContext, username, uniqueId) == null) {
                                                            val entrance = EntranceStruct()
                                                            entrance.entranceSetId = setId
                                                            entrance.entranceSetTitle = setName
                                                            entrance.entranceOrgTitle = org
                                                            entrance.entranceLastPublished = lastPublished
                                                            entrance.entranceBookletCounts = bookletsCount
                                                            entrance.entranceDuration = duration
                                                            entrance.entranceExtraData = extraData
                                                            entrance.entranceGroupTitle = group
                                                            entrance.entranceTypeTitle = type
                                                            entrance.entranceUniqueId = uniqueId
                                                            entrance.entranceYear = year
                                                            entrance.entranceMonth = month

                                                            EntranceModelHandler.add(applicationContext, username, entrance)
                                                        }
                                                    }
                                                }
                                            }

                                            purchasedId.add(id)
                                        }

                                        val dat = ArrayList<Int>()
                                        for (i in purchasedId.indices) {
                                            dat.add(purchasedId[i])
                                        }

                                        val deletedItems = PurchasedModelHandler.getAllPurchasedNotIn(applicationContext, username, dat.toTypedArray())
                                        if (deletedItems!!.size > 0) {
                                            for (pm in deletedItems) {
                                                this@SynchronizerService.deletePurchaseData(pm.productUniqueId, username)

                                                if ("Entrance" == pm.productType) {
                                                    if (EntranceModelHandler.removeById(applicationContext, username, pm.productUniqueId)) {
                                                        EntranceOpenedCountModelHandler.removeByEntranceId(applicationContext, username, pm.productUniqueId)
                                                        EntranceQuestionStarredModelHandler.removeByEntranceId(applicationContext, username, pm.productUniqueId)
                                                        PurchasedModelHandler.removeById(applicationContext, username, pm.id)
                                                    }
                                                }
                                            }
                                        }

                                        this@SynchronizerService.downloadEsetImages(dat.toTypedArray())

                                        if ((application as MainApplication).getActiveActivity() != null) {
                                            if ((application as MainApplication).getActiveActivity() is FavoritesActivity) {
                                                ((application as MainApplication).getActiveActivity() as FavoritesActivity).reloadData()
                                            }
                                        }

                                    }
                                } catch (exc: Exception) {
                                    //Log.d(TAG, exc.getLocalizedMessage());
                                }

                                "Error" -> {
                                    val errorType = jsonElement.asJsonObject.get("error_type").asString
                                    when (errorType) {
                                        "EmptyArray" -> {
                                            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                            if (username != null) {
                                                val items = PurchasedModelHandler.getAllPurchased(applicationContext, username)

                                                for (pm in items!!) {
                                                    this@SynchronizerService.deletePurchaseData(pm.productUniqueId, username)

                                                    if ("Entrance" == pm.productType) {
                                                        if (EntranceModelHandler.removeById(applicationContext, username, pm.productUniqueId)) {
                                                            EntranceOpenedCountModelHandler.removeByEntranceId(applicationContext, username, pm.productUniqueId)
                                                            EntranceQuestionStarredModelHandler.removeByEntranceId(applicationContext, username, pm.productUniqueId)
                                                            PurchasedModelHandler.removeById(applicationContext, username, pm.id)
                                                        }
                                                    }
                                                }

                                                if ((application as MainApplication).getActiveActivity() != null) {
                                                    if ((application as MainApplication).getActiveActivity() is FavoritesActivity) {
                                                        ((application as MainApplication).getActiveActivity() as FavoritesActivity).reloadData()
                                                    }
                                                }

                                            }
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }) {
                uiThread {
                   if (this@SynchronizerService.retryCounterMap["FAVOURITES"]!! < CONNECTION_MAX_RETRY) {
                       this@SynchronizerService.retryCounterMap["FAVOURITES"] = this@SynchronizerService.retryCounterMap["FAVOURITES"]!! + 1

                       val msg1 = this@SynchronizerService.queue.obtainMessage(msgIndex)
                       this@SynchronizerService.queue.sendMessage(msg1)
                   } else {
                       this@SynchronizerService.retryCounterMap["FAVOURITES"] = 0
                   }
                }
            }
        }
    }


    private fun createWallet(message: Message?) {
        doAsync {
            WalletRestAPIClass.info(applicationContext, { jsonObject, httpErrorType ->
                uiThread {
                    if (httpErrorType === HTTPErrorType.Success) {
                        this@SynchronizerService.retryCounterMap["WALLET"] = 0

                        jsonObject?.let {
                            val status = jsonObject.get("status").asString
                            when (status) {
                                "OK" -> {
                                    val walletRecord = jsonObject.getAsJsonObject("record")
                                    val cash = walletRecord.get("cash").asInt
                                    val updatedStr = walletRecord.get("updated").asString

                                    var myCash = 0
                                    if (UserDefaultsSingleton.getInstance(applicationContext).hasWallet()) {
                                        myCash = UserDefaultsSingleton.getInstance(applicationContext).getWalletInfo()!!.cash
                                    }

                                    UserDefaultsSingleton.getInstance(applicationContext).setWalletInfo(
                                            cash, updatedStr)

                                    if (myCash != cash) {
                                        if ((application as MainApplication).getActiveActivity() != null) {
                                            if ((application as MainApplication).getActiveActivity() is SettingActivity) {
                                                ((application as MainApplication).getActiveActivity() as SettingActivity).reloadForSync()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (httpErrorType === HTTPErrorType.Refresh) {
                            val msg1 = this@SynchronizerService.queue.obtainMessage(SYNC_LIST.indexOf("WALLET"))
                            this@SynchronizerService.queue.sendMessage(msg1)
                        } else {
                            if (this@SynchronizerService.retryCounterMap["WALLET"]!! < CONNECTION_MAX_RETRY) {
                                this@SynchronizerService.retryCounterMap["WALLET"] = this@SynchronizerService.retryCounterMap["WALLET"]!! + 1

                                val msg1 = this@SynchronizerService.queue.obtainMessage(SYNC_LIST.indexOf("WALLET"))
                                this@SynchronizerService.queue.sendMessage(msg1)
                            } else {
                                this@SynchronizerService.retryCounterMap["WALLET"] = 0
                            }
                        }

                    }
                }
            }) {
                uiThread {
                    if (this@SynchronizerService.retryCounterMap["WALLET"]!! < CONNECTION_MAX_RETRY) {
                        this@SynchronizerService.retryCounterMap["WALLET"] = this@SynchronizerService.retryCounterMap["WALLET"]!! + 1

                        val msg1 = this@SynchronizerService.queue.obtainMessage(SYNC_LIST.indexOf("WALLET"))
                        this@SynchronizerService.queue.sendMessage(msg1)
                    } else {
                        this@SynchronizerService.retryCounterMap["WALLET"] = 0
                    }
                }
            }
        }
    }

    private fun checkDeviceStateWithServer(message: Message?) {
        doAsync {
            DeviceRestAPIClass.deviceState(applicationContext, { data, error ->
                uiThread {
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            val msg = this@SynchronizerService.queue.obtainMessage(SYNC_LIST.indexOf("LOCK"))
                            this@SynchronizerService.queue.sendMessage(msg)
                        } else {
                            if (this@SynchronizerService.retryCounterMap["LOCK"]!! < CONNECTION_MAX_RETRY) {
                                this@SynchronizerService.retryCounterMap["LOCK"] = this@SynchronizerService.retryCounterMap["LOCK"]!! + 1

                                val msg = this@SynchronizerService.queue.obtainMessage(SYNC_LIST.indexOf("LOCK"))
                                this@SynchronizerService.queue.sendMessage(msg)
                            } else {
                                this@SynchronizerService.retryCounterMap["LOCK"] = 0
                            }
                        }
                    } else {
                        this@SynchronizerService.retryCounterMap["LOCK"] = 0
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val state = data.get("data").asJsonObject.get("state").asBoolean
                                        val device_unique_id = data.get("data").asJsonObject.get("device_unique_id").asString

                                        val androidId = Settings.Secure.getString(applicationContext.contentResolver,
                                                Settings.Secure.ANDROID_ID)
                                        if (device_unique_id == androidId) {
                                            if (!state) {
                                                val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
                                                DeviceInformationSingleton.getInstance(applicationContext).setDeviceState(username,
                                                        "android", Build.MANUFACTURER + " " + Build.MODEL,
                                                        false, true)
                                                this@SynchronizerService.setupLocked()
                                            }
                                        }

                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "AnotherDevice" -> {
                                                val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                                if (username != null) {
                                                    if ((application as MainApplication).getActiveActivity() != null) {
                                                        AlertClass.showAlertMessage(this@SynchronizerService, "DeviceInfoError", errorType, "error", {
                                                            val device_name = data.get("error_data").asJsonObject.get("device_name").asString
                                                            val device_model = data.get("error_data").asJsonObject.get("device_model").asString

                                                            if (DeviceInformationSingleton.getInstance(applicationContext).setDeviceState(username, device_name, device_model, false, false)) {
                                                            }
                                                            this@SynchronizerService.setupLocked()
                                                        })
                                                    }
                                                }

                                            }
                                            "UserNotExist", "DeviceNotRegistered" -> {
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
                }

            }, {error ->
                uiThread {
                    if (this@SynchronizerService.retryCounterMap["LOCK"]!! < CONNECTION_MAX_RETRY) {
                        this@SynchronizerService.retryCounterMap["LOCK"] = this@SynchronizerService.retryCounterMap["LOCK"]!! + 1

                        val msg = this@SynchronizerService.queue.obtainMessage(SYNC_LIST.indexOf("LOCK"))
                        this@SynchronizerService.queue.sendMessage(msg)
                    } else {
                        this@SynchronizerService.retryCounterMap["LOCK"] = 0

                        if (TokenHandlerSingleton.getInstance(applicationContext).isAuthenticated() &&
                                TokenHandlerSingleton.getInstance(applicationContext).isAuthorized()) {
                            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
                            val device = DeviceInformationModelHandler.findByUniqueId(applicationContext, username)
                            if (device != null) {
                                if (device.state  == false) {
                                    this@SynchronizerService.setupLocked()
                                }
                            } else {
                                this@SynchronizerService.setupLocked()
                            }
                        }
                    }
                }
            })
        }
    }

    private fun setupLocked() {
        this.stopSync()
        val intent = StartupActivity.newIntent(this)
        this.startActivity(intent)

        if ((application as MainApplication).getActiveActivity() != null) {
            (application as MainApplication).getActiveActivity()!!.finish()
        }

    }

    private fun deletePurchaseData(path: String, username: String) {
        val finalPath = username + "_" + path

        var f = File(applicationContext.filesDir, finalPath)
        if (!(f.exists() && f.isDirectory)) {
            f = File(applicationContext.filesDir, path)
        }

        if (f.exists() && f.isDirectory) {
            for (fc in f.listFiles()!!) {
                fc.delete()
            }
            val rd = f.delete()
        }
    }

    private fun downloadEsetImages(ids: Array<Int>) {
        val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
        val purchasedIn = PurchasedModelHandler.getAllPurchasedIn(applicationContext, username, ids)
        if (purchasedIn != null) {
            for (purchasedModel in purchasedIn) {
                if (purchasedModel.productType == "Entrance") {
                    val entranceModel = EntranceModelHandler.getByUsernameAndId(applicationContext, username, purchasedModel.productUniqueId)
                    if (entranceModel != null) {
                        downloadImage(entranceModel.setId)
                    }
                }
            }
        }
    }

    private fun downloadImage(imageId: Int) {
        val url = MediaRestAPIClass.makeEsetImageUrl(imageId)

        if (url != null) {
            val data = MediaCacheSingleton.getInstance(applicationContext)[url]
            if (data != null) {
                saveToFile(data, imageId)
            } else {
                MediaRestAPIClass.downloadEsetImage(this@SynchronizerService, imageId, { data1, httpErrorType ->
                    runOnUiThread {
                        if (httpErrorType !== HTTPErrorType.Success) {
                            if (httpErrorType === HTTPErrorType.Refresh) {
                                downloadImage(imageId)
                            }
                        } else {
                            MediaCacheSingleton.getInstance(applicationContext)[url] = data1!!
                            saveToFile(data1, imageId)
                        }
                    }
                }) { }
            }
        }

    }

    private fun saveToFile(data: ByteArray, imageId: Int) {
        val folder = File(applicationContext.filesDir, "images")
        val folder2 = File(applicationContext.filesDir.toString() + "/images", "eset")
        if (!folder.exists()) {
            folder.mkdir()
            folder2.mkdir()
        }

        val photo = File(applicationContext.filesDir.toString() + "/images/eset", imageId.toString())
        if (photo.exists()) {
            photo.delete()
        }

        try {
            val fos = FileOutputStream(photo.path)

            fos.write(data)
            fos.close()
        } catch (e: java.io.IOException) {
            Log.e("PictureDemo", "Exception in photoCallback", e)
        }

    }
}