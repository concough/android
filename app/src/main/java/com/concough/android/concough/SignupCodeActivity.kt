package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import com.concough.android.rest.AuthRestAPIClass
import com.concough.android.settings.PASSWORD_KEY
import com.concough.android.settings.USERNAME_KEY
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.structures.SignupStruct
import com.concough.android.utils.KeyChainAccessProxy
import kotlinx.android.synthetic.main.activity_signup_code.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SignupCodeActivity : AppCompatActivity() {
    private var signupStruct: SignupStruct? = null
    private var fromWhichActivity: String? = null

    companion object {
        private val TAG = "SignupCodeActivity"
        private val FROM_ACTIVITY_KEY = "FromA"
        private val SIGNUP_STRUCTURE_KEY = "SignupS"

        @JvmStatic
        fun newIntent(packageContext: Context, fromA: String, signupStruct: SignupStruct?): Intent {
            val i = Intent(packageContext, SignupCodeActivity::class.java)

            i.putExtra(FROM_ACTIVITY_KEY, fromA)
            i.putExtra(SIGNUP_STRUCTURE_KEY, signupStruct)
            return i
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_code)

        // Hiding Action Bar
        supportActionBar?.hide()

        this.fromWhichActivity = intent.getStringExtra(FROM_ACTIVITY_KEY)
        this.signupStruct = intent.getSerializableExtra(SIGNUP_STRUCTURE_KEY) as SignupStruct?

        Log.d(TAG, fromWhichActivity)
        Log.d(TAG, signupStruct.toString())

        SignupCodeA_titleTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Bold
        SignupCodeA_codeEditText.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        SignupCodeA_SubmitButton.typeface = FontCacheSingleton.getInstance(applicationContext).Regular
        SignupCodeA_ResendButton.typeface = FontCacheSingleton.getInstance(applicationContext).Regular
        SignupCodeA_infoTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Regular

        SignupCodeA_codeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length > 0) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        SignupCodeA_codeEditText.textDirection = View.TEXT_DIRECTION_LTR
                        SignupCodeA_codeEditText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    } else {
                        SignupCodeA_codeEditText.gravity = Gravity.CENTER
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        SignupCodeA_codeEditText.textDirection = View.TEXT_DIRECTION_RTL
                        SignupCodeA_codeEditText.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    } else {
                        SignupCodeA_codeEditText.gravity = Gravity.START
                    }
                }

            }
        })

        SignupCodeA_ResendButton.setOnClickListener {
            when(this@SignupCodeActivity.fromWhichActivity) {
                "SignupA" -> makePreSignup()
                else -> return@setOnClickListener
            }
        }

        SignupCodeA_SubmitButton.setOnClickListener {
            when(this@SignupCodeActivity.fromWhichActivity) {
                "SignupA" -> sendPreSignupCode()
                else -> return@setOnClickListener
            }
        }
    }

    private fun sendPreSignupCode() {
        val code: String = SignupCodeA_codeEditText.text.trim().toString()
        if (code != "") {
            try {
                val intCode = code.toInt()

                // TODO: show loading in view
                doAsync {

                    AuthRestAPIClass.signup(this@SignupCodeActivity.signupStruct?.username!!, this@SignupCodeActivity.signupStruct?.preSignupId!!, intCode, { data, error ->
                        uiThread {
                            // TODO: hide loading that showed before
                            if (error == HTTPErrorType.Success) {
                                if (data != null) {
                                    try {
                                        val status = data.get("status").asString
                                        when (status) {
                                            "OK" -> {
                                                this@SignupCodeActivity.signupStruct?.password = code
                                                // TODO: make login request
                                                this@SignupCodeActivity.login()

                                            }
                                            "Error" -> {
                                                val errorType = data.get("error_type").asString
                                                when (errorType) {
                                                    "ExistUsername", "PreAuthNoExist" -> {
                                                        // TODO: show simple error message with messageType = "AuthProfile"
                                                    }
                                                    "ExpiredCode" -> {
                                                        // TODO: show simple error message with messageType = "ErrorResult"
                                                    }
                                                }
                                            }
                                        }

                                    } catch (exc: Exception) {

                                    }
                                }
                            } else {
                                // TODO: show top message with type = error
                            }
                        }
                    }, { error ->
                        uiThread {
                            // TODO: hide loading that showed before
                            if (error != null) {
                                when (error) {
                                    NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
                                        // TODO: show top message about error with type error
                                    }
                                    else -> {
                                        // TODO: Show top message about error without type
                                    }
                                }
                            }

                        }
                    })
                }

            } catch (ext: Exception) {

            }
        }
    }

    private fun makePreSignup() {
        val username = this@SignupCodeActivity.signupStruct?.username ?: return

        // TODO: show loading in view
        doAsync {

            AuthRestAPIClass.preSignup(username, { data, error ->
                uiThread {
                    // TODO: hide loading that showed before
                    if (error == HTTPErrorType.Success) {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val id = data.get("id").asInt
                                        this@SignupCodeActivity.signupStruct?.preSignupId = id

                                        // TODO: Show message to user about resending successful msgType = "ActionResult" and msgSubType = "ResendCodeSuccessful"

                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "ExistUsername" -> {

                                                // TODO: show simple error message with messageType = "AuthProfile"
                                            }
                                            else -> {
                                                // TODO: show simple error message with messageType = "ErrorResult"
                                            }
                                        }
                                    }
                                }

                            } catch (exc: Exception) {

                            }
                        }
                    } else {
                        // TODO: show top message with type = error
                    }
                }
            }, { error ->
                uiThread {
                    // TODO: hide loading that showed before
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
                                // TODO: show top message about error with type error
                            }
                            else -> {
                                // TODO: Show top message about error without type
                            }
                        }
                    }

                }
            })
        }
    }

    private fun login() {
        // Set Username and password
        TokenHandlerSingleton.getInstance(applicationContext).setUsernameAndPassword(this@SignupCodeActivity.signupStruct?.username!!, this@SignupCodeActivity.signupStruct?.password!!)

        // TODO: show loading in view
        doAsync {
            TokenHandlerSingleton.getInstance(applicationContext).authorize({ error ->
                uiThread {
                    // TODO: hide loading that showed before
                    if (error == HTTPErrorType.Success) {
                        if (TokenHandlerSingleton.getInstance(applicationContext).isAuthorized()) {
                            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString(USERNAME_KEY, this@SignupCodeActivity.signupStruct?.username!!)
                            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString(PASSWORD_KEY, this@SignupCodeActivity.signupStruct?.password!!)

                            // Navigate to Signup More Info Activity
                            val moreInfoIntent =  SignupMoreInfo1Activity.newIntent(this@SignupCodeActivity)
                            startActivity(moreInfoIntent)
                            this@SignupCodeActivity.finish()

                        } else {
                            // Navigate to LoginActivity
                            val i = LoginActivity.newIntent(this@SignupCodeActivity)
                            startActivity(i)
                        }
                    } else {
                        // TODO: Show Simple message box "HTTPError" and in completion navigate to login activity
                        // Navigate to LoginActivity
                        val i = LoginActivity.newIntent(this@SignupCodeActivity)
                        startActivity(i)
                    }
                }
            }, { error ->

            })
        }
    }

}
