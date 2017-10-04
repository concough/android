package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import com.concough.android.general.AlertClass
import com.concough.android.rest.AuthRestAPIClass
import com.concough.android.settings.PASSWORD_KEY
import com.concough.android.settings.USERNAME_KEY
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.singletons.TokenHandlerSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.structures.SignupStruct
import com.concough.android.utils.KeyChainAccessProxy
import com.concough.android.vendor.progressHUD.KProgressHUD
import kotlinx.android.synthetic.main.activity_signup_code.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SignupCodeActivity : AppCompatActivity() {
    private var signupStruct: SignupStruct? = null
    private var fromWhichActivity: String? = null
    private var loadingProgress: KProgressHUD? = null


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
            when (this@SignupCodeActivity.fromWhichActivity) {
                "SignupA" -> makePreSignup()
                "ForgotPassA" -> makeForgotPass()
                else -> return@setOnClickListener
            }
        }

        SignupCodeA_SubmitButton.setOnClickListener {
            when (this@SignupCodeActivity.fromWhichActivity) {
                "SignupA" -> sendPreSignupCode()
                "ForgotPassA" -> sendForgotPassCode()
                else -> return@setOnClickListener
            }
        }
    }

    private fun sendPreSignupCode() {
        val code: String = SignupCodeA_codeEditText.text.trim().toString()
        if (code != "") {
            var intCode: Int? = null
            try {
                intCode = code.toInt()
            } catch (ext: Exception) {

            }
            if (intCode == null) {
                AlertClass.showTopMessage(this@SignupCodeActivity,findViewById(R.id.container),"Form","CodeWrong","error",null)
                return
            }

            loadingProgress = AlertClass.showLoadingMessage(this@SignupCodeActivity)
            loadingProgress?.show()
            doAsync {

                AuthRestAPIClass.signup(this@SignupCodeActivity.signupStruct?.username!!, this@SignupCodeActivity.signupStruct?.preSignupId!!, intCode!!, { data, error ->
                    uiThread {
                        AlertClass.hideLoadingMessage(loadingProgress)
                        if (error == HTTPErrorType.Success) {
                            if (data != null) {
                                try {
                                    val status = data.get("status").asString
                                    when (status) {
                                        "OK" -> {
                                            this@SignupCodeActivity.signupStruct?.password = code
                                            this@SignupCodeActivity.login()

                                        }
                                        "Error" -> {
                                            val errorType = data.get("error_type").asString
                                            when (errorType) {
                                                "ExistUsername", "PreAuthNoExist" -> {
                                                    AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "AuthProfile", errorType, "", null)
                                                }
                                                "ExpiredCode" -> {
                                                    AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "ErrorResult", errorType, "", null)
                                                }
                                            }
                                        }
                                    }

                                } catch (exc: Exception) {

                                }
                            }
                        } else {
                            AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                        }
                    }
                }, { error ->
                    uiThread {
                        AlertClass.hideLoadingMessage(loadingProgress)
                        if (error != null) {
                            when (error) {
                                NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                    AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                                }
                                else -> {
                                    AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                                }
                            }
                        }

                    }
                })
            }


        } else {
            AlertClass.showTopMessage(this@SignupCodeActivity,findViewById(R.id.container),"Form","EmptyFields","error",null)
        }
    }

    private fun makePreSignup() {
        val username = this@SignupCodeActivity.signupStruct?.username ?: return
        loadingProgress = AlertClass.showLoadingMessage(this@SignupCodeActivity)
        loadingProgress?.show()
        doAsync {

            AuthRestAPIClass.preSignup(username, { data, error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error == HTTPErrorType.Success) {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val id = data.get("id").asInt
                                        this@SignupCodeActivity.signupStruct?.preSignupId = id
                                        AlertClass.showAlertMessage(this@SignupCodeActivity, "ActionResult", "ResendCodeSuccessful", "", null)
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "ExistUsername" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", null)
                                            }
                                            else -> {
                                            }
                                        }
                                    }
                                }

                            } catch (exc: Exception) {

                            }
                        }
                    } else {
                        AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                    }
                }
            }, { error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                            }
                        }
                    }
                }
            })
        }
    }

    private fun makeForgotPass() {
        val username = this@SignupCodeActivity.signupStruct?.username ?: return
        loadingProgress = AlertClass.showLoadingMessage(this@SignupCodeActivity)
        loadingProgress?.show()
        doAsync {

            AuthRestAPIClass.preSignup(username, { data, error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error == HTTPErrorType.Success) {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val id = data.get("id").asInt
                                        this@SignupCodeActivity.signupStruct?.preSignupId = id
                                        AlertClass.showAlertMessage(this@SignupCodeActivity, "ActionResult", "ResendCodeSuccessful", "", null)

                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "UserNotExist" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", null)
                                            }
                                            else -> {

                                            }
                                        }
                                    }
                                }

                            } catch (exc: Exception) {

                            }
                        }
                    } else {
                        AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                    }
                }
            }, { error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
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

        loadingProgress = AlertClass.showLoadingMessage(this@SignupCodeActivity)
        loadingProgress?.show()
        doAsync {
            TokenHandlerSingleton.getInstance(applicationContext).authorize({ error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error == HTTPErrorType.Success) {
                        if (TokenHandlerSingleton.getInstance(applicationContext).isAuthorized()) {
                            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString(USERNAME_KEY, this@SignupCodeActivity.signupStruct?.username!!)
                            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString(PASSWORD_KEY, this@SignupCodeActivity.signupStruct?.password!!)

                            // Navigate to Signup More Info Activity
                            val moreInfoIntent = SignupMoreInfo1Activity.newIntent(this@SignupCodeActivity)
                            startActivity(moreInfoIntent)
                            this@SignupCodeActivity.finish()


                        } else {
                            // Navigate to LoginActivity
                            val i = LoginActivity.newIntent(this@SignupCodeActivity)
                            startActivity(i)
                        }
                    } else {
                        AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                    }
                }
            }, { error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                            }
                        }
                    }

                }
            })
        }
    }

    private fun sendForgotPassCode() {
        val code: String = SignupCodeA_codeEditText.text.trim().toString()
        if (code != "") {
            var intCode: Int? = null
            try {
                intCode = code.toInt()
            } catch (ext: Exception) {

            }
            if (intCode == null) {
                AlertClass.showTopMessage(this@SignupCodeActivity,findViewById(R.id.container),"Form","CodeWrong","error",null)
                return
            }

            this@SignupCodeActivity.signupStruct?.password = code
            val i = ResetPasswordActivity.newIntent(this@SignupCodeActivity, this@SignupCodeActivity.signupStruct, false)
            startActivity(i)
        } else {
            AlertClass.showTopMessage(this@SignupCodeActivity,findViewById(R.id.container),"Form","EmptyFields","error",null)
        }
    }
}
