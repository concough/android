package com.concough.android.rest

import android.content.Context
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.singletons.UrlMakerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by abolfazl on 7/10/17.
 */
class BasketRestAPIClass {
    companion object Factory {
        val TAG = "BasketRestAPIClass"

        // Get User Basket Items
        @JvmStatic
        fun loadBasketItems(context: Context, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().getLoadBasketItemsUrl() ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.get(url = fullPath, headers = headers!!)

                    request.enqueue(object: Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            failure(NetworkErrorType.toType(t))
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            val resCode = HTTPErrorType.toType(response?.code()!!)
//                          Log.d(TAG, resCode.toString())
                            when (resCode) {
                                HTTPErrorType.Success -> {
                                    val res = response.body()!!.string()
                                    try {
                                        val jobj = Gson().fromJson(res, JsonObject::class.java)

                                        completion(jobj, resCode)
                                    } catch (exc: JsonParseException) {
                                        completion(null, HTTPErrorType.UnKnown)
                                    }
                                }
                                HTTPErrorType.UnAuthorized, HTTPErrorType.ForbiddenAccess -> {
                                    TokenHandlerSingleton.getInstance(context).assureAuthorized(true, completion = { authenticated, error ->
                                        if (authenticated && error == HTTPErrorType.Success) {
                                            completion(null, HTTPErrorType.Refresh)
                                        }
                                    }, failure = { error ->
                                        failure(error)
                                    })
                                }
                                else -> completion(null, resCode)
                            }
                        }
                    })
                } else {
                    completion(null, error)
                }
            }, failure = {error ->
                failure(error)
            })

        }

        // Create Basket API
        @JvmStatic
        fun createBasket(context: Context, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().getCreateBasketUrl() ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.post(url = fullPath, body = HashMap<String, Any>(), headers = headers!!)

                    request.enqueue(object: Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            failure(NetworkErrorType.toType(t))
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            val resCode = HTTPErrorType.toType(response?.code()!!)
//                          Log.d(TAG, resCode.toString())
                            when (resCode) {
                                HTTPErrorType.Success -> {
                                    val res = response.body()!!.string()
                                    try {
                                        val jobj = Gson().fromJson(res, JsonObject::class.java)

                                        completion(jobj, resCode)
                                    } catch (exc: JsonParseException) {
                                        completion(null, HTTPErrorType.UnKnown)
                                    }
                                }
                                HTTPErrorType.UnAuthorized, HTTPErrorType.ForbiddenAccess -> {
                                    TokenHandlerSingleton.getInstance(context).assureAuthorized(true, completion = { authenticated, error ->
                                        if (authenticated && error == HTTPErrorType.Success) {
                                            completion(null, HTTPErrorType.Refresh)
                                        }
                                    }, failure = { error ->
                                        failure(error)
                                    })
                                }
                                else -> completion(null, resCode)
                            }
                        }
                    })
                } else {
                    completion(null, error)
                }
            }, failure = {error ->
                failure(error)
            })

        }

        // Add Product To Basket
        @JvmStatic
        fun addProductToBasket(context: Context, basketId: String, productId: String, productType: String,
                               completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().getAddToBasketUrl(basketId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val parameters: HashMap<String, Any> = hashMapOf("product_id" to productId,
                            "product_type" to productType)

                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.put(url = fullPath, body = parameters, headers = headers!!)

                    request.enqueue(object: Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            failure(NetworkErrorType.toType(t))
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            val resCode = HTTPErrorType.toType(response?.code()!!)
//                          Log.d(TAG, resCode.toString())
                            when (resCode) {
                                HTTPErrorType.Success -> {
                                    val res = response.body()!!.string()
                                    try {
                                        val jobj = Gson().fromJson(res, JsonObject::class.java)

                                        completion(jobj, resCode)
                                    } catch (exc: JsonParseException) {
                                        completion(null, HTTPErrorType.UnKnown)
                                    }
                                }
                                HTTPErrorType.UnAuthorized, HTTPErrorType.ForbiddenAccess -> {
                                    TokenHandlerSingleton.getInstance(context).assureAuthorized(true, completion = { authenticated, error ->
                                        if (authenticated && error == HTTPErrorType.Success) {
                                            completion(null, HTTPErrorType.Refresh)
                                        }
                                    }, failure = { error ->
                                        failure(error)
                                    })
                                }
                                else -> completion(null, resCode)
                            }
                        }
                    })
                } else {
                    completion(null, error)
                }
            }, failure = {error ->
                failure(error)
            })

        }

        // Remove Product From Basket
        @JvmStatic
        fun removeSaleFromBasket(context: Context, basketId: String, saleId: Int, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().getRemoveSaleFromBasketUrl(basketId, saleId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.delete(url = fullPath, headers = headers!!)

                    request.enqueue(object: Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            failure(NetworkErrorType.toType(t))
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            val resCode = HTTPErrorType.toType(response?.code()!!)
//                          Log.d(TAG, resCode.toString())
                            when (resCode) {
                                HTTPErrorType.Success -> {
                                    val res = response.body()!!.string()
                                    try {
                                        val jobj = Gson().fromJson(res, JsonObject::class.java)

                                        completion(jobj, resCode)
                                    } catch (exc: JsonParseException) {
                                        completion(null, HTTPErrorType.UnKnown)
                                    }
                                }
                                HTTPErrorType.UnAuthorized, HTTPErrorType.ForbiddenAccess -> {
                                    TokenHandlerSingleton.getInstance(context).assureAuthorized(true, completion = { authenticated, error ->
                                        if (authenticated && error == HTTPErrorType.Success) {
                                            completion(null, HTTPErrorType.Refresh)
                                        }
                                    }, failure = { error ->
                                        failure(error)
                                    })
                                }
                                else -> completion(null, resCode)
                            }
                        }
                    })
                } else {
                    completion(null, error)
                }
            }, failure = {error ->
                failure(error)
            })

        }

        // Remove Product From Basket
        @JvmStatic
        fun checkoutBasket(context: Context, basketId: String, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().getCheckoutBasketUrl(basketId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.post(url = fullPath, body = HashMap<String, Any>(), headers = headers!!)

                    request.enqueue(object: Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            failure(NetworkErrorType.toType(t))
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            val resCode = HTTPErrorType.toType(response?.code()!!)
//                          Log.d(TAG, resCode.toString())
                            when (resCode) {
                                HTTPErrorType.Success -> {
                                    val res = response.body()!!.string()
                                    try {
                                        val jobj = Gson().fromJson(res, JsonObject::class.java)

                                        completion(jobj, resCode)
                                    } catch (exc: JsonParseException) {
                                        completion(null, HTTPErrorType.UnKnown)
                                    }
                                }
                                HTTPErrorType.UnAuthorized, HTTPErrorType.ForbiddenAccess -> {
                                    TokenHandlerSingleton.getInstance(context).assureAuthorized(true, completion = { authenticated, error ->
                                        if (authenticated && error == HTTPErrorType.Success) {
                                            completion(null, HTTPErrorType.Refresh)
                                        }
                                    }, failure = { error ->
                                        failure(error)
                                    })
                                }
                                else -> completion(null, resCode)
                            }
                        }
                    })
                } else {
                    completion(null, error)
                }
            }, failure = {error ->
                failure(error)
            })

        }

    }
}