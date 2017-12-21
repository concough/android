package com.concough.android.concough

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.concough.android.general.AlertClass
import com.concough.android.models.*
import com.concough.android.rest.*
import com.concough.android.settings.APP_VERSION
import com.concough.android.singletons.*
import com.concough.android.structures.EntranceStruct
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.utils.NetworkUtil
import com.concough.android.vendor.progressHUD.KProgressHUD
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_startup.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList


class StartupActivity : AppCompatActivity() {
    private var broadcastReceiver: NetworkChangeReceiver? = null
    private var isOnline: Boolean = true

    private var loadingProgress: KProgressHUD? = null

    companion object {
        private val TAG = "StartupActivity"
        private val IS_STARTUP_KEY = "IS_STARTUP"
        private val SPLASH_DISPLAY_LENGTH = 5000

        @JvmStatic
        fun newIntent(packageContext: Context): Intent {
            val i = Intent(packageContext, StartupActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            i.putExtra(IS_STARTUP_KEY, false)
            return i
        }

    }

    inner class NetworkChangeReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val status = NetworkUtil.getConnectivityStatusString(context)
            if ("android.net.conn.CONNECTIVITY_CHANGE" == intent.action || "android.net.conn.WIFI_STATE_CHANGED" == intent.action) {
                if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    //Log.d(TAG, "Internet Not Connected")
                } else {
                    //Log.d(TAG, "Internet Connected")
                    if (this@StartupActivity.broadcastReceiver != null)
                        this@StartupActivity.unregisterReceiver(this@StartupActivity.broadcastReceiver)

                    if (TokenHandlerSingleton.getInstance(applicationContext).isAuthorized() && TokenHandlerSingleton.getInstance(applicationContext).isAuthenticated()) {
                        val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                        val device = DeviceInformationModelHandler.findByUniqueId(applicationContext, username!!)
                        if (device != null) {
                            if (device.state) {
                                this@StartupActivity.isOnline = true
                                this@StartupActivity.navigateToHome()
                            }
                        }
                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        deviceAlert.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Light
        deviceName.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold
        ExitFromLockModeButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold
        ResetPasswordButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Light

        welcomeMessage.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Regular
        LoginButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold
        SignUpButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold

        offlineMessage.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Light
        offlineButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold

        supportActionBar?.hide()

        val is_startup = intent.getBooleanExtra(IS_STARTUP_KEY, true)
        if (is_startup) {
            StartupA_splash.visibility = View.VISIBLE
            val handler = Handler()
            handler.postDelayed({
                StartupA_splash.visibility = View.GONE

            }, 4000)
        } else {
            StartupA_splash.visibility = View.VISIBLE
            val handler = Handler()
            handler.postDelayed({
                StartupA_splash.visibility = View.GONE

            }, 500)
        }


        LoginButton.setOnClickListener({
            val loginIntent = LoginActivity.newIntent(this@StartupActivity)
            startActivity(loginIntent)
        })

        SignUpButton.setOnClickListener({
            val loginIntent = SignupActivity.newIntent(this@StartupActivity)
            startActivity(loginIntent)
        })


        ExitFromLockModeButton.setOnClickListener({
            this.getLockedStatus()
        })

        ResetPasswordButton.setOnClickListener({
            val intent = ForgotPasswordActivity.newIntent(this@StartupActivity)
            startActivity(intent)
        })

        offlineButton.setOnClickListener({
            this@StartupActivity.isOnline = false
            this@StartupActivity.navigateToHome()
        })


    }

    override fun onResume() {

        super.onResume()
        this.startup()

        introVideoView.init()
        introVideoViewLogin.init()
        introVideoViewOffline.init()

    }

    override fun onPause() {
        super.onPause()
//        introVideoView.pauseMe()
    }

    private fun startup() {
//        if (TokenHandlerSingleton.getInstance(applicationContext).isAuthorized()) {
//            if (UserDefaultsSingleton.getInstance(applicationContext).hasProfile()) {
//
//                this@StartupActivity.loadBasketItems()
//                val homeIntent = HomeActivity.newIntent(this@StartupActivity)
//                startActivity(homeIntent)
//                finish()
//
//            } else {
//                this@StartupActivity.getProfile()
//            }
//        } else
        if (NetworkUtil.getConnectivityStatus(applicationContext) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            this@StartupActivity.isOnline = false
            if (TokenHandlerSingleton.getInstance(applicationContext).isAuthorized() && TokenHandlerSingleton.getInstance(applicationContext).isAuthenticated()) {
                val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                val device = DeviceInformationModelHandler.findByUniqueId(applicationContext, username!!)
                if (device != null) {
                    if (device.state) {
                        this@StartupActivity.setupOffline()
                    } else {
                        this@StartupActivity.setupLocked()
                    }
                } else {
                    this@StartupActivity.setupLocked()
                }
            } else {
                this@StartupActivity.setupUnauthenticated()
            }
        } else {

            if (TokenHandlerSingleton.getInstance(applicationContext).isAuthenticated()) {
                doAsync {
                    TokenHandlerSingleton.getInstance(applicationContext).assureAuthorized(true, { authenticated, error ->
                        uiThread {
                            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
                            val device = DeviceInformationModelHandler.findByUniqueId(applicationContext, username)
                            if (device != null) {
                                if (device.state) {
                                    if (authenticated) {
                                        if (UserDefaultsSingleton.getInstance(applicationContext).hasProfile()) {
                                            this@StartupActivity.checkVersion()
                                        } else {
                                            this@StartupActivity.getProfile()
                                        }
                                    } else {
                                        this@StartupActivity.setupUnauthenticated()
                                    }
                                } else {
                                    this@StartupActivity.setupLocked()
                                }
                            } else {
                                this@StartupActivity.setupLocked()
                            }
//                                if (UserDefaultsSingleton.getInstance(applicationContext).hasProfile()) {
//
//                                    this@StartupActivity.navigateToHome()
//                                } else {
//                                    this@StartupActivity.getProfile()
//                                }
                        }

                    }, { error ->
                        uiThread {
                            if (TokenHandlerSingleton.getInstance(applicationContext).isAuthenticated() && TokenHandlerSingleton.getInstance(applicationContext).isAuthorized()) {
                                val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
                                val device = DeviceInformationModelHandler.findByUniqueId(applicationContext, username)
                                if (device != null) {
                                    if (device.state) {
                                        this@StartupActivity.isOnline = false
                                        this@StartupActivity.setupOffline()
                                    } else {
                                        this@StartupActivity.setupLocked()
                                    }
                                } else {
                                    this@StartupActivity.setupLocked()
                                }
                            } else {
                                this@StartupActivity.setupUnauthenticated()
                            }
                        }
                    })

                }

            } else {
                this@StartupActivity.setupUnauthenticated()
            }
        }
    }

    private fun checkVersion() {
        doAsync {
            SettingsRestAPIClass.appLastVersion(this@StartupActivity, { data, error ->
                uiThread {
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            this@StartupActivity.checkVersion()
                        } else {
                            this@StartupActivity.navigateToHome()
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
                                            val msg = AlertClass.convertMessage("DeviceAction", "UpdateApp")
                                            val date = FormatterSingleton.getInstance().UTCDateFormatter.parse(released)
                                            val versionInPersian = FormatterSingleton.getInstance().NumberFormatter.format(version)
                                            val dateInPersian = FormatterSingleton.getInstance().getPersianDateString(date)

                                            val message = "نسخه $versionInPersian منتشر شده است\nتاریخ: $dateInPersian"
                                            AlertClass.showSucceessMessageCustom(this@StartupActivity, msg.title, message, "دانلود", "بعدا", completion = {
                                                val intentUri = Uri.parse(link)

                                                val intent = Intent()
                                                intent.action = Intent.ACTION_VIEW
                                                intent.data = intentUri
                                                this@StartupActivity.startActivity(intent)


                                            }, noCompletion = {
                                                this@StartupActivity.navigateToHome()
                                            })
                                        } else {
                                            this@StartupActivity.navigateToHome()
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

                                        this@StartupActivity.navigateToHome()
                                    }
                                }
                            } catch (exc: Exception) {

                            }

                        }
                    }
                }
            }, { error ->
                uiThread {
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                            }
                        }

                    }

                    this@StartupActivity.navigateToHome()
                }
            })

        }
    }

    private fun getLockedStatus() {
        this@StartupActivity.loadingProgress = AlertClass.showLoadingMessage(this@StartupActivity)
        this@StartupActivity.loadingProgress?.show()

        doAsync {
            DeviceRestAPIClass.deviceLock(this@StartupActivity, false, { data, error ->
                uiThread {
                    AlertClass.hideLoadingMessage(this@StartupActivity.loadingProgress)

                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            this@StartupActivity.getLockedStatus()
                        } else {
                            AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                        }
                    } else {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val state = data.get("data").asJsonObject.get("state").asBoolean
                                        val device_unique_id = data.get("data").asJsonObject.get("device_unique_id").asString

                                        val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                        if (username != null) {
                                            val androidId = Settings.Secure.getString(applicationContext.contentResolver,
                                                    Settings.Secure.ANDROID_ID)
                                            val deviceModel = Build.MANUFACTURER + " " + Build.MODEL

                                            if (device_unique_id == androidId) {
                                                if (DeviceInformationSingleton.getInstance(applicationContext).setDeviceState(username, "android", deviceModel, state, true)) {
                                                    if (state) {
                                                        this@StartupActivity.syncWithServer()
                                                        this@StartupActivity.getProfile()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "AnotherDevice" -> {
                                                val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                                if (username != null) {
                                                    AlertClass.showAlertMessage(this@StartupActivity, "DeviceInfoError", errorType, "error", {
                                                        val device_name = data.get("error_data").asJsonObject.get("device_name").asString
                                                        val device_model = data.get("error_data").asJsonObject.get("device_model").asString

                                                        if (DeviceInformationSingleton.getInstance(applicationContext).setDeviceState(username, device_name, device_model, false, false)) {
                                                            deviceName.text = "دستگاه: $device_name $device_model"
                                                        }
                                                    })
                                                }

                                            }
                                            "UserNotExist", "DeviceNotRegistered" -> {
                                                val loginIntent = LoginActivity.newIntent(this@StartupActivity)
                                                startActivity(loginIntent)
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
            }, { error ->
                uiThread {
                    AlertClass.hideLoadingMessage(this@StartupActivity.loadingProgress)
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                            }
                        }

                    }
                }

            })

        }
    }

    private fun getProfile() {
        doAsync {
            ProfileRestAPIClass.getProfileData(this@StartupActivity, { data, error ->
                uiThread {
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            this@StartupActivity.getProfile()
                        } else {
                            this@StartupActivity.setupUnauthenticated()
                        }
                    } else {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val profile = data.get("record").asJsonArray[0].asJsonObject
                                        if (profile != null) {
                                            val gender = profile.get("gender").asString
                                            val grade = profile.get("grade").asString
                                            val gradeString = profile.get("grade_string").asString
                                            val birthday = profile.get("birthday").asString
                                            val modified = profile.get("modified").asString
                                            val firstname = profile.get("user").asJsonObject.get("first_name").asString
                                            val lastname = profile.get("user").asJsonObject.get("last_name").asString


                                            val birthdayDate = FormatterSingleton.getInstance().UTCShortDateFormatter.parse(birthday)
                                            val modifiedDate = FormatterSingleton.getInstance().UTCDateFormatter.parse(modified)

                                            if ("" != firstname && "" != lastname && "" != gender && "" != grade) {
                                                UserDefaultsSingleton.getInstance(applicationContext).createProfile(firstname, lastname, grade, gradeString, gender, birthdayDate, modifiedDate)
                                            }

                                            if (UserDefaultsSingleton.getInstance(applicationContext).hasProfile()) {

                                                this@StartupActivity.checkVersion()

//                                                this@StartupActivity.loadBasketItems()
//                                                val homeIntent = HomeActivity.newIntent(this@StartupActivity)
//                                                startActivity(homeIntent)
//                                                finish()

                                            } else {
                                                // Profile not created
                                                val moreInfoIntent = SignupMoreInfo1Activity.newIntent(this@StartupActivity)
                                                startActivity(moreInfoIntent)
                                                finish()
                                            }

                                        }
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        val moreInfoIntent = SignupMoreInfo1Activity.newIntent(this@StartupActivity)
                                        startActivity(moreInfoIntent)
                                        finish()

                                    }
                                }
                            } catch (exc: Exception) {

                            }

                        }
                    }
                }
            }, { error ->
                uiThread {
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                            }
                        }

                    }
                    this@StartupActivity.setupUnauthenticated()

                }

            })

        }
    }

    private fun syncWithServer() {
        SyncWithServerTask().execute()
    }

    private inner class SyncWithServerTask : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
//            runOnUiThread {
//                loadingProgress = AlertClass.showLoadingMessage(this@StartupActivity)
//                loadingProgress?.show()
//            }

            PurchasedRestAPIClass.getPurchasedList(applicationContext, { jsonElement, httpErrorType ->
                runOnUiThread {
                    //AlertClass.hideLoadingMessage(loadingProgress)

                    if (httpErrorType !== HTTPErrorType.Success) {
                        if (httpErrorType === HTTPErrorType.Refresh) {
                            SyncWithServerTask().execute()
                        } else {
                            AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null)
                        }
                    } else {
                        if (jsonElement != null) {
                            val status = jsonElement!!.asJsonObject.get("status").asString
                            when (status) {
                                "OK" -> try {
                                    val purchasedId = ArrayList<Int>()
                                    val records = jsonElement!!.asJsonObject.get("records").asJsonArray
                                    val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                    if (username != null) {
                                        for (record in records) {
                                            val id = record.asJsonObject.get("id").asInt
                                            val downloaded = record.asJsonObject.get("downloaded").asInt
                                            val createdStr = record.asJsonObject.get("created").asString
                                            val created = FormatterSingleton.getInstance().UTCDateFormatter.parse(createdStr)

                                            val target = record.asJsonObject.get("target")
                                            val targetType = target.asJsonObject.get("product_type").asString

                                            if (PurchasedModelHandler.getByUsernameAndId(applicationContext, username!!, id) != null) {
                                                PurchasedModelHandler.updateDownloadTimes(applicationContext, username!!, id, downloaded)

                                                if ("Entrance" == targetType) {
                                                    val uniqueId = target.asJsonObject.get("unique_key").asString
                                                    if (EntranceModelHandler.getByUsernameAndId(applicationContext, username!!, uniqueId) == null) {
                                                        val org = target.asJsonObject.get("organization").asJsonObject.get("title").asString
                                                        val type = target.asJsonObject.get("entrance_type").asJsonObject.get("title").asString
                                                        val setName = target.asJsonObject.get("entrance_set").asJsonObject.get("title").asString
                                                        val group = target.asJsonObject.get("entrance_set").asJsonObject.get("group").asJsonObject.get("title").asString
                                                        val setId = target.asJsonObject.get("entrance_set").asJsonObject.get("id").asInt
                                                        val bookletsCount = target.asJsonObject.get("booklets_count").asInt
                                                        val duration = target.asJsonObject.get("duration").asInt
                                                        val year = target.asJsonObject.get("year").asInt

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

                                                        EntranceModelHandler.add(applicationContext, username!!, entrance)

                                                    }
                                                }
                                            } else {

                                                if ("Entrance" == targetType) {
                                                    val uniqueId = target.asJsonObject.get("unique_key").asString

                                                    if (PurchasedModelHandler.add(applicationContext, id, username!!, false, downloaded, false, targetType, uniqueId, created)) {
                                                        val org = target.asJsonObject.get("organization").asJsonObject.get("title").asString
                                                        val type = target.asJsonObject.get("entrance_type").asJsonObject.get("title").asString
                                                        val setName = target.asJsonObject.get("entrance_set").asJsonObject.get("title").asString
                                                        val group = target.asJsonObject.get("entrance_set").asJsonObject.get("group").asJsonObject.get("title").asString
                                                        val setId = target.asJsonObject.get("entrance_set").asJsonObject.get("id").asInt
                                                        val bookletsCount = target.asJsonObject.get("booklets_count").asInt
                                                        val duration = target.asJsonObject.get("duration").asInt
                                                        val year = target.asJsonObject.get("year").asInt

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

                                                        if (EntranceModelHandler.getByUsernameAndId(applicationContext, username!!, uniqueId) == null) {
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

                                                            EntranceModelHandler.add(applicationContext, username!!, entrance)
                                                        }
                                                    }
                                                }
                                            }

                                            purchasedId.add(id)
                                        }

                                        val dat = arrayOf(purchasedId.size)
                                        for (i in purchasedId.indices) {
                                            dat[i] = purchasedId[i]
                                        }

                                        val deletedItems = PurchasedModelHandler.getAllPurchasedNotIn(applicationContext, username!!, dat)
                                        if (deletedItems!!.size > 0) {
                                            for (pm in deletedItems!!) {
                                                this@StartupActivity.deletePurchaseData(pm.productUniqueId)

                                                if ("Entrance" == pm.productType) {
                                                    if (EntranceModelHandler.removeById(applicationContext, username!!, pm.productUniqueId)) {
                                                        //EntranceOpenedCountModelHandler.removeByEntranceId(getApplicationContext(), username, pm.productUniqueId);
                                                        EntranceQuestionStarredModelHandler.removeByEntranceId(applicationContext, username!!, pm.productUniqueId)
                                                        PurchasedModelHandler.removeById(applicationContext, username!!, pm.id)
                                                    }
                                                }
                                            }
                                        }

                                        //RealmResults<PurchasedModel> purchasedIn = PurchasedModelHandler.getAllPurchasedIn(BasketCheckoutActivity.this, username, purchasedIds);

                                        purchasedIds(dat)
                                    }
                                } catch (exc: Exception) {
                                    Log.d(TAG, exc.localizedMessage)
                                }

                                "Error" -> {
                                    val errorType = jsonElement!!.asJsonObject.get("error_type").asString
                                    when (errorType) {
                                        "EmptyArray" -> {
                                            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                            if (username != null) {
                                                val items = PurchasedModelHandler.getAllPurchased(applicationContext, username!!)

                                                for (pm in items!!) {
                                                    this@StartupActivity.deletePurchaseData(pm.productUniqueId)

                                                    if ("Entrance" == pm.productType) {
                                                        if (EntranceModelHandler.removeById(applicationContext, username!!, pm.productUniqueId)) {
                                                            EntranceOpenedCountModelHandler.removeByEntranceId(applicationContext, username!!, pm.productUniqueId)
                                                            EntranceQuestionStarredModelHandler.removeByEntranceId(applicationContext, username!!, pm.productUniqueId)
                                                            PurchasedModelHandler.removeById(applicationContext, username!!, pm.id)
                                                        }
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
                return@getPurchasedList Unit
            }, fun(networkErrorType: NetworkErrorType?): Unit {
                runOnUiThread {
                    //AlertClass.hideLoadingMessage(loadingProgress)
                    if (networkErrorType != null) {
                        when (networkErrorType) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", networkErrorType!!.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", networkErrorType!!.name, "", null)
                            }
                        }
                    }
                }
                return Unit
            })

            return null
        }
    }


    private fun deletePurchaseData(uniqueId: String) {
        val f = File(this@StartupActivity.getFilesDir(), uniqueId)
        if (f.exists() && f.isDirectory()) {
            //                                String[] children = f.list();
            for (fc in f.listFiles()) {
                fc.delete()
            }
            val rd = f.delete()
        }

    }

    private fun purchasedIds(ids: Array<Int>) {
        val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()

        val purchasedIn = PurchasedModelHandler.getAllPurchasedIn(applicationContext, username!!, ids)
        if (purchasedIn != null) {
            for (purchasedModel in purchasedIn!!) {
                if (purchasedModel.productType == "Entrance") {
                    val entranceModel = EntranceModelHandler.getByUsernameAndId(applicationContext, username!!, purchasedModel.productUniqueId)
                    if (entranceModel != null) {
                        downloadImage(entranceModel!!.setId)
                    }
                }
            }
        }
    }


    private fun downloadImage(imageId: Int) {
        val url = MediaRestAPIClass.makeEsetImageUrl(imageId)

        if (url != null) {
            val data = MediaCacheSingleton.getInstance(applicationContext)[url!!]
            if (data != null) {

                val folder = File(applicationContext.filesDir, "images")
                val folder2 = File((getApplicationContext().getFilesDir()).toString() + "/images", "eset")
                if (!folder.exists()) {
                    folder.mkdir()
                    folder2.mkdir()
                }

                val photo = File((getApplicationContext().getFilesDir()).toString() + "/images/eset", (imageId).toString())
                if (photo.exists()) {
                    photo.delete()
                }

                try {
                    val fos = FileOutputStream(photo.getPath())

                    fos.write(data!!)
                    fos.close()
                } catch (e: java.io.IOException) {
                    Log.e("PictureDemo", "Exception in photoCallback", e)
                }

            }
        }

    }

    private fun navigateToHome() {
        if (isOnline) {
            this@StartupActivity.loadBasketItems()
            val homeIntent = HomeActivity.newIntent(this@StartupActivity)
            startActivity(homeIntent)
            finish()
        } else {
            val intent = FavoritesActivity.newIntent(this@StartupActivity)
            startActivity(intent)
            finish()
        }
    }

    private fun loadBasketItems() {
        BasketSingleton.getInstance().loadBasketItems(this@StartupActivity)
    }

    private fun setupUnauthenticated() {
        //StartupA_splash.visibility = View.GONE
        StartupA_introLogin.visibility = View.VISIBLE
    }

    private fun setupLocked() {
        //StartupA_splash.visibility = View.GONE
        StartupA_intro.visibility = View.VISIBLE

        val username: String? = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
        if (username != null) {
            val device: DeviceInformationModel? = DeviceInformationModelHandler.findByUniqueId(applicationContext, username)
            if (device != null) {
                if (device.is_me) {
                    deviceName.text = "دستگاه فعلی"
                } else {
                    deviceName.text = "دستگاه: ${device.device_name} ${device.device_model}"
                }
            } else {
                // TODO: check it
                Log.d(TAG, "Unauthenticated device")
            }
        }
    }

    private fun setupOffline() {
        //StartupA_splash.visibility = View.GONE
        StartupA_offline.visibility = View.VISIBLE

        val i = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        i.addAction("android.net.wifi.WIFI_STATE_CHANGED")

        this.broadcastReceiver = NetworkChangeReceiver()
        this.registerReceiver(this.broadcastReceiver, i)
    }
}