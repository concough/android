package com.concough.android.concough.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView

import com.concough.android.concough.R
import com.concough.android.concough.interfaces.ProductBuyDelegate
import com.concough.android.general.AlertClass
import com.concough.android.rest.ProductRestAPIClass
import com.concough.android.settings.CONNECTION_MAX_RETRY
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.FormatterSingleton
import com.concough.android.singletons.UserDefaultsSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.dialog_entrance_buy.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

/**
 * Created by abolfazl on 10/24/18.
 */

class EntranceBuyDialog(context: Context) : Dialog(context) {
    private var retryCounter: Int = 0
    public var listener: ProductBuyDelegate? = null
    
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_entrance_buy)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        EBD_titleTextView.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Regular
        EBD_subTitleTextView.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Light
        EBD_walletCashTextView.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Bold
        EBD_costTitleTextView.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Regular
        EBD_costTextView.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Bold
        EBD_messageTextView.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Light

        EBD_cancelButton.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Light
        EBD_buyButton.typeface = FontCacheSingleton.getInstance(this.context.applicationContext).Regular

        EBD_subTitleTextView.visibility = View.GONE
    }

    fun setupDialog(productType: String, uniqueId: String, canBuy: Boolean, cost: Int, balance: Int, subTitle: String?) {
        if (productType === "EntranceMulti") {
            EBD_titleTextView.text = "خرید بسته آزمون"
        } else {
            EBD_titleTextView.text = "خرید آزمون"
        }

        if (subTitle != null) {
            EBD_subTitleTextView.text = subTitle
            EBD_subTitleTextView.visibility = View.VISIBLE
        } else {
            EBD_subTitleTextView.text = ""
            EBD_subTitleTextView.visibility = View.GONE
        }

        EBD_walletCashTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(balance.toLong())
        if (cost == 0) {
            EBD_costTextView.text = "رایگان"
            EBD_costTextView.setTextColor(ContextCompat.getColor(this.context, R.color.colorConcoughRed))
        } else {
            EBD_costTextView.text = FormatterSingleton.getInstance().NumberFormatter.format(cost.toLong()) + " بنکوق"
        }

        EBD_cancelButton.setOnClickListener { this@EntranceBuyDialog.dismiss() }

        EBD_buyButton.setOnClickListener {
            if (canBuy) {
                this.addToLibrary(uniqueId, productType)
            } else {
                AlertClass.showAlertMessage(this@EntranceBuyDialog.context,
                        "ErrorResult",
                        "UnsupportedVersion",
                        "error", null)
            }
        }
    }

    private fun setupButtons(canBuy: Boolean) {
        EBD_buyButton.isEnabled = true
        EBD_cancelButton.visibility = View.VISIBLE

        if (canBuy) {
            EBD_messageTextView.visibility = View.GONE
            EBD_buyButton.background = ContextCompat.getDrawable(this.context, R.drawable.concough_border_outline_style)
        } else {
            EBD_messageTextView.visibility = View.VISIBLE
            EBD_buyButton.text = "خرید بنکوق"
            EBD_buyButton.background = ContextCompat.getDrawable(this.context, R.drawable.concough_border_outline_green_style)
        }
    }

    private fun disableButtons() {
        EBD_cancelButton.visibility = View.GONE
        EBD_buyButton.isEnabled = false

        EBD_buyButton.background = ContextCompat.getDrawable(this.context, R.drawable.concough_border_outline_gray_style)
        EBD_buyButton.text = "●●●"
    }

    private fun addToLibrary(productId: String, productType: String) {
        doAsync {
            ProductRestAPIClass.addToLibrary(this@EntranceBuyDialog.context, productId, productType,
                    completion = { data, error ->
                        uiThread {
                            if (error != HTTPErrorType.Success) {
                                if (error == HTTPErrorType.Refresh) {
                                    this@EntranceBuyDialog.addToLibrary(productId, productType)
                                } else {
                                    if (this@EntranceBuyDialog.retryCounter < CONNECTION_MAX_RETRY) {
                                        this@EntranceBuyDialog.retryCounter += 1
                                        this@EntranceBuyDialog.addToLibrary(productId, productType)
                                    } else {
                                        this@EntranceBuyDialog.retryCounter = 0
                                        AlertClass.showTopMessage(this@EntranceBuyDialog.context, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                                    }
                                }
                            } else {
                                this@EntranceBuyDialog.retryCounter = 0
                                if (data != null) {
                                    try {
                                        val status = data.asJsonObject.get("status").asString
                                        when (status) {
                                            "OK" -> {
                                                this@EntranceBuyDialog.listener?.let {
                                                    this@EntranceBuyDialog.listener?.productBuyResult(data.asJsonObject, productId, productType)
                                                }
                                                this@EntranceBuyDialog.dismiss()
                                            }
                                            "Error" -> {
                                                val errorType = data.asJsonObject.get("error_type").asString
                                                when (errorType) {
                                                    "DuplicateSale" -> {
                                                        AlertClass.showAlertMessage(this@EntranceBuyDialog.context,
                                                                "BasketResult",
                                                                errorType,
                                                                "error") {
                                                            this@EntranceBuyDialog.dismiss()
                                                        }
                                                    }
                                                    "UnsupportedVersion" -> {
                                                        AlertClass.showAlertMessage(this@EntranceBuyDialog.context,
                                                                "ErrorResult",
                                                                errorType,
                                                                "error") {
                                                            this@EntranceBuyDialog.dismiss()
                                                        }
                                                    }
                                                    "ProductNotExist" -> {
                                                        AlertClass.showAlertMessage(this@EntranceBuyDialog.context,
                                                                "ErrorResult",
                                                                errorType,
                                                                "") {
                                                            this@EntranceBuyDialog.dismiss()
                                                        }
                                                    }
                                                    "WalletNotEnoughCash" -> {
                                                        AlertClass.showAlertMessage(this@EntranceBuyDialog.context,
                                                                "WalletResult",
                                                                errorType,
                                                                "error") {
                                                            this@EntranceBuyDialog.setupButtons(false)
                                                        }
                                                    }
                                                    else -> {
                                                        AlertClass.showAlertMessage(this@EntranceBuyDialog.context,
                                                                "ErrorResult",
                                                                errorType,
                                                                "", null)

                                                    }
                                                }

                                            }

                                        }
                                    } catch (exc: Exception) {

                                    }

                                }
                            }
                        }
                    }, failure = { error ->
                        uiThread {
                            if (this@EntranceBuyDialog.retryCounter < CONNECTION_MAX_RETRY) {
                                this@EntranceBuyDialog.retryCounter += 1
                                this@EntranceBuyDialog.addToLibrary(productId, productType)
                            } else {
                                this@EntranceBuyDialog.retryCounter = 0

                                if (error != null) {
                                    when (error) {
                                        NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                            AlertClass.showTopMessage(this@EntranceBuyDialog.context, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                                        }
                                        else -> {
                                            AlertClass.showTopMessage(this@EntranceBuyDialog.context, findViewById(R.id.container), "NetworkError", error.name, "", null)
                                        }
                                    }
                                }
                            }
                        }
                    })
        }
    }
}