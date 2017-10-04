package com.concough.android.rest

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.singletons.UrlMakerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import okhttp3.ResponseBody
import org.jetbrains.anko.runOnUiThread
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
        fun downloadEsetImage(context: Context, imageId: Int, imageHolder: ImageView, completion: (data: JsonObject?, error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit): Unit {
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

//                                        val line = reader.read()
//                                        sb.append(line)

                                        context.runOnUiThread {
                                            if(imageHolder!=null) {

                                            }
                                            Glide.with(context)

                                                    .load(sb)

                                                    .listener(object : RequestListener<ByteArray, GlideDrawable> {
                                                        override fun onException(e: Exception, model: ByteArray, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                                                            Log.d("GLIDE", "First Exeption : "+ e.toString())
                                                            completion(null, HTTPErrorType.NotFound)
                                                            return false
                                                        }

                                                        override fun onResourceReady(resource: GlideDrawable, model: ByteArray, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                                            Log.d("GLIDE", "onResourceReady")
                                                            completion(null, HTTPErrorType.Success)
                                                            return false
                                                        }

                                                    })
                                                    .crossFade()
//                                                .fitCenter()
                                                    .into(imageHolder)

                                        }


                                    } catch (exc: JsonParseException) {
                                        completion(null, HTTPErrorType.UnKnown)
                                        Log.d("GLIDE", "JsonParseException")

                                    } catch (exc: Exception) {
                                        completion(null, HTTPErrorType.UnKnown)
                                        Log.d("GLIDE", "Exception")

                                    } catch (exc: java.lang.Exception) {
                                        completion(null, HTTPErrorType.UnKnown)
                                        Log.d("GLIDE", "java.lang.Exception")

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
    }
}