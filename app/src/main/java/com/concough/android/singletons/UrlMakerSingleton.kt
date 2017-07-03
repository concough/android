package com.concough.android.singletons

import android.content.Context
import com.concough.android.settings.*

/**
 * Created by abolfazl on 7/2/17.
 */
class UrlMakerSingleton private constructor(){
    private val _base_url: String = BASE_URL
    private val _api_version: String = API_VERSION
    private val _jwt_prefix: String = JWT_URL_PREFIX

    private val _oauth_class_name: String = OAUTH_CLASS_NAME
    private val _jauth_class_name: String = JAUTH_CLASS_NAME
    private val _auth_class_name: String = AUTH_CLASS_NAME
    private val _profile_class_name = PROFILE_CLASS_NAME

    companion object Factory {
        private var sharedInstance : UrlMakerSingleton? = null

        @JvmStatic
        fun  getInstance(): UrlMakerSingleton {
            if (sharedInstance == null)
                sharedInstance = UrlMakerSingleton()

            return sharedInstance!!
        }

    }
    fun checkUsernameUrl(): String? {
        var fullPath: String? = null
        val functionName = "check_username"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._auth_class_name}/$functionName/"
        }
        return fullPath
    }
}