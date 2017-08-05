package com.concough.android.singletons

import android.app.Activity
import android.content.Context
import android.os.*
import android.util.Log
import com.concough.android.concough.HomeActivity
import com.concough.android.concough.LoginActivity
import com.concough.android.concough.R
import com.concough.android.concough.SignupCodeActivity
import com.concough.android.models.EntranceModelHandler
import com.concough.android.models.PurchasedModelHandler
import com.concough.android.rest.BasketRestAPIClass
import com.concough.android.structures.EntranceStruct
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by abolfazl on 7/10/17.
 */
class BasketSingleton : Handler.Callback {
    interface BasketSingletonListener {
        fun onLoadItemCompleted(count: Int)
        fun onCreateCompleted()
        fun onAddCompleted(count: Int)
        fun onRemoveCompleted(count: Int, position: Int)
        fun onCheckout(count: Int, purchased: HashMap<Int, PurchasedItem>)
    }

    public data class SaleItem(var id: Int, var created: Date, var cost: Int, var target: Any, var type: String)
    public data class PurchasedItem(var purchasedId: Int, var downlaoded: Int, var purchasedTime: Date)

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    private var basketId: String? = null
    private var totalCost: Int = 0
    private var sales: ArrayList<SaleItem> = ArrayList()

    var listener: BasketSingletonListener? = null

    companion object Factory {
        private val TAG = "BasketSingleton"
        private val CONTEXT_WHO_KEY = "CONTEXT_WHO"
        private val HANDLE_THREAD_NAME = "Concough-BasketSingleton"

        private val LOAD_BASKET_ITEMS = 0
        private val CREATE_BASKET = 1
        private val ADD_TO_BASKET = 2
        private val DELETE_FROM_BASKET = 3
        private val CHECKOUT_BASKET = 4

        private var sharedInstance : BasketSingleton? = null

        @JvmStatic
        fun  getInstance(): BasketSingleton {
            if (sharedInstance == null)
                sharedInstance = BasketSingleton()

            return sharedInstance!!
        }
    }

    private constructor() {
        this.handlerThread = HandlerThread(HANDLE_THREAD_NAME)
        this.handlerThread?.start()

        val looper: Looper? = this.handlerThread?.looper
        if (looper != null) {
            this.handler = Handler(looper, this)
        }
    }

    fun stopHandler() {
        this.handlerThread?.quit()
    }

    var BasketId: String?
        get() = this.basketId
        set(value) {this.basketId = value}

    val SalesCount: Int
        get() = this.sales.count()

    val TotalCost: Int
        get() = this.totalCost

    fun addSale(saleId: Int, created: Date, cost: Int, target: Any, type: String) {
        synchronized(this.sales) {
            var productId: String? = null
            if (type == "Entrance") {
                val entrance = target as? EntranceStruct
                if (entrance != null) {
                    productId = entrance.entranceUniqueId

                    if(this.findSaleByTargetId(productId!!, type) != null) {
                        if (this.findSaleByTargetId(productId!!, type)!! >= 0) {
                            return
                        }
                    }
                }
            }

            this.sales.add(SaleItem(saleId, created, cost, target, type))
            this.totalCost += cost
        }
    }

    fun findSaleByTargetId(targetId: String, type: String): Int? {

        val sale: SaleItem? = this.sales.find find@ {
            if (it.type == type) {
                val target = it.target as? EntranceStruct
                if (target != null) {
                    if (target.entranceUniqueId == targetId) return@find true
                }
            }
            return@find false
        }

        return sale?.id;
    }

    fun findSaleById(saleId: Int): Int? {

        val sale: SaleItem? = this.sales.find find@ {
            if (it.id == saleId) {
                return@find true
            }
            return@find false
        }

        val index = this.sales.indexOf(sale)
        return index
    }

    fun removeSaleById(saleId: Int) {
        val sale: SaleItem? = this.sales.find find@ {
            if (it.id == saleId) {
                return@find true
            }
            return@find false
        }

        if (sale != null) {
            synchronized(this.sales) {
                this.totalCost -= sale.cost
                this.sales.remove(sale)
            }
        }

        if (this.sales.count() == 0) {
            this.BasketId = null
            this.totalCost = 0
        }
    }

    fun removeAllSales() {
        synchronized(this.sales) {
            this.sales.clear()
            this.totalCost = 0
            this.basketId = null
        }
    }

    fun loadBasketItems(context: Context?) {
        if (this.handler != null) {
            val msg = this.handler?.obtainMessage(LOAD_BASKET_ITEMS)
            msg?.target = Handler(context?.mainLooper)
            msg?.obj = context

            this.handler?.sendMessage(msg)
        }
    }

    fun createBasket(context: Context?) {
        if (this.handler != null) {
            val msg = this.handler?.obtainMessage(CREATE_BASKET)
            msg?.target = Handler(context?.mainLooper)
            msg?.obj = context

            this.handler?.sendMessage(msg)
        }
    }

    fun addSale(context: Context?, target: Serializable, type: String) {
        var productId: String? = null
        synchronized(this.sales) {
            if (type == "Entrance") {
                val entrance = target as? EntranceStruct
                if (entrance != null) {
                    productId = entrance.entranceUniqueId

                    if (this.findSaleByTargetId(productId!!, type) != null) {
                        if (this.findSaleByTargetId(productId!!, type)!! >= 0) {
                            return
                        }
                    }
                }
            }
        }

        if (this.handler != null && productId != null) {
            val msg = this.handler?.obtainMessage(ADD_TO_BASKET)
            msg?.target = Handler(context?.mainLooper)
            msg?.obj = context

            val bundle = Bundle()
            bundle.putString("PRODUCT_ID", productId!!)
            bundle.putString("TYPE", type)
            bundle.putSerializable("TARGET", target)
            msg?.data = bundle

            this.handler?.sendMessage(msg)
        }
    }

    fun removeSaleById(context: Context?, saleId: Int, position: Int) {
        if (this.findSaleById(saleId) != null) {
            if (this.handler != null) {
                val msg = this.handler?.obtainMessage(DELETE_FROM_BASKET)
                msg?.target = Handler(context?.mainLooper)
                msg?.obj = context

                val bundle = Bundle()
                bundle.putInt("SALE_ID", saleId)
                bundle.putInt("SALE_POSITION", position)
                msg?.data = bundle

                this.handler?.sendMessage(msg)
            }
        }
    }

    fun checkout(context: Context?) {
        if (this.handler != null) {
            val msg = this.handler?.obtainMessage(CHECKOUT_BASKET)
            msg?.target = Handler(context?.mainLooper)
            msg?.obj = context

            this.handler?.sendMessage(msg)
        }
    }

    private fun getSaleTypeById(saleId: Int): String? {
        var local: String? = null
        synchronized(this.sales) {
            for (item in this.sales) {
                if (item.id == saleId)
                        local = item.type
            }
        }

        return local
    }

    fun getSaleById(saleId: Int): Any? {
        var local: Any? = null
        synchronized(this.sales) {
            for (item in this.sales) {
                if (item.id == saleId)
                    local = item.target
            }
        }

        return local
    }

    fun getSaleByIndex(index: Int): SaleItem? {
        var local: SaleItem? = null
        synchronized(this.sales) {
            if (index < this.sales.count()) {
                local = this.sales.get(index)
            }
        }

        return local
    }

    override fun handleMessage(msg: Message?): Boolean {
        when(msg?.what) {
            0 -> this.handleLoadBasketItems(msg)
            1 -> this.handleCreateBasket(msg)
            2 -> this.handleAddSale(msg)
            3 -> this.handleRemoveFromBasket(msg)
            4 -> this.handleCheckout(msg)
        }
        return true
    }


    private fun handleLoadBasketItems(msg: Message?) {
//            // TODO: show loading

        val context: Context? = msg?.obj as Context?

        BasketRestAPIClass.loadBasketItems(context?.applicationContext!!, {data, error ->
            // TODO: hide loading that showed before
            if (error == HTTPErrorType.Success) {
                if (data != null) {
                    try {
                        val status = data.get("status").asString
                        when (status) {
                            "OK" -> {
                                this@BasketSingleton.BasketId = data.get("basket_uid").asString

                                try {
                                    if (data.get("records") != null) {
                                        val records = data.getAsJsonArray("records")

                                        var salesLocal: ArrayList<SaleItem> = ArrayList()
                                        var tCostLocal = 0

                                        for (item in records) {
                                            val sale_id = item.asJsonObject.get("id").asInt
                                            val cost = item.asJsonObject.get("pay_amount").asInt
                                            val createdStr = item.asJsonObject.get("created").asString
                                            val created = FormatterSingleton.getInstance().UTCDateFormatter.parse(createdStr)

                                            val target = item.asJsonObject.getAsJsonObject("target")
                                            val productType = target.get("product_type").asString

                                            if (productType == "Entrance") {
                                                val entrance = EntranceStruct()
                                                entrance.entranceBookletCounts = target.get("booklets_count").asInt
                                                entrance.entranceDuration = target.get("duration").asInt
                                                entrance.entranceExtraData = Gson().fromJson<JsonElement>(target.get("extra_data").asString, JsonElement::class.java)
                                                entrance.entranceGroupTitle = target.getAsJsonObject("entrance_set").getAsJsonObject("group").get("title").asString
                                                entrance.entranceLastPublished = FormatterSingleton.getInstance().UTCDateFormatter.parse(target.get("last_published").asString)
                                                entrance.entranceOrgTitle = target.getAsJsonObject("organization").get("title").asString
                                                entrance.entranceSetId = target.getAsJsonObject("entrance_set").get("id").asInt
                                                entrance.entranceSetTitle = target.getAsJsonObject("entrance_set").get("title").asString
                                                entrance.entranceTypeTitle = target.getAsJsonObject("entrance_type").get("title").asString
                                                entrance.entranceUniqueId = target.get("unique_key").asString
                                                entrance.entranceYear = target.get("year").asInt

                                                tCostLocal += cost
                                                salesLocal.add(SaleItem(sale_id, created, cost, entrance, "Entrance"))
                                            }
                                        }

                                        synchronized(this@BasketSingleton.sales) {
                                            this@BasketSingleton.sales = salesLocal
                                            this@BasketSingleton.totalCost = tCostLocal
                                        }
                                    }
                                } catch (exc: Exception) {

                                }

                                if (this@BasketSingleton.listener != null) {
                                    this@BasketSingleton.listener?.onLoadItemCompleted(this@BasketSingleton.sales.count())
                                }
                            }

                            "Error" -> {
                                val errorType = data.get("error_type").asString
                                when (errorType) {
                                    "EmptyArray" -> {
                                        if (this.listener != null) {
                                            listener?.onLoadItemCompleted(0)
                                        }
                                    }
                                    else -> {
                                        // TODO: show simple error message with messageType = "ErrorResult"
                                    }
                                }
                            }
                        }

                    } catch (exc: Exception) {

                    }
                }
            } else {
                // TODO: show top message with type = error
            }

        }, {error ->
            // TODO: hide loading that showed before
            if (error != null) {
                when (error) {
                    NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
                        // TODO: show top message about error with type error
                    }
                    else -> {
                        // TODO: Show top message about error without type
                    }
                }
            }

        })
    }

    private fun handleCreateBasket(msg: Message?) {
//            // TODO: show loading

        val context: Context? = msg?.obj as Context?

        BasketRestAPIClass.createBasket(context?.applicationContext!!, {data, error ->
            // TODO: hide loading that showed before
            if (error == HTTPErrorType.Success) {
                if (data != null) {
                    try {
                        val status = data.get("status").asString
                        when (status) {
                            "OK" -> {
                                try {
                                    this@BasketSingleton.basketId = data.get("basket_uid").asString
                                } catch (exc: Exception) {
                                }
                                if (this@BasketSingleton.listener != null) {
                                    this@BasketSingleton.listener?.onCreateCompleted()
                                }
                            }

                            "Error" -> {
                                val errorType = data.get("error_type").asString
                                when (errorType) {
                                    else -> {
                                        // TODO: show simple error message with messageType = "ErrorResult"
                                    }
                                }
                            }
                        }

                    } catch (exc: Exception) {

                    }
                }
            } else {
                // TODO: show top message with type = error
            }

        }, {error ->
            // TODO: hide loading that showed before
            if (error != null) {
                when (error) {
                    NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
                        // TODO: show top message about error with type error
                    }
                    else -> {
                        // TODO: Show top message about error without type
                    }
                }
            }

        })
    }

    private fun handleAddSale(msg: Message?) {
//            // TODO: show loading

        val context: Context? = msg?.obj as Context?
        val bundle = msg?.data ?: return

        val productId = bundle.getString("PRODUCT_ID")
        val productType = bundle.getString("TYPE")
        val target: Any = bundle.getSerializable("TARGET")

        BasketRestAPIClass.addProductToBasket(context?.applicationContext!!, this.basketId!!, productId!!, productType!!,  {data, error ->
            // TODO: hide loading that showed before
            if (error == HTTPErrorType.Success) {
                if (data != null) {
                    try {
                        val status = data.get("status").asString
                        when (status) {
                            "OK" -> {
                                try {
                                    this@BasketSingleton.basketId = data.get("basket_uid").asString
                                    if (basketId == this@BasketSingleton.BasketId) {
                                        val sale = data.get("records").asJsonObject
                                        if (sale != null) {
                                            val cost = sale.get("pay_amount").asInt
                                            val saleId = sale.get("id").asInt
                                            val targetProductUnjqueKey = sale.getAsJsonObject("target").get("unique_key").asString
                                            val targetProductType = sale.getAsJsonObject("target").get("sale_type").asString

                                            val createdStr = sale.get("created").asString
                                            val created = FormatterSingleton.getInstance().UTCDateFormatter.parse(createdStr)

                                            if (targetProductType == productType && targetProductUnjqueKey == productId) {
                                                synchronized(this@BasketSingleton.sales) {
                                                    this@BasketSingleton.sales.add(SaleItem(saleId, created, cost, target, productType))
                                                    this@BasketSingleton.totalCost += cost
                                                }

                                                if (this@BasketSingleton.listener != null) {
                                                    this@BasketSingleton.listener?.onAddCompleted(this@BasketSingleton.sales.count())
                                                }
                                            }
                                        }
                                    }

                                } catch (exc: Exception) {
                                }
                            }

                            "Error" -> {
                                val errorType = data.get("error_type").asString
                                when (errorType) {
                                    "DuplicateSale" -> {
                                        // TODO: show simple error message "BasketResult"
                                    }
                                    "EntrancenotExist" -> {
                                        // TODO: show simple error message "EntranceResult"
                                    }

                                    else -> {
                                        // TODO: show simple error message with messageType = "ErrorResult"
                                    }
                                }
                            }
                        }

                    } catch (exc: Exception) {

                    }
                }
            } else {
                // TODO: show top message with type = error
            }

        }, {error ->
            // TODO: hide loading that showed before
            if (error != null) {
                when (error) {
                    NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
                        // TODO: show top message about error with type error
                    }
                    else -> {
                        // TODO: Show top message about error without type
                    }
                }
            }

        })
    }

    private fun handleRemoveFromBasket(msg: Message?) {
//            // TODO: show loading

        val context: Context? = msg?.obj as Context?
        val bundle = msg?.data ?: return

        val saleId = bundle.getInt("SALE_ID")
        val salePosition = bundle.getInt("SALE_POSITION")

        BasketRestAPIClass.removeSaleFromBasket(context?.applicationContext!!, this.basketId!!, saleId!!,  {data, error ->
            // TODO: hide loading that showed before
            if (error == HTTPErrorType.Success) {
                if (data != null) {
                    try {
                        val status = data.get("status").asString
                        when (status) {
                            "OK" -> {
                                try {
                                    synchronized(this.sales) {
                                        this@BasketSingleton.removeSaleById(saleId)
                                    }
                                    if (this@BasketSingleton.listener != null) {
                                        this@BasketSingleton.listener?.onRemoveCompleted(this@BasketSingleton.sales.count(), salePosition)
                                    }

                                } catch (exc: Exception) {
                                }
                            }

                            "Error" -> {
                                val errorType = data.get("error_type").asString
                                when (errorType) {
                                    "SaleNotExist" -> {
                                        // TODO: show simple error message "BasketResult"
                                    }
                                    else -> {
                                        // TODO: show simple error message with messageType = "ErrorResult"
                                    }
                                }
                            }
                        }

                    } catch (exc: Exception) {

                    }
                }
            } else {
                // TODO: show top message with type = error
            }

        }, {error ->
            // TODO: hide loading that showed before
            if (error != null) {
                when (error) {
                    NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
                        // TODO: show top message about error with type error
                    }
                    else -> {
                        // TODO: Show top message about error without type
                    }
                }
            }

        })
    }

    private fun handleCheckout(msg: Message?) {
//            // TODO: show loading

        val context: Context? = msg?.obj as Context?

        BasketRestAPIClass.checkoutBasket(context?.applicationContext!!, this@BasketSingleton.basketId!!,  {data, error ->
            // TODO: hide loading that showed before
            if (error == HTTPErrorType.Success) {
                if (data != null) {
                    try {
                        val status = data.get("status").asString
                        when (status) {
                            "OK" -> {
                                try {
                                    val purchased = data.getAsJsonArray("purchased")
                                    if (purchased != null) {
                                        val username: String? = UserDefaultsSingleton.getInstance(context.applicationContext!!).getUsername(context.applicationContext!!)
                                        var localPurchased = HashMap<Int, PurchasedItem>()

                                        for (item in purchased) {
                                            val saleId = item.asJsonObject.get("sale_id").asInt
                                            val purchaseId = item.asJsonObject.get("purchase_id").asInt
                                            val downloaded = item.asJsonObject.get("downloaded").asInt

                                            val purchasedTimeStr = item.asJsonObject.get("purchase_time").asString
                                            val purchasedTime = FormatterSingleton.getInstance().UTCDateFormatter.parse(purchasedTimeStr)

                                            // Update Realm db
                                            val saleType = this@BasketSingleton.getSaleTypeById(saleId)
                                            if (saleType == "Entrance") {
                                                val entrance = this.getSaleById(saleId) as? EntranceStruct
                                                if (entrance != null) {
                                                    if (EntranceModelHandler.add(context.applicationContext, username!!, entrance)) {
                                                        if (!PurchasedModelHandler.add(context.applicationContext, purchaseId, username, false,
                                                                downloaded, false, saleType, entrance.entranceUniqueId!!, purchasedTime)) {

                                                            // rollback entrance insert
                                                            EntranceModelHandler.removeById(context.applicationContext,username, entrance.entranceUniqueId!!)
                                                        }
                                                    }
                                                }
                                            }


                                            localPurchased.put(saleId, PurchasedItem(purchaseId, downloaded, purchasedTime))
                                            this@BasketSingleton.removeSaleById(saleId)
                                        }

                                        synchronized(this.sales) {
                                            this@BasketSingleton.basketId = null
                                        }
                                        if (this@BasketSingleton.listener != null) {
                                            this@BasketSingleton.listener?.onCheckout(this@BasketSingleton.sales.count(), localPurchased)
                                        }

                                    }
                                } catch (exc: Exception) {
                                }
                            }

                            "Error" -> {
                                val errorType = data.get("error_type").asString
                                when (errorType) {
                                    "EmptyBasket" -> {
                                        // TODO: show simple error message "BasketResult" and on result
                                        this@BasketSingleton.basketId = null
                                        this@BasketSingleton.removeAllSales()

                                    }

                                    else -> {
                                        // TODO: show simple error message with messageType = "ErrorResult"
                                    }
                                }
                            }
                        }

                    } catch (exc: Exception) {

                    }
                }
            } else {
                // TODO: show top message with type = error
            }

        }, {error ->
            // TODO: hide loading that showed before
            if (error != null) {
                when (error) {
                    NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
                        // TODO: show top message about error with type error
                    }
                    else -> {
                        // TODO: Show top message about error without type
                    }
                }
            }

        })

    }
}
