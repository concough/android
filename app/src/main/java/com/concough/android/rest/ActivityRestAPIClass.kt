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
class ActivityRestAPIClass {
    companion object Factory {
        val TAG = "RestAPIService"

        // get latest activity
        @JvmStatic
        fun updateActivity(next: String?, context: Context, completion: (refresh: Boolean, data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            var fullPath = UrlMakerSingleton.getInstance().activityUrl() ?: return

            var hasNext = false
            if (next != null && next != "") {
                fullPath = UrlMakerSingleton.getInstance().activityUrlWithNext(next) ?: return
                hasNext = true
            }

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = {authenticated, error ->
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

                                        completion(!hasNext, jobj, resCode)
                                    } catch (exc: JsonParseException) {
                                        completion(false, null, HTTPErrorType.UnKnown)
                                    }
                                }
                                HTTPErrorType.UnAuthorized, HTTPErrorType.ForbiddenAccess -> {
                                    TokenHandlerSingleton.getInstance(context).assureAuthorized(true, completion = {authenticated, error ->
                                        if (authenticated && error == HTTPErrorType.Success) {
                                            completion(false, null, HTTPErrorType.Refresh)
                                        }
                                    }, failure = { error ->
                                        failure(error)
                                    })
                                }
                                else -> completion(false, null, resCode)
                            }
                        }
                    })
                } else {
                    completion(false, null, error)
                }

            }, failure = { error ->
                failure(error)
            })
        }
    }
}