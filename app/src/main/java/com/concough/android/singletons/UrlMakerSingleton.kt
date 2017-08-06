package com.concough.android.singletons

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
    private val _activity_class_name = ACTIVITY_CLASS_NAME
    private val _basket_class_name = BASKET_CLASS_NAME
    private val _archive_class_name = ARCHIVE_CLASS_NAME
    private val _media_class_name = MEDIA_CLASS_NAME

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

    fun preSignupUrl(): String? {
        var fullPath: String? = null
        val functionName = "pre_signup"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._auth_class_name}/$functionName/"
        }
        return fullPath
    }

    fun signupUrl(): String? {
        var fullPath: String? = null
        val functionName = "signup"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._auth_class_name}/$functionName/"
        }
        return fullPath
    }

    fun forgotPassword(): String? {
        var fullPath: String? = null
        val functionName = "forgot_password"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._auth_class_name}/$functionName/"
        }
        return fullPath
    }

    fun resetPassword(): String? {
        var fullPath: String? = null
        val functionName = "reset_password"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._auth_class_name}/$functionName/"
        }
        return fullPath
    }

    fun changePassword(): String? {
        var fullPath: String? = null
        val functionName = "change_password"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._auth_class_name}/$functionName/"
        }
        return fullPath
    }

    fun jwtTokenUrl(): String? {
        var fullPath: String? = null
        val functionName = "token"

        fullPath = "${this._base_url}${this._api_version}/${this._jauth_class_name}/$functionName/"
        return fullPath
    }

    fun jwtRefreshTokenUrl(): String? {
        var fullPath: String? = null
        val functionName = "refresh_token"

        fullPath = "${this._base_url}${this._api_version}/${this._jauth_class_name}/$functionName/"
        return fullPath
    }

    fun jwtVerifyTokenUrl(): String? {
        var fullPath: String? = null
        val functionName = "verify"

        fullPath = "${this._base_url}${this._api_version}/${this._jauth_class_name}/$functionName/"
        return fullPath
    }

    fun profileUrl(): String? {
        var fullPath: String? = null

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._profile_class_name}/"
        }
        return fullPath
    }

    fun activityUrl(): String? {
        var fullPath: String? = null

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._activity_class_name}/"
        }
        return fullPath
    }

    fun activityUrlWithNext(next: String): String? {
        var fullPath: String? = UrlMakerSingleton.getInstance().activityUrl()
        if (fullPath != null) {
            fullPath += "next/$next/"
        }

        return fullPath
    }

    private fun getBasketUrl(functionName: String): String? {
        var fullPath: String? = null

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._basket_class_name}/$functionName/"
        }
        return fullPath
    }

    fun getLoadBasketItemsUrl(): String? {
        val functionName = "list"
        return this.getBasketUrl(functionName)
    }

    fun getCreateBasketUrl(): String? {
        val functionName = "create"
        return this.getBasketUrl(functionName)
    }

    fun getAddToBasketUrl(basketId: String): String? {
        val functionName = "$basketId/add"
        return this.getBasketUrl(functionName)
    }

    fun getRemoveSaleFromBasketUrl(basketId: String, saleId: Int): String? {
        val functionName = "$basketId/sale/$saleId"
        return this.getBasketUrl(functionName)
    }

    fun getCheckoutBasketUrl(basketId: String): String? {
        val functionName = "$basketId/checkout"
        return this.getBasketUrl(functionName)
    }

    fun archiveEntranceTypesUrl(): String? {
        var fullPath: String? = null
        val functionName = "entrance/types"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._archive_class_name}/$functionName/"
        }
        return fullPath
    }

    fun archiveEntranceGroupsUrl(etypeId: Int): String? {
        var fullPath: String? = null
        val functionName = "entrance/groups/$etypeId"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._archive_class_name}/$functionName/"
        }
        return fullPath
    }

    fun archiveEntranceSetsUrl(egroupId: Int): String? {
        var fullPath: String? = null
        val functionName = "entrance/sets/$egroupId"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._archive_class_name}/$functionName/"
        }
        return fullPath
    }

    fun archiveEntranceUrl(esetId: Int): String? {
        var fullPath: String? = null
        val functionName = "entrance/$esetId"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._archive_class_name}/$functionName/"
        }
        return fullPath
    }

    fun mediaForUrl(type: String, mediaId: Any): String? {
        var fullPath: String? = null
        val functionName = "$type/$mediaId"

        if (OAUTH_METHOD == "jwt") {
            fullPath = "${this._base_url}${this._api_version}/${this._jwt_prefix}/${this._media_class_name}/$functionName/"
        }
        return fullPath
    }

}