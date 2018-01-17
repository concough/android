package com.concough.android.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by abolfazl on 7/8/17.
 */
interface RestAPIService {
    @POST()
    fun post(@Url url: String, @Body body: HashMap<String, Any>, @HeaderMap headers: HashMap<String, String>): Call<ResponseBody>

    @PUT
    fun put(@Url url: String, @Body body: HashMap<String, Any>, @HeaderMap headers: HashMap<String, String>): Call<ResponseBody>


    @GET()
    fun get(@Url url: String, @HeaderMap headers: HashMap<String, String>): Call<ResponseBody>

    @GET()
//    @FormUrlEncoded
    fun getWithParams(@Url url: String, @QueryMap params: HashMap<String, Any>?, @HeaderMap headers: HashMap<String, String>): Call<ResponseBody>

    @DELETE
    fun delete(@Url url: String, @HeaderMap headers: HashMap<String, String>): Call<ResponseBody>
}