package com.concough.android.rest

import android.content.Context
import android.widget.ImageView
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.singletons.UrlMakerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.jakewharton.picasso.OkHttp3Downloader
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.squareup.picasso.OkHttpDownloader
import com.squareup.picasso.Picasso
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.IOException


/**
 * Created by abolfazl on 7/11/17.
 */
class MediaRestAPIClass {
    companion object Factory {
        val TAG = "BasketRestAPIClass"

        // Get Entrance Types
        @JvmStatic
        fun makeEsetImageUrl(imageId: Int): String? {
            return UrlMakerSingleton.getInstance().mediaForUrl("eset", imageId)
        }

        @JvmStatic
        fun downloadEsetImage(context: Context, imageId: Int, imageHolder: ImageView,  completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
            val fullPath = makeEsetImageUrl(imageId) ?: return

            TokenHandlerSingleton.getInstance(context).assureAuthorized(completion = { authenticated, error ->
                if (authenticated && error == HTTPErrorType.Success) {
                    val headers = TokenHandlerSingleton.getInstance(context).getHeader()
                    val headers2 = Headers.of(headers)

                    val okHttpClient = OkHttpClient.Builder().addInterceptor (object : Interceptor {

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

                    Picasso.Builder(context).downloader(OkHttp3Downloader(okHttpClient)).build().load(fullPath).into(imageHolder)
                    completion(null, HTTPErrorType.Success)
               } else {
                    completion(null, error)
                }
            }, failure = {error ->
                failure(error)
            })
        }

    }
}