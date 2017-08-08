package com.concough.android.rest

import android.content.Context
import android.widget.ImageView
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.singletons.UrlMakerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.JsonObject
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.NetworkPolicy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException
import com.concough.android.concough.R.id.imageView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.CharBuffer
import java.util.stream.Stream

class MediaRestAPIClass {
    companion object Factory {
        val TAG = "BasketRestAPIClass"

        // Get Entrance Types
        @JvmStatic
        fun makeEsetImageUrl(imageId: Int): String? {
            return UrlMakerSingleton.getInstance().mediaForUrl("eset", imageId)
        }

        @JvmStatic
        fun downloadEsetImage(context: Context, imageId: Int, imageHolder: ImageView, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = makeEsetImageUrl(imageId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()
                    val headers2 = Headers.of(headers)

                    val okHttpClient = OkHttpClient.Builder().addInterceptor(object : Interceptor {

                        @Throws(IOException::class)
                        override fun intercept(chain: Interceptor.Chain): okhttp3.Response? {
                            var original = chain.request()

                            val newRequest = original.newBuilder()
                                    .headers(headers2)
                                    .method(original.method(), original.body())
                                    .build()
                            return chain.proceed(newRequest)
                        }
                    }).build()

                    try {
                        val builder = Picasso.Builder(context)
                        builder.downloader(OkHttp3Downloader(okHttpClient))
                        val built = builder.build()
//                        built.setIndicatorsEnabled(true)
//                        built.isLoggingEnabled = true
                        Picasso.setSingletonInstance(built)

                    } catch (exc: Exception) {}


                    Picasso.with(context)
                            .load(fullPath)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(imageHolder, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    completion(null, HTTPErrorType.Success)
                                }

                                override fun onError() {
                                    //Try again online if cache failed
                                    completion(null, HTTPErrorType.NotFound)
                                }
                            })

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

                    request.enqueue(object: Callback<ResponseBody> {
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
            }, failure = {error ->
                failure(error)
            })
        }
    }
}