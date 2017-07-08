package com.concough.android.singletons

import android.content.Context
import android.icu.util.Calendar
import com.concough.android.settings.*
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.tokenservice.JwtAdapter
import com.concough.android.utils.JwtHandler
import com.concough.android.utils.KeyChainAccessProxy
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by abolfazl on 7/4/17.
 */
class TokenHandlerSingleton {
    private var _token: String? = null
    private var _refreshToken: String? = null
    private var _username: String? = null
    private var _password: String? = null
    private var _tokenType: String? = "Bearer"
    private var _oauthMethod: String = OAUTH_METHOD
    private var _lastTime: Date? = null
    private var _expiresIn: Date? = null

    private var _context: Context


    companion object Factory {
        private var sharedInstance : TokenHandlerSingleton? = null

        @JvmStatic
        fun  getInstance(context: Context): TokenHandlerSingleton {
            if (sharedInstance == null)
                sharedInstance = TokenHandlerSingleton(context)

            return sharedInstance!!
        }
    }

    private constructor(context: Context) {
        this._context = context

        if (this._oauthMethod == "jwt") {
            val token = KeyChainAccessProxy.getInstance(context).getValueAsString(OAUTH_TOKEN_KEY)
            val rtoken = KeyChainAccessProxy.getInstance(context).getValueAsString(OAUTH_REFRESH_TOKEN_KEY)
            val username = KeyChainAccessProxy.getInstance(context).getValueAsString(USERNAME_KEY)
            val password = KeyChainAccessProxy.getInstance(context).getValueAsString(PASSWORD_KEY)
            val lastTime = KeyChainAccessProxy.getInstance(context).getValueAsString(OAUTH_LAST_ACCESS_KEY)
            val expiresIn = KeyChainAccessProxy.getInstance(context).getValueAsString(OAUTH_EXPIRES_IN_KEY)
            val tokenType = KeyChainAccessProxy.getInstance(context).getValueAsString(OAUTH_TOKEN_TYPE_KEY)

            if (token != "") this._token = token
            if (rtoken != "") this._refreshToken = rtoken
            if (username != "") this._username = username
            if (password != "") this._password = password

            if (expiresIn != "") {
                this._expiresIn = FormatterSingleton.getInstance().UTCDateFormatter.parse(expiresIn)
            }
            if (lastTime != "") {
                this._lastTime = FormatterSingleton.getInstance().UTCDateFormatter.parse(lastTime)
            }

            if (tokenType != "") this._tokenType = tokenType
        }
    }

    public fun authorize(completion: (error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit) {
        if (this._oauthMethod == "jwt") {
            JwtAdapter.token(username = this._username!!, password = this._password!!, completion = { data, statusCode, error ->
                if (statusCode == 200) {
                    if (data != null) {
                        val token = data.get("token").asString
                        if (token != "") {
                            this._token = token
                            this._tokenType = "JWT"

                            try {
                                val payload = JwtHandler.getPeyloadData(token)

                                this._lastTime = payload.issuedAt
                                this._expiresIn = payload.expiresAt

                                KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_TOKEN_KEY, token)
                                KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_TOKEN_TYPE_KEY, this._tokenType!!)

                                if (this._lastTime != null) KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_LAST_ACCESS_KEY, FormatterSingleton.getInstance().UTCDateFormatter.format(this._lastTime))
                                if (this._expiresIn != null) KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_EXPIRES_IN_KEY, FormatterSingleton.getInstance().UTCDateFormatter.format(this._expiresIn))

                                completion(HTTPErrorType.Success)
                            } catch (exc: Exception) {

                            }

                        }
                    }
                    completion(HTTPErrorType.UnKnown)
                }
                completion(error)
            }, failure = { error ->
                failure(error)
            })
        }
    }

    public fun refreshToken(completion: (error: HTTPErrorType?) -> Unit, failure: (error: NetworkErrorType?) -> Unit) {
        if (this._oauthMethod == "jwt") {
            JwtAdapter.refreshToken(token = this._token!!, completion = { data, statusCode, error ->
                if (statusCode == 200) {
                    if (data != null) {
                        val token = data.get("token").asString
                        if (token != "") {
                            this._token = token
                            this._tokenType = "JWT"

                            try {
                                val payload = JwtHandler.getPeyloadData(token)

                                this._lastTime = payload.issuedAt
                                this._expiresIn = payload.expiresAt

                                KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_LAST_ACCESS_KEY, FormatterSingleton.getInstance().UTCDateFormatter.format(this._lastTime))
                                KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_EXPIRES_IN_KEY, FormatterSingleton.getInstance().UTCDateFormatter.format(this._expiresIn))

                                completion(HTTPErrorType.Success)
                            } catch (exc: Exception) {

                            }

                            KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_TOKEN_KEY, token)
                            KeyChainAccessProxy.getInstance(this._context).setValueAsString(OAUTH_TOKEN_TYPE_KEY, this._tokenType!!)

                        }
                    }
                    completion(HTTPErrorType.UnKnown)
                }
                completion(error)
            }, failure = { error ->
                failure(error)
            })
        }
    }

    public fun setUsernameAndPassword(username: String, password: String) {
        this._username = username
        this._password = password
    }

    public fun getUsername(): String? = this._username
    public fun getPassword(): String? = this._password

    public fun invalidateTokens() {
        this._token = null
        this._refreshToken = null
        this._username = null
        this._password = null

        KeyChainAccessProxy.getInstance(this._context).clearAllValue()
    }

    public fun getHeader(): HashMap<String, String>? {
        if (this._token != null) {
            val map = hashMapOf<String, String>("Authorization" to "${this._tokenType} ${this._token}")
            return map
        }
        return  null
    }

    public fun isAuthorized(): Boolean {
        if (this._oauthMethod == "oauth") {
            return (this._token != null && this._refreshToken != null)
        }
        return this._token != null
    }

    public fun isAuthenticated(): Boolean {
        return (this._username != null && this._password != null)
    }

    public fun assureAuthorized(refresh: Boolean = false, completion: (authenticated: Boolean, error: HTTPErrorType?) -> Unit,  failure: (error: NetworkErrorType?) -> Unit) {
        if (this.isAuthorized()) {
            if (refresh) {
                this.refreshToken(completion = { error ->
                    if (error == HTTPErrorType.Success) {
                        completion(true, error)
                    } else {
                        if (error == HTTPErrorType.BadRequest || error == HTTPErrorType.ServerInternalError) {
                            this.authorize(completion = {error ->
                                if (error == HTTPErrorType.Success) completion(true, error)
                                else {
                                        UserDefaultsSingleton.getInstance(_context).clearAll()
                                        KeyChainAccessProxy.getInstance(this._context).clearAllValue()
                                        completion(false, error)
                                }
                            }, failure = {error ->
                                failure(error)
                            })
                            completion(false, error)
                        }
                    }
                }, failure = {error ->
                    failure(error)
                })
            } else {
//                if (this._lastTime != null && this._expiresIn != null) {
                if (this._expiresIn != null) {
                    if (Date() > this._expiresIn) {
                        this.refreshToken(completion = {error ->
                            if (error == HTTPErrorType.Success) {
                                completion(true, error)
                            } else {
                                if (error == HTTPErrorType.BadRequest || error == HTTPErrorType.ServerInternalError) {
                                    this.authorize(completion = {error ->
                                        if (error == HTTPErrorType.Success) completion(true, error)
                                        else {
                                            UserDefaultsSingleton.getInstance(_context).clearAll()
                                            KeyChainAccessProxy.getInstance(this._context).clearAllValue()
                                            completion(false, error)
                                        }
                                    }, failure = {error ->
                                        failure(error)
                                    })
                                    completion(false, error)
                                }
                            }
                        }, failure = {error ->
                            failure(error)

                        })
                    } else {
                        completion(true, HTTPErrorType.Success)
                    }
                }
            }

        } else {
            if (this.isAuthenticated()) {
                this.authorize(completion = { error ->
                    if (error == HTTPErrorType.Success) {
                        completion(true, error)
                    } else {
                        completion(false, error)
                    }
                }, failure = {error ->
                    failure(error)
                })
            } else {
                completion(false, null)
            }
        }
    }
}