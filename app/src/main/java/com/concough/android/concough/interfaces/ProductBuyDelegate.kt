package com.concough.android.concough.interfaces

import com.google.gson.JsonObject

/**
 * Created by abolfazl on 10/25/18.
 */
interface ProductBuyDelegate {
    fun productBuyResult(data: JsonObject, productId: String, productType: String)
}