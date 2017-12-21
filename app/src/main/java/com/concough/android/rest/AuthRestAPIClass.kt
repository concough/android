package com.concough.android.rest

import android.content.Context
import android.util.Log
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.singletons.UrlMakerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by abolfazl on 7/2/17.
 */
class AuthRestAPIClass {

    companion object Factory {
        val TAG = "RestAPIService"

        // Check Username API Call
        @JvmStatic
        fun checkUsername(username: String, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().checkUsernameUrl() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("username" to username)
            val headers = hashMapOf("Content-Type" to "application/json",
                                    "Accept" to "application/json")


            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(RestAPIService::class.java)
            val request = auth.post(fullPath, parameters, headers)

            request.enqueue(object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    failure(NetworkErrorType.toType(t))
                }

                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    val resCode = HTTPErrorType.toType(response?.code()!!)
//                    Log.d(TAG, resCode.toString())
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

        // Pre Signup Phase API
        @JvmStatic
        fun preSignup(username: String, send_type: String, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().preSignupUrl() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("username" to username, "type" to send_type)
            val headers = hashMapOf("Content-Type" to "application/json",
                    "Accept" to "application/json")

            val okHttpClient = OkHttpClient.Builder()
                    .readTimeout(60,TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build()

            val Obj = Retrofit.Builder().client(okHttpClient).baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(RestAPIService::class.java)
            val request = auth.post(fullPath, parameters, headers)

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

        // Signup Phase API -> Send Code to Server
        @JvmStatic
        fun signup(username: String, id: Int, code: Int, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().signupUrl() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("username" to username, "id" to id, "code" to code)
            val headers = hashMapOf("Content-Type" to "application/json",
                    "Accept" to "application/json")

            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(RestAPIService::class.java)
            val request = auth.post(fullPath, parameters, headers)

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

        // Forgot Password Phase for UnAuthenticated User
        @JvmStatic
        fun forgotPassword(username: String, send_type: String, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().forgotPassword() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("username" to username, "type" to send_type)
            val headers = hashMapOf("Content-Type" to "application/json",
                    "Accept" to "application/json")

            val okHttpClient = OkHttpClient.Builder()
                    .readTimeout(60,TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build()

            val Obj = Retrofit.Builder().client(okHttpClient).baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(RestAPIService::class.java)
            val request = auth.post(fullPath, parameters, headers)

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

        // Forgot Password Phase for UnAuthenticated User
        @JvmStatic
        fun resetPassword(username: String, id: Int, password: String, rpassword: String, code: Int, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().resetPassword() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("username" to username,
                                                            "password" to password,
                                                            "rpassword" to rpassword,
                                                            "id" to id,
                                                            "code" to code)
            val headers = hashMapOf("Content-Type" to "application/json",
                    "Accept" to "application/json")

            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(RestAPIService::class.java)
            val request = auth.post(fullPath, parameters, headers)

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

        @JvmStatic
        fun changePassword(pass1: String, pass2: String, context: Context, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().changePassword() ?: return

            val parameters: HashMap<String, Any> = hashMapOf("oldPass" to pass1, "newPass" to pass2)
            val headers = TokenHandlerSingleton.getInstance(context).getHeader()

            val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
            val auth = Obj.create(RestAPIService::class.java)
            val request = auth.post(fullPath, parameters, headers!!)

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