package com.concough.android.rest

import android.util.Log
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
import retrofit2.http.*
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by abolfazl on 7/2/17.
 */
class AuthRestAPIClass {
    private interface AuthRestAPIService {
        @POST()
        fun checkUsername(@Url url: String, @Body body: HashMap<String, Any>, @HeaderMap headers: HashMap<String, String>): Call<ResponseBody>
    }

    companion object Factory {
        val TAG = "AuthRestAPIService"

        @JvmStatic
        fun checkUsername(username: String, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().checkUsernameUrl() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("username" to username)
            val headers = hashMapOf("Content-Type" to "application/json",
                                    "Accept" to "application/json")

            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(AuthRestAPIService::class.java)
            val request = auth.checkUsername(fullPath, parameters, headers)

            request.enqueue(object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    failure(NetworkErrorType.toType(t))
                }

                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    val resCode = HTTPErrorType.toType(response?.code()!!)
                    Log.d(TAG, resCode.toString())
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
                        else -> completion(null, resCode)
                    }
                }
            })

        }
    }
}