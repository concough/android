package com.concough.android.rest

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.singletons.UrlMakerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.JsonParseException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MediaRestAPIClass {
    companion object Factory {
        val TAG = "BasketRestAPIClass"

        // Get Entrance Types
        @JvmStatic
        fun makeEsetImageUrl(imageId: Int): String? {
            return UrlMakerSingleton.getInstance().mediaForUrl("eset", imageId)
        }

        @JvmStatic
        fun downloadEsetImage(context: Context, imageId: Int, imageHolder: ImageView, completion: (data: ByteArray?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = makeEsetImageUrl(imageId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()


                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
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
                                    val res = response.body()
                                    try {
//                                        val reader: BufferedReader = BufferedReader(InputStreamReader(res?.byteStream()))
                                        val sb: ByteArray = res?.bytes()!!
                                        completion(sb, HTTPErrorType.Success)
//                                        val line = reader.read()
//                                        sb.append(line)



                                    } catch (exc: Exception) {
                                        completion(null, HTTPErrorType.UnKnown)
                                        Log.d("GLIDE", "Exception")
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


//                    val headers2 = Headers.of(headers)
//                    Log.d("HEADER REST Img:", headers.toString())
//
//
//                    val okHttpClient = OkHttpClient.Builder().addInterceptor(object : Interceptor {
//
//                        @Throws(IOException::class)
//                        override fun intercept(chain: Interceptor.Chain): okhttp3.Response? {
//                            var original = chain.request()
//
//                            val newRequest = original.newBuilder()
//                                    .headers(headers2)
//                                    .method(original.method(), original.body())
//                                    .cacheControl(CacheControl.FORCE_NETWORK)
//                                    .addHeader("Cache-Control", "no-cache")
//                                    .build()
//                            return chain.proceed(newRequest)
//                        }
//                    })
//                            .cache(null)
//                            .build()
//
//
//                    try {
//                        val builder = Picasso.Builder(context)
//                        builder.downloader(OkHttp3Downloader(okHttpClient))
//                        builder.listener { picasso, uri, exception ->
//                            Log.d("Picaso B Exc:", exception.toString())
//
//                            if (exception is Downloader.ResponseException) {
//                                Log.d("Picaso ResponseExc", "URI:" + uri)
//                            }
//
//                            Picasso.with(context).invalidate(uri)
////                            picasso.invalidate(uri)
//                        }
//
//
//                        val built = builder.build()
////                        built.setIndicatorsEnabled(true)
////                        built.isLoggingEnabled = true
//                        Picasso.setSingletonInstance(built)
//
//                    } catch (exc: Exception) {
//                    }
//
//
//                    Picasso.with(context)
//                            .load(fullPath)
//                            .networkPolicy(NetworkPolicy.NO_CACHE)
//                            .memoryPolicy(MemoryPolicy.NO_CACHE)
//                            .into(imageHolder, object : com.squareup.picasso.Callback {
//                                override fun onSuccess() {
//                                    completion(null, HTTPErrorType.Success)
//                                }
//
//                                override fun onError() {
//                                    //Try again online if cache failed
//                                    completion(null, HTTPErrorType.NotFound)
//                                }
//                            })

//                    Picasso.with(context).load(fullPath).networkPolicy(NetworkPolicy.OFFLINE)
                } else {
                    completion(null, error)
                }
            }, failure = { error ->
                failure(error)
            })
        }

        @JvmStatic
        fun downloadEntranceQuestionImage(context: Context, uniqueId: String, imageId: String, completion: (data: ByteArray?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().mediaForQuestionUrl(uniqueId, imageId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
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
                                    val res = response.body()
                                    try {
//                                        val reader: BufferedReader = BufferedReader(InputStreamReader(res?.byteStream()))
                                        val sb: ByteArray = res?.bytes()!!

//                                        val line = reader.read()
//                                        sb.append(line)

                                        completion(sb, resCode)
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
        fun downloadEntranceQuestionBulkImages(context: Context, uniqueId: String, questionsId: Array<String>, completion: (data: ByteArray?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = UrlMakerSingleton.getInstance().mediaForBulkQuestionUrl(uniqueId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()

                    var parameters = HashMap<String, Any>()
                    var query = ""
                    for ((index, element) in questionsId.withIndex()) {
                        if (index != questionsId.count() - 1) {
                            query += "$element$"
                        } else {
                            query += element
                        }
                    }
                    parameters.set("ids", query)

                    val Obj = Retrofit.Builder().baseUrl(fullPath).addConverterFactory(GsonConverterFactory.create()).build()
                    val profile = Obj.create(RestAPIService::class.java)
                    val request = profile.getWithParams(fullPath, parameters, headers!!)

                    request.enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            failure(NetworkErrorType.toType(t))
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            val resCode = HTTPErrorType.toType(response?.code()!!)
//                          Log.d(TAG, resCode.toString())
                            when (resCode) {
                                HTTPErrorType.Success -> {
                                    val res = response.body()
                                    try {
//                                        val reader: BufferedReader = BufferedReader(InputStreamReader(res?.byteStream()))
                                        val sb: ByteArray = res?.bytes()!!

//                                        val line = reader.read()
//                                        sb.append(line)

                                        completion(sb, resCode)
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