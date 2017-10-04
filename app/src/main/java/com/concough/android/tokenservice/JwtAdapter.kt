package com.concough.android.tokenservice

import android.util.Log
import com.concough.android.rest.AuthRestAPIClass
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
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Created by abolfazl on 7/5/17.
 */
class JwtAdapter {
    private interface JwtAdapterService {
        @POST()
        fun request(@Url url: String, @Body body: HashMap<String, Any>, @HeaderMap headers: HashMap<String, String>): Call<ResponseBody>
    }

    companion object Factory {
        val TAG = "JwtAdapter"

        // Jwt Token API
        @JvmStatic
        fun token(username: String, password: String, completion: (data: JsonObject?, statusCode: Int, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().jwtTokenUrl() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("username" to username, "password" to password)
            val headers = hashMapOf("Content-Type" to "application/json",
                    "Accept" to "application/json")

            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(JwtAdapter.JwtAdapterService::class.java)
            val request = auth.request(fullPath, parameters, headers)

            request.enqueue(object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    failure(NetworkErrorType.toType(t))
                }

                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    val resCode = HTTPErrorType.toType(response?.code()!!)
                    Log.d(AuthRestAPIClass.TAG, resCode.toString())
                    when (resCode) {
                        HTTPErrorType.Success -> {
                            val res = response.body()!!.string()
                            try {
                                val jobj = Gson().fromJson(res, JsonObject::class.java)

                                completion(jobj, response?.code()!!, resCode)


                            } catch (exc: JsonParseException) {
                                completion(null, 0, HTTPErrorType.UnKnown)
                            }
                        }
                        else -> completion(null, response?.code()!!, resCode)
                    }
                }
            })
        }

        // Jwt Refresh Token API
        @JvmStatic
        fun refreshToken(token: String, completion: (data: JsonObject?, statusCode: Int, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().jwtRefreshTokenUrl() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("token" to token)
            val headers = hashMapOf("Content-Type" to "application/json",
                    "Accept" to "application/json")

            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(JwtAdapter.JwtAdapterService::class.java)
            val request = auth.request(fullPath, parameters, headers)

            request.enqueue(object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    failure(NetworkErrorType.toType(t))
                }

                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    val resCode = HTTPErrorType.toType(response?.code()!!)
                    Log.d(AuthRestAPIClass.TAG, resCode.toString())
                    when (resCode) {
                        HTTPErrorType.Success -> {
                            val res = response.body()!!.string()
                            try {
                                val jobj = Gson().fromJson(res, JsonObject::class.java)

                                completion(jobj, response?.code()!!, resCode)


                            } catch (exc: JsonParseException) {
                                completion(null, 0, HTTPErrorType.UnKnown)
                            }
                        }
                        else -> completion(null, response?.code()!!, resCode)
                    }
                }
            })
        }

        // Jwt Refresh Token API
        @JvmStatic
        fun verify(token: String, completion: (data: JsonObject?, statusCode: Int, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().jwtVerifyTokenUrl() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("token" to token)
            val headers = hashMapOf("Content-Type" to "application/json",
                    "Accept" to "application/json")

            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(JwtAdapter.JwtAdapterService::class.java)
            val request = auth.request(fullPath, parameters, headers)

            request.enqueue(object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    failure(NetworkErrorType.toType(t))
                }

                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    val resCode = HTTPErrorType.toType(response?.code()!!)
                    Log.d(AuthRestAPIClass.TAG, resCode.toString())
                    when (resCode) {
                        HTTPErrorType.Success -> {
                            val res = response.body()!!.string()
                            try {
                                val jobj = Gson().fromJson(res, JsonObject::class.java)

                                completion(jobj, response?.code()!!, resCode)


                            } catch (exc: JsonParseException) {
                                completion(null, 0, HTTPErrorType.UnKnown)
                            }
                        }
                        else -> completion(null, response?.code()!!, resCode)
                    }
                }
            })
        }
    }
}