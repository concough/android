package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ContentLoadingProgressBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.concough.android.concough.dialogs.EntranceBuyDialog
import com.concough.android.concough.interfaces.ProductBuyDelegate
import com.concough.android.general.AlertClass
import com.concough.android.models.EntranceModelHandler
import com.concough.android.models.PurchasedModelHandler
import com.concough.android.rest.MediaRestAPIClass
import com.concough.android.rest.ProductRestAPIClass
import com.concough.android.rest.WalletRestAPIClass
import com.concough.android.settings.CONNECTION_MAX_RETRY
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.MediaCacheSingleton
import com.concough.android.singletons.UserDefaultsSingleton
import com.concough.android.structures.*
import com.concough.android.utils.monthToString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_entrance_multi_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EntranceMultiDetailActivity : BottomNavigationActivity(), ProductBuyDelegate {
    data class InternalEntranceData(var uniqueId: String, var entrance: EntranceStruct, var buyedCount: Int)

    private var uniqueId: String? = null
    private var activityType: String? = null
    private var target: JsonObject? = null
    private var isPackageBuyed: Boolean = true

    private var retryCounter: Int = 0
    private var selectedActivityIndex: Int = -1
    private var entranceMultiSale: EntranceMultiSaleStructure? = null
    private var entrances: ArrayList<InternalEntranceData> = ArrayList()

    private lateinit var entranceMultiDetailAdapter: EntranceMultiDetailAdapter

    companion object {
        private const val TAG = "EntranceMultiDetailActivity"
        private const val UNIQUE_ID_KEY = "UNIQUE_ID"
        private const val ACTIVITY_TYPE_KEY = "ACTIVITY_TYPE"
        private const val TARGET_KEY = "TARGET"

        @JvmStatic
        fun newIntent(packageContext: Context, uniqueId: String, actType: String, target: String): Intent {
            val i = Intent(packageContext, EntranceMultiDetailActivity::class.java)
            i.putExtra(UNIQUE_ID_KEY, uniqueId)
            i.putExtra(ACTIVITY_TYPE_KEY, actType)
            i.putExtra(TARGET_KEY, target)
            return i
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_entrance_multi_detail
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.setMenuSelectedIndex(0)
        super.onCreate(savedInstanceState)

        this.uniqueId = intent.getStringExtra(UNIQUE_ID_KEY)
        this.activityType = intent.getStringExtra(ACTIVITY_TYPE_KEY)
        val cas = intent.getStringExtra(TARGET_KEY)

        val parser = JsonParser()
        this.target = parser.parse(cas).asJsonObject

        entranceMultiDetailA_recycle.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false)

        this.entranceMultiDetailAdapter = EntranceMultiDetailAdapter(this,
                this.entranceMultiSale, this.entrances)
        entranceMultiDetailA_recycle.adapter = this.entranceMultiDetailAdapter
    }

    override fun onResume() {
        super.onResume()
        actionBarSet()

        this.entranceMultiSale = null
        this.entrances.clear()
        this.entranceMultiDetailAdapter.setItems(this.entranceMultiSale, this.entrances, this.isPackageBuyed)
        this.entranceMultiDetailAdapter.notifyDataSetChanged()

        if (this.target!!.has("entrances")) {
            val entranceArray = this.target!!.get("entrances").asJsonArray

            this.isPackageBuyed = true
            val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()!!
            for (ent in entranceArray) {
                val entranceUniqueId = ent.asJsonObject.get("unique_key").asString
                val isPurchased = PurchasedModelHandler.getByProductId(applicationContext, username,
                        "Entrance", entranceUniqueId)
                if (isPurchased == null) {
                    this.isPackageBuyed = false
                }
            }

//            if (this.isPackageBuyed) {
//                val h = entranceMultiDetailA_recycle.findViewHolderForAdapterPosition(0)
//                if (h is EntranceMultiDetailAdapter.EMDInitialHolder) {
//                    h.configureCosts(this@EntranceMultiDetailActivity,
//                            this@EntranceMultiDetailActivity.entranceMultiSale!!)
//                    h.disableBuyGreen(this@EntranceMultiDetailActivity)
//                }
//
//            }

            this.getEntranceMultiSaleData()

            for (ent in entranceArray) {
                val organization = ent.asJsonObject.get("organization").asJsonObject.get("title").asString
                val entranceType = ent.asJsonObject.get("entrance_type").asJsonObject.get("title").asString
                val entranceSet = ent.asJsonObject.get("entrance_set").asJsonObject.get("title").asString
                val entranceSetId = ent.asJsonObject.get("entrance_set").asJsonObject.get("id").asInt
                val entranceGroup = ent.asJsonObject.get("entrance_set").asJsonObject.get("group").asJsonObject.get("title").asString

                val extraStr = ent.asJsonObject.get("extra_data").asString
                var extraData: JsonElement? = null
                if (extraStr != null && "" != extraStr) {
                    extraData = try {
                        JsonParser().parse(extraStr)
                    } catch (exc: Exception) {
                        JsonParser().parse("[]")
                    }
                }

//                val bookletCount = ent.asJsonObject.get("booklets_count").asInt
//                val duration = ent.asJsonObject.get("duration").asInt

                val bookletCount = 0
                val duration = 0
                val year = ent.asJsonObject.get("year").asInt
                val month = ent.asJsonObject.get("month").asInt
                val lastPublishedStr = ent.asJsonObject.get("last_published").asString
                val entranceUniqueId = ent.asJsonObject.get("unique_key").asString

                val lastPublished = FormatterSingleton.getInstance().UTCDateFormatter.parse(lastPublishedStr)

                val entranceS = EntranceStruct()
                entranceS.entranceMonth = month
                entranceS.entranceUniqueId = entranceUniqueId
                entranceS.entranceBookletCounts = bookletCount
                entranceS.entranceDuration = duration
                entranceS.entranceExtraData = extraData
                entranceS.entranceGroupTitle = entranceGroup
                entranceS.entranceLastPublished = lastPublished
                entranceS.entranceOrgTitle = organization
                entranceS.entranceSetId = entranceSetId
                entranceS.entranceSetTitle = entranceSet
                entranceS.entranceTypeTitle = entranceType
                entranceS.entranceYear = year

                val buyedCount = ent.asJsonObject.get("stats").asJsonArray.get(0).asJsonObject.get("purchased").asInt
                this.entrances.add(InternalEntranceData(entranceUniqueId,
                        entranceS, buyedCount))
            }

            this.entranceMultiDetailAdapter.setItems(this.entranceMultiSale, this.entrances, this.isPackageBuyed)
            this.entranceMultiDetailAdapter.notifyDataSetChanged()
        }
    }

    private fun actionBarSet() {

        val buttonDetailArrayList = ArrayList<TopNavigationActivity.ButtonDetail>()

        super.clickEventInterface = object : TopNavigationActivity.OnClickEventInterface {
            override fun OnButtonClicked(id: Int) {
            }

            override fun OnBackClicked() {
                try {
                    onBackPressed()
                } catch (e: Exception) {
                    finish()
                }

            }

            override fun OnTitleClicked() {
//                if (this@EntranceMultiDetailActivity.recycleView != null) {
//                    this@EntranceMultiDetailActivity.recycleView.smoothScrollToPosition(0)
//                }
            }
        }

        super.createActionBar("اطلاعات بسته آزمون", true, buttonDetailArrayList)
    }

    private fun getEntranceMultiSaleData() {
        doAsync {
            ProductRestAPIClass.getEntranceMultiSaleData(this@EntranceMultiDetailActivity,
                    this@EntranceMultiDetailActivity.uniqueId!!, completion = { jsonElement, httpErrorType ->
                uiThread {
                    if (httpErrorType != HTTPErrorType.Success) {
                        if (httpErrorType == HTTPErrorType.Refresh) {
                            this@EntranceMultiDetailActivity.getEntranceMultiSaleData()
                        } else {
                            if (this@EntranceMultiDetailActivity.retryCounter < CONNECTION_MAX_RETRY) {
                                this@EntranceMultiDetailActivity.retryCounter += 1
                                this@EntranceMultiDetailActivity.getEntranceMultiSaleData()
                            } else {
                                this@EntranceMultiDetailActivity.retryCounter = 0
                                AlertClass.showTopMessage(this@EntranceMultiDetailActivity, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null)
                            }
                        }
                    } else {
                        this@EntranceMultiDetailActivity.retryCounter = 0

                        if (jsonElement != null) {
                            val status = jsonElement.asJsonObject.get("status").asString
                            when (status) {
                                "OK" -> try {
                                    val sale = jsonElement.asJsonObject.get("sale_data")
                                    val discount = sale.asJsonObject.get("discount").asInt
                                    val totalCost = sale.asJsonObject.get("sale_record").asJsonObject.get("total_cost").asInt
                                    val cost = sale.asJsonObject.get("sale_record").asJsonObject.get("cost").asInt

                                    this@EntranceMultiDetailActivity.entranceMultiSale = EntranceMultiSaleStructure(discount,
                                            totalCost, cost)

                                    this@EntranceMultiDetailActivity.entranceMultiDetailAdapter.setItems(this@EntranceMultiDetailActivity.entranceMultiSale,
                                            this@EntranceMultiDetailActivity.entrances, this@EntranceMultiDetailActivity.isPackageBuyed)

                                    val h = entranceMultiDetailA_recycle.findViewHolderForAdapterPosition(0)
                                    if (h is EntranceMultiDetailAdapter.EMDInitialHolder) {
                                        h.configureCosts(this@EntranceMultiDetailActivity,
                                                this@EntranceMultiDetailActivity.entranceMultiSale!!)
                                        if (this@EntranceMultiDetailActivity.isPackageBuyed) {
                                            h.disableBuyGreen(this@EntranceMultiDetailActivity)
                                        } else {
                                            h.disableBuy(this@EntranceMultiDetailActivity,
                                                    false)
                                        }
                                    }
                                    //this@EntranceMultiDetailActivity.entranceMultiDetailAdapter.notifyItemChanged(0)

                                } catch (exc: Exception) {
                                }
                                "Error" -> {
                                    val errorType = jsonElement!!.asJsonObject.get("error_type").asString
                                    when (errorType) {
                                        "EmptyArray" -> { }
                                        "EntranceNotExist" -> {
                                            AlertClass.showAlertMessage(this@EntranceMultiDetailActivity,
                                                    "ErrorResult",
                                                    "NotExist",
                                                    "error") {
                                                this@EntranceMultiDetailActivity.finish()
                                            }
                                        }
                                        else -> {
                                            AlertClass.showAlertMessage(this@EntranceMultiDetailActivity,
                                                    "ErrorResult",
                                                    errorType, "", null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, failure = { networkErrorType ->
                runOnUiThread {
                    //AlertClass.hideLoadingMessage(loadingProgress)
                    if (this@EntranceMultiDetailActivity.retryCounter < CONNECTION_MAX_RETRY) {
                        this@EntranceMultiDetailActivity.retryCounter += 1
                        this@EntranceMultiDetailActivity.getEntranceMultiSaleData()
                    } else {
                        this@EntranceMultiDetailActivity.retryCounter = 0
                        if (networkErrorType != null) {
                            when (networkErrorType) {
                                NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                    AlertClass.showTopMessage(this@EntranceMultiDetailActivity, findViewById(R.id.container), "NetworkError", networkErrorType!!.name, "error", null)
                                }
                                else -> {
                                    AlertClass.showTopMessage(this@EntranceMultiDetailActivity, findViewById(R.id.container), "NetworkError", networkErrorType!!.name, "", null)
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun createWallet() {
        doAsync {
            WalletRestAPIClass.info(this@EntranceMultiDetailActivity, completion = { jsonObject, httpErrorType ->
                uiThread {
                    if (httpErrorType != HTTPErrorType.Success) {
                        if (httpErrorType == HTTPErrorType.Refresh) {
                            this@EntranceMultiDetailActivity.createWallet()
                        } else {
                            if (this@EntranceMultiDetailActivity.retryCounter < CONNECTION_MAX_RETRY) {
                                this@EntranceMultiDetailActivity.retryCounter += 1
                                this@EntranceMultiDetailActivity.createWallet()
                            } else {
                                this@EntranceMultiDetailActivity.retryCounter = 0
                                AlertClass.showTopMessage(this@EntranceMultiDetailActivity, findViewById(R.id.container), "HTTPError", httpErrorType.toString(), "error", null)

                                val holder = entranceMultiDetailA_recycle.findViewHolderForAdapterPosition(0)
                                if (holder is EntranceMultiDetailAdapter.EMDInitialHolder) {
                                    (holder as EntranceMultiDetailAdapter.EMDInitialHolder).disableBuy(this@EntranceMultiDetailActivity, false)
                                }
                            }
                        }
                    } else {
                        this@EntranceMultiDetailActivity.retryCounter = 0
                        val holder = entranceMultiDetailA_recycle.findViewHolderForAdapterPosition(0)
                        if (holder is EntranceMultiDetailAdapter.EMDInitialHolder) {
                            (holder as EntranceMultiDetailAdapter.EMDInitialHolder).disableBuy(this@EntranceMultiDetailActivity, false)
                        }

                        if (jsonObject != null) {
                            val status = jsonObject.get("status").asString
                            when (status) {
                                "OK" -> {
                                    val walletRecord = jsonObject.get("record")
                                    val cash = walletRecord.asJsonObject.get("cash").asInt
                                    val updatedStr = walletRecord.asJsonObject.get("updated").asString

                                    UserDefaultsSingleton.getInstance(this@EntranceMultiDetailActivity).setWalletInfo(cash, updatedStr)

                                    if (UserDefaultsSingleton.getInstance(this@EntranceMultiDetailActivity).hasWallet()) {
                                        this@EntranceMultiDetailActivity.entranceMultiDetailAdapter.notifyItemChanged(0)
                                        this@EntranceMultiDetailActivity.entranceMultiDetailAdapter.showBuyDialog()
                                    }
                                }
                                "Error" -> {
                                    val errorType = jsonObject.get("error_type").asString
                                    when (errorType) {
                                        "EmptyArray" -> { }
                                        else -> {
                                            AlertClass.showTopMessage(this@EntranceMultiDetailActivity,
                                                    findViewById(R.id.container),
                                                    "ErrorResult",
                                                    errorType,
                                                    "",
                                                    null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, failure = {networkErrorType ->
                if (this@EntranceMultiDetailActivity.retryCounter < CONNECTION_MAX_RETRY) {
                    this@EntranceMultiDetailActivity.retryCounter += 1
                    this@EntranceMultiDetailActivity.createWallet()
                } else {
                    this@EntranceMultiDetailActivity.retryCounter = 0
                    val holder = entranceMultiDetailA_recycle.findViewHolderForAdapterPosition(0)
                    if (holder is EntranceMultiDetailAdapter.EMDInitialHolder) {
                        (holder as EntranceMultiDetailAdapter.EMDInitialHolder).disableBuy(this@EntranceMultiDetailActivity, false)
                    }

                    if (networkErrorType != null) {
                        when (networkErrorType) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@EntranceMultiDetailActivity, findViewById(R.id.container), "NetworkError", networkErrorType!!.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@EntranceMultiDetailActivity, findViewById(R.id.container), "NetworkError", networkErrorType!!.name, "", null)
                            }
                        }
                    }
                }

            })
        }
    }

    override fun productBuyResult(data: JsonObject, productId: String, productType: String) {
        try {
            val cash = data.get("wallet_cash").asInt
            val updated = data.get("wallet_updated").asString

            UserDefaultsSingleton.getInstance(this.applicationContext).setWalletInfo(cash, updated)

            val purchasedTemp = ArrayList<Int>()
            val username = UserDefaultsSingleton.getInstance(this.applicationContext).getUsername()!!

            var purchased: JsonArray? = null
            if (data.has("purchased")) {
                if (data.get("purchased").isJsonArray) {
                    purchased = data.get("purchased").asJsonArray
                }
            }

            if (purchased != null) {
                for (element in purchased) {
                    val purchaseId = element.asJsonObject.get("purchase_id").asInt
                    val downloaded = element.asJsonObject.get("downloaded").asInt
                    val productId2 = element.asJsonObject.get("product_key").asString

                    val purchasedTimeStr = element.asJsonObject.get("purchase_time").asString
                    var purchasedTime = Date()
                    try {
                        purchasedTime = FormatterSingleton.getInstance().UTCDateFormatter.parse(purchasedTimeStr)
                    } catch (exc: Exception) {
                    }

                    val entranceData = this.entrances.find {
                        if (it.entrance.entranceUniqueId == productId2) return@find true
                        return@find false

                    }

                    entranceData?.let {
                        if (EntranceModelHandler.add(this.applicationContext, username, entranceData.entrance)) {
                            if (PurchasedModelHandler.add(this.applicationContext, purchaseId,
                                            username, false, downloaded, false,
                                            "Entrance", entranceData.entrance.entranceUniqueId!!, purchasedTime)) {

                                purchasedTemp.add(purchaseId)
                            } else {
                                EntranceModelHandler.removeById(this, username, entranceData.entrance.entranceUniqueId!!)
                            }
                        }
                    }
                }
            }

            this.isPackageBuyed = true
            this.entranceMultiDetailAdapter.setPackageBuyed(this.isPackageBuyed)
            this.entranceMultiDetailAdapter.notifyDataSetChanged()
            this.downloadImages(purchasedTemp)

            AlertClass.showAlertMessage(this, "ActionResult",
                    "PurchasedSuccess",
                    "success", null)

            this.setMenuItemColor(1, R.color.colorConcoughRedLight)
        } catch (exc: Exception) {}
    }

    private fun downloadImages(ids: ArrayList<Int>) {
        val username = UserDefaultsSingleton.getInstance(this).getUsername()!!
        val purchased = PurchasedModelHandler.getAllPurchasedIn(this.applicationContext,
                username, ids.toTypedArray())
        if (purchased != null) {
            for (pm in purchased) {
                if (pm.productType === "Entrance") {
                    val em = EntranceModelHandler.getByUsernameAndId(this.applicationContext,
                            username, pm.productUniqueId)
                    if (em != null) {
                        this.downloadEsetImage(em.setId)
                    }
                }
            }
        }
    }

    private fun downloadEsetImage(imageId: Int) {
        val url = MediaRestAPIClass.makeEsetImageUrl(imageId)

        if (url != null) {
            val data = MediaCacheSingleton.getInstance(applicationContext)[url]
            if (data != null) {
                saveToFile(data, imageId)
            } else {
                MediaRestAPIClass.downloadEsetImage(this, imageId, { data1, httpErrorType ->
                    runOnUiThread {
                        if (httpErrorType !== HTTPErrorType.Success) {
                            if (httpErrorType === HTTPErrorType.Refresh) {
                                downloadEsetImage(imageId)
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
            val fos = FileOutputStream(photo.getPath())

            fos.write(data)
            fos.close()
        } catch (e: java.io.IOException) {
//            Log.e("PictureDemo", "Exception in photoCallback", e)
        }

    }

    private enum class EntranceMultiDetailHolderType(val code: Int) {
        ENTRANCE_MULTI_INITIAL(1),
        ENTRANCE_MULTI_ITEM(2);

        fun getValue(): Int {
            return this.code
        }
    }

    private class EntranceMultiDetailAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private lateinit var context: Context
        private var entranceMultiSaleStructure: EntranceMultiSaleStructure? = null
        private lateinit var entrances: ArrayList<InternalEntranceData>
        private var isPackageBuyed = false

        constructor(context: Context, entranceMultiSaleStructure: EntranceMultiSaleStructure?,
                           entrances: ArrayList<InternalEntranceData>) {
            this.context = context
            this.entranceMultiSaleStructure = entranceMultiSaleStructure
            this.entrances = entrances
        }

        internal fun setItems(entranceMultiSaleStructure: EntranceMultiSaleStructure?,
                              entrances: ArrayList<InternalEntranceData>, isPackageBuyed: Boolean) {
            this.entrances = entrances
            this.entranceMultiSaleStructure = entranceMultiSaleStructure
            this.isPackageBuyed = isPackageBuyed
        }

        internal fun setPackageBuyed(isPackageBuyed: Boolean) {
            this.isPackageBuyed = isPackageBuyed
        }

        internal fun showBuyDialog() {
            if (this.entranceMultiSaleStructure != null) {
                if (UserDefaultsSingleton.getInstance(context.applicationContext).hasWallet()) {
                    val ws = UserDefaultsSingleton.getInstance(context.applicationContext).getWalletInfo()
                    val cost = this.entranceMultiSaleStructure!!.payCost

                    var canBuy = true
                    if (cost > ws?.cash ?: 0) {
                        canBuy = false
                    }

                    val dialog = EntranceBuyDialog(context)
                    dialog.setCancelable(false)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.listener = context as EntranceMultiDetailActivity
                    dialog.show()
                    dialog.setupDialog("EntranceMulti", (context as EntranceMultiDetailActivity).uniqueId!!,
                            canBuy, cost, ws?.cash ?: 0, null)

                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == EntranceMultiDetailHolderType.ENTRANCE_MULTI_INITIAL.getValue()) {
                val view = LayoutInflater.from(context).inflate(R.layout.item_entrancemulti_initial, parent, false)
                return EMDInitialHolder(view)
            } else {
                val view = LayoutInflater.from(context).inflate(R.layout.item_entrancemulti_item, parent, false)
                return EMDEntranceItemHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if (position == 0) {
                var count = 0
                this.entrances.let {
                    count = this.entrances.size
                }

                if (this.entranceMultiSaleStructure != null) {
                    (holder as EMDInitialHolder).setupHolder(this.context,
                            this.entrances.get(position).entrance,
                            count,
                            false, this.isPackageBuyed)
                } else {
                    (holder as EMDInitialHolder).setupHolder(this.context,
                            this.entrances.get(position).entrance,
                            count,
                            true, this.isPackageBuyed)
                }

            } else {
                val buyedCount = this.entrances.get(position - 1).buyedCount

                val item = this.entrances.get(position - 1).entrance
                (holder as EMDEntranceItemHolder).setupHolder(this.context,
                        item,
                        buyedCount)

                (holder as EMDEntranceItemHolder).itemView.setOnClickListener {
                    val i = EntranceDetailActivity.newIntent(context, item.entranceUniqueId, "EntranceMulti")
                    context.startActivity(i)
                }
            }
        }

        override fun getItemCount(): Int {
            if (this.entrances.size > 0) {
                return this.entrances.size + 1
            }
            return 1
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                return EntranceMultiDetailHolderType.ENTRANCE_MULTI_INITIAL.getValue()
            } else {
                return EntranceMultiDetailHolderType.ENTRANCE_MULTI_ITEM.getValue()
            }
        }

        public class EMDInitialHolder: RecyclerView.ViewHolder {
            private lateinit var logoImageView: ImageView
            private lateinit var entranceTypeTextView: TextView
            private lateinit var entranceSetTextView: TextView
            private lateinit var entranceOrgTextView: TextView
            private lateinit var buyButton: Button
            private lateinit var costLabelTextView: TextView
            private lateinit var costValueTextView: TextView
            private lateinit var payCostLabelTextView: TextView
            private lateinit var payCostValueTextView: TextView
            private lateinit var costContainer: LinearLayout
            private lateinit var loadingProgressBar: ProgressBar
            private lateinit var entrancesCountTextView: TextView

            private var entranceSaleStruct: EntranceMultiSaleStructure? = null

            constructor(itemView: View): super(itemView) {
                this.entranceTypeTextView = itemView.findViewById(R.id.itemEMI_entranceTypeTextView) as TextView
                this.logoImageView = itemView.findViewById(R.id.itemEMI_logoImageView) as ImageView
                this.entranceSetTextView = itemView.findViewById(R.id.itemEMI_entranceSetTextView) as TextView
                this.entranceOrgTextView = itemView.findViewById(R.id.itemEMI_entranceOrgTextView) as TextView
                this.buyButton = itemView.findViewById(R.id.itemEMI_buyButton) as Button
                this.costLabelTextView = itemView.findViewById(R.id.itemEMI_costLableTextView) as TextView
                this.costValueTextView = itemView.findViewById(R.id.itemEMI_costValueTextView) as TextView
                this.payCostLabelTextView = itemView.findViewById(R.id.itemEMI_payCostLableTextView) as TextView
                this.payCostValueTextView = itemView.findViewById(R.id.itemEMI_payCostValueTextView) as TextView
                this.costContainer = itemView.findViewById(R.id.itemEMI_costContainer) as LinearLayout
                this.loadingProgressBar = itemView.findViewById(R.id.itemEMI_loadingProgressBar) as ProgressBar
                this.entrancesCountTextView = itemView.findViewById(R.id.itemEMI_entrancesCountTextView) as TextView

                this.loadingProgressBar.getIndeterminateDrawable().setColorFilter(
                        ContextCompat.getColor(itemView.context , R.color.colorConcoughGray),
                        PorterDuff.Mode.SRC_IN)

                this.entranceTypeTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.entranceSetTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Bold
                this.entranceOrgTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.costLabelTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.payCostLabelTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.costValueTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.payCostValueTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Bold
                this.entrancesCountTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.buyButton.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
            }

            fun setupHolder(context: Context, firstEntrance: EntranceStruct, entrancesCount: Int,
                            disableBuy: Boolean, isPackageBuyed: Boolean) {
                if (isPackageBuyed) {
                    this.disableBuyGreen(context)
                } else {
                    this.disableBuy(context, disableBuy)
                }
                this.entranceOrgTextView.text = firstEntrance.entranceOrgTitle
                this.entranceTypeTextView.text = firstEntrance.entranceTypeTitle
                this.entranceSetTextView.text = "${firstEntrance.entranceSetTitle} (${firstEntrance.entranceGroupTitle})"
                this.entrancesCountTextView.text = "حاوی ${FormatterSingleton.getInstance().NumberFormatter.format(entrancesCount)} آزمون"

                this.downloadImage(context, firstEntrance.entranceSetId!!)

                this.buyButton.setOnClickListener {

                    if (this@EMDInitialHolder.entranceSaleStruct != null) {
                        if (UserDefaultsSingleton.getInstance(context.applicationContext).hasWallet()) {
                            val ws = UserDefaultsSingleton.getInstance(context.applicationContext).getWalletInfo()
                            val cost = this@EMDInitialHolder.entranceSaleStruct!!.payCost

                            var canBuy = true
                            if (cost > ws?.cash ?: 0) {
                                canBuy = false
                            }

                            val dialog = EntranceBuyDialog(context)
                            dialog.setCancelable(false)
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.listener = context as EntranceMultiDetailActivity
                            dialog.show()
                            dialog.setupDialog("EntranceMulti", context.uniqueId!!,
                                    canBuy, cost, ws?.cash ?: 0, null)

                        } else {
                            this@EMDInitialHolder.disableBuyButton(context)
                            (context as EntranceMultiDetailActivity).createWallet()
                        }
                    }
                }
            }

            fun configureCosts(context: Context, saleStructure: EntranceMultiSaleStructure) {
                this.entranceSaleStruct = saleStructure
                if (saleStructure.totalCost == 0) {
                    this.costValueTextView.text = "رایگان"
                } else {
                    this.costValueTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(saleStructure.totalCost)
                }

                if (saleStructure.payCost == 0) {
                    this.payCostValueTextView.text = "رایگان"
                    this.payCostValueTextView.setTextColor(ContextCompat.getColor(context, R.color.colorConcoughRed))
                } else {
                    this.payCostValueTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(saleStructure.payCost)
                }

            }

            fun disableBuy(context: Context, state: Boolean) {
                if (state) {
                    this.costContainer.visibility = View.INVISIBLE
                    this.loadingProgressBar.visibility = View.VISIBLE

                    this.buyButton.isEnabled = false
                    this.buyButton.setTextColor(ContextCompat.getColor(context , R.color.colorConcoughGray2))
                    this.buyButton.background = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_gray_style)
                } else {
                    this.costContainer.visibility = View.VISIBLE
                    this.loadingProgressBar.visibility = View.INVISIBLE

                    this.buyButton.isEnabled = true
                    this.buyButton.setTextColor(ContextCompat.getColor(context , R.color.colorConcoughBlue))
                    this.buyButton.background = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_style)
                }
            }

            fun disableBuyGreen(context: Context) {
                this.costContainer.visibility = View.VISIBLE
                this.loadingProgressBar.visibility = View.INVISIBLE

                this.buyButton.text = "خریداری شده"
                this.buyButton.isEnabled = false
                this.buyButton.setTextColor(ContextCompat.getColor(context , R.color.colorConcoughGreen))
                this.buyButton.background = ContextCompat.getDrawable(context, R.drawable.concough_outline_green_rounded_style)
            }

            fun disableBuyButton(context: Context) {
                this.buyButton.isEnabled = false
                this.buyButton.text = "●●●"
                this.buyButton.background = ContextCompat.getDrawable(context, R.drawable.concough_border_radius_lightgray_style)
                this.buyButton.setTextColor(ContextCompat.getColor(context , R.color.colorConcoughGray))
            }

            private fun downloadImage(context: Context, imageId: Int) {
                val url = MediaRestAPIClass.makeEsetImageUrl(imageId)
                val data = MediaCacheSingleton.getInstance(context.applicationContext)[url!!]
                if (data != null) {

                    Glide.with(context)

                            .load(data)
                            //.crossFade()
                            .dontAnimate()
                            .into(this.logoImageView)
                            .onLoadFailed(null, ContextCompat.getDrawable(context.applicationContext, R.drawable.no_image))


                } else {
                    doAsync {
                        MediaRestAPIClass.downloadEsetImage(context.applicationContext, imageId, { data1, httpErrorType ->
                            uiThread {
                                if (httpErrorType !== HTTPErrorType.Success) {
                                    if (httpErrorType === HTTPErrorType.Refresh) {
                                        downloadImage(context, imageId)
                                    } else {
                                        this@EMDInitialHolder.logoImageView.setImageResource(R.drawable.no_image)
                                    }
                                } else {
                                    MediaCacheSingleton.getInstance(context.applicationContext)[url] = data1!!

                                    Glide.with(context)

                                            .load(data1)
                                            //.crossFade()
                                            .dontAnimate()
                                            .into(this@EMDInitialHolder.logoImageView)
                                            .onLoadFailed(null, ContextCompat.getDrawable(context.applicationContext, R.drawable.no_image))

                                }
                            }
                        }) { }
                    }
                }
            }

        }

        public class EMDEntranceItemHolder : RecyclerView.ViewHolder {
            private lateinit var yearTextView: TextView
            private lateinit var monthTextView: TextView
            private lateinit var buyCountContainer: LinearLayout
            private lateinit var buyCountTextView: TextView
            private lateinit var buyedImageView: ImageView

            constructor(itemView: View): super(itemView) {
                this.yearTextView = itemView.findViewById(R.id.itemEMIT_yearTextView) as TextView
                this.monthTextView = itemView.findViewById(R.id.itemEMIT_monthTextView) as TextView
                this.buyCountContainer = itemView.findViewById(R.id.itemEMIT_buyCountContainer) as LinearLayout
                this.buyCountTextView = itemView.findViewById(R.id.itemEMIT_buyCountTextView) as TextView
                this.buyedImageView = itemView.findViewById(R.id.itemEMIT_buyedImageView) as ImageView

                this.monthTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Light
                this.yearTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Regular
                this.buyCountTextView.typeface = FontCacheSingleton.getInstance(itemView.context.applicationContext).Bold
            }

            fun setupHolder(context: Context, entrance: EntranceStruct, buyedCount: Int) {
                val username = UserDefaultsSingleton.getInstance(context.applicationContext).getUsername()!!
                val uniqueId = entrance.entranceUniqueId!!

                var buyed = false
                if (EntranceModelHandler.existById(context.applicationContext, username, uniqueId)) {
                    buyed = true
                }

                this.yearTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(entrance.entranceYear)
                this.monthTextView.text = monthToString(entrance.entranceMonth!!)

                if (buyed) {
                    this.monthTextView.setTextColor(ContextCompat.getColor(context, R.color.colorConcoughGreenLight))
                    this.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.colorConcoughGreen))
                } else {
                    this.monthTextView.setTextColor(ContextCompat.getColor(context, R.color.colorConcoughBlackLight))
                    this.yearTextView.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                }

                this.buyCountTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(buyedCount)

                if (!buyed) {
                    this.buyedImageView.visibility = View.INVISIBLE
                    this.buyCountContainer.visibility = View.VISIBLE
                } else {
                    this.buyedImageView.visibility = View.VISIBLE
                    this.buyCountContainer.visibility = View.INVISIBLE
                }
            }
        }
    }
}
