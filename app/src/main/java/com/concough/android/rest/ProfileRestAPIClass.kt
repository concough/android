package com.concough.android.rest

import android.content.Context
import com.concough.android.settings.CONNECT_TIMEOUT
import com.concough.android.settings.READ_TIMEOUT
import com.concough.android.singletons.RetrofitSSLClientSingleton
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.singletons.UrlMakerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.structures.SignupMoreInfoStruct
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
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by abolfazl on 7/8/17.
 */
class ProfileRestAPIClass {
    companion object Factory {
        val TAG = "AuthRestAPIService"

        // Get Profile Data
        @JvmStatic
        fun getProfileData(context: Context, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().profileUrl() ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val client = RetrofitSSLClientSingleton.getInstance().getBuilder().build()
                    val Obj = Retrofit.Builder().baseUrl(fullPath).client(client).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.get(url = fullPath, headers = headers!!)

                    request.enqueue(object : Callback<ResponseBody> {
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
            }, failure = { error ->
                failure(error)
            })
        }


        // Get Grade List
        @JvmStatic
        fun getProfileGradeList(context: Context, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().gradeListProfileUrl() ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val client = RetrofitSSLClientSingleton.getInstance().getBuilder().build()
                    val Obj = Retrofit.Builder().baseUrl(fullPath).client(client).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.get(url = fullPath, headers = headers!!)

                    request.enqueue(object : Callback<ResponseBody> {
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
            }, failure = { error ->
                failure(error)
            })
        }


        // Post Profile Data
        @JvmStatic
        fun postProfileData(info: SignupMoreInfoStruct, context: Context, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().profileUrl() ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    var cal = Calendar.getInstance()
                    cal.time = info.birthday!!
                    val year = cal.get(Calendar.YEAR)

                    val parameters: HashMap<String, Any> = hashMapOf("firstname" to info.firstname!!,
                            "lastname" to info.lastname!!,
                            "grade" to info.grade!!,
                            "gender" to info.gender!!,
                            "byear" to year,
                            "bmonth" to info.birthday?.month!!,
                            "bday" to info.birthday?.day!!)

                    val client = RetrofitSSLClientSingleton.getInstance().getBuilder().build()
                    val Obj = Retrofit.Builder().baseUrl(fullPath).client(client).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.post(fullPath, parameters, headers!!)

                    request.enqueue(object : Callback<ResponseBody> {
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
            }, failure = { error ->
                failure(error)
            })
        }


        @JvmStatic
        fun putProfileGrade(grade: String, context: Context, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().editGradeProfileUrl() ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val parameters: HashMap<String, Any> = hashMapOf(
                            "grade" to grade
                    )

                    val client = RetrofitSSLClientSingleton.getInstance().getBuilder().build()
                    val Obj = Retrofit.Builder().baseUrl(fullPath).client(client).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.put(fullPath, parameters, headers!!)

                    request.enqueue(object : Callback<ResponseBody> {
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
            }, failure = { error ->
                failure(error)
            })
        }


    }
}