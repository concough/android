package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.concough.android.extensions.isValidPhoneNumber
import com.concough.android.general.AlertClass
import com.concough.android.rest.AuthRestAPIClass
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.structures.SignupStruct
import com.concough.android.vendor.progressHUD.KProgressHUD
import kotlinx.android.synthetic.main.activity_signup.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SignupActivity : AppCompatActivity() {
    private var isUsernameValid = false
    private var mainUsernameText: String = ""
    private var signupStruct: SignupStruct? = null
    private var loadingProgress: KProgressHUD? = null

    private var send_type: String = "sms"
        set(value) {
            field = value
            when (value) {
                "call" -> {
                    signupA_sendCode.text = "ارسال کد از طریق تماس"
                    signupA_sendCode.background = ContextCompat.getDrawable(applicationContext, R.drawable.concough_border_outline_gray_style)
                }
                "sms" -> {
                    signupA_sendCode.text = "ارسال کد فعالسازی"
                }
                "" -> {
                    signupA_sendCode.text = "فردا سعی نمایید..."
                }
            }
        }

    companion object {
        val TAG = "SignupActivity"

        @JvmStatic
        fun newIntent(packageContext: Context): Intent {
            val i = Intent(packageContext, SignupActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        this.mainUsernameText = signupA_usernameCheckTextView.text.toString()
        this.signupStruct = SignupStruct()
        signupA_sendCode.background = ActivityCompat.getDrawable(this, R.drawable.concough_border_outline_gray_style)
        signupA_sendCode.isEnabled = false

        // Hiding Action Bar
        supportActionBar?.hide()

        // Setting Font of Controls
        signupA_TextView.typeface = FontCacheSingleton.getInstance(applicationContext).Bold
        signupA_sendCode.typeface = FontCacheSingleton.getInstance(applicationContext).Regular
        signupA_usernameEdit.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        signupA_loginHintTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        signupA_loginButton.typeface = FontCacheSingleton.getInstance(applicationContext).Regular
        signupA_hintTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        signupA_coutryCode.typeface = FontCacheSingleton.getInstance(applicationContext).Bold
        signupA_usernameCheckTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light

        signupA_SignupAloading.visibility = View.GONE

        // -- Listeners
        signupA_sendCode.setOnClickListener {
            // request server to send code
            var username = signupA_usernameEdit.text.toString()
            if (username == "") {
                AlertClass.showAlertMessage(this@SignupActivity, "Form", "EmptyFields", "error", null)
            } else if (username.isValidPhoneNumber) {

                if (username.startsWith("0")) {
                    username = username.substring(1)
                }
                username = "98$username"
                this.signupStruct?.username = username
                this.makePreSignup(username)
            } else {
            }
        }

        signupA_loginButton.setOnClickListener {
            val i = LoginActivity.newIntent(this@SignupActivity)

            startActivity(i)
            finish()
        }

        signupA_usernameEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                signupA_usernameCheckTextView.visibility = View.VISIBLE

                if (s.toString().length > 0) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        signupA_usernameEdit.textDirection = View.TEXT_DIRECTION_LTR
                        signupA_usernameEdit.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    } else {
                        signupA_usernameEdit.gravity = Gravity.END
                    }

                    var username = s.toString()
                    if (username.isValidPhoneNumber) {

                        if (username.startsWith("0")) {
                            username = username.substring(1)
                        }
                        username = "98$username"
                        signupA_usernameCheckTextView.text = ""

                        signupA_SignupAloading.visibility = View.VISIBLE
                        doAsync {

                            AuthRestAPIClass.checkUsername(username, completion = { data, error ->

                                uiThread {
                                    signupA_SignupAloading.visibility = View.GONE

                                    signupA_usernameCheckTextView.visibility = View.VISIBLE
//                                    Log.d(TAG, data.toString())
//                                    Log.d(TAG, error.toString())

                                    if (error == HTTPErrorType.Success) {
                                        val status = data?.get("status")?.asString
                                        status.let {
                                            when (status) {
                                                "OK" -> {
                                                    isUsernameValid = true
                                                    signupA_usernameCheckTextView.text = "شماره همراه وارد شده صحیح است"
                                                    signupA_usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughGreen))
                                                    signupA_sendCode.background = ActivityCompat.getDrawable(this@SignupActivity, R.drawable.concough_border_outline_style)
                                                    signupA_sendCode.isEnabled = true

                                                }
                                                "Error" -> {
                                                    isUsernameValid = false
                                                    signupA_sendCode.background = ActivityCompat.getDrawable(this@SignupActivity, R.drawable.concough_border_outline_gray_style)
                                                    signupA_sendCode.isEnabled = false

                                                    val error_type = data?.get("error_type")?.asString
                                                    error_type.let {
                                                        when (error_type) {
                                                            "ExistUsername" -> {
                                                                signupA_usernameCheckTextView.text = "این شماره همراه قبلا رزرو شده است"
                                                                signupA_usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughRedLight))

                                                            }
                                                            else -> {
                                                                signupA_usernameCheckTextView.text = mainUsernameText
                                                                signupA_usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughGray))
                                                                AlertClass.showAlertMessage(this@SignupActivity, "ErrorResult", error_type.toString(), "error", null);

                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        AlertClass.showTopMessage(this@SignupActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                                        signupA_usernameCheckTextView.text = mainUsernameText
                                        signupA_usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughGray))
                                    }

                                }

                            }, failure = { error ->
                                //                                Log.d(TAG, error.toString())
                                uiThread {
                                    signupA_SignupAloading.visibility = View.GONE

                                    if (error != null) {
                                        when (error) {
                                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                                AlertClass.showTopMessage(this@SignupActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                                            }
                                            else -> {
                                                AlertClass.showTopMessage(this@SignupActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                                            }
                                        }

//                                        when (error) {
//                                            NetworkErrorType.HostUnreachable, NetworkErrorType.NoInternetAccess -> {
//                                            }
//                                            else -> {
//                                            }
//                                        }
                                    }

                                    val t = Toast.makeText(this@SignupActivity, error.toString(), Toast.LENGTH_LONG)
                                    t.show()
                                }
                            })
                        }
                    } else {
                        signupA_usernameCheckTextView.text = "شماره همراه وارد شده صحیح نمی باشد"
                        signupA_usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughRedLight))

                    }

                } else {
                    signupA_usernameCheckTextView.text = mainUsernameText
                    signupA_usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughGray))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        signupA_usernameEdit.textDirection = View.TEXT_DIRECTION_RTL
                        signupA_usernameEdit.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    } else {
                        signupA_usernameEdit.gravity = Gravity.START
                    }
                }
            }

        })
    }

    private fun makePreSignup(username: String) {
        loadingProgress = AlertClass.showLoadingMessage(this@SignupActivity)
        loadingProgress?.show()
        doAsync {

            AuthRestAPIClass.preSignup(username, send_type, { data, error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error == HTTPErrorType.Success) {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val id = data.get("id").asInt
                                        this@SignupActivity.signupStruct?.preSignupId = id

                                        val signupCodeIntent = SignupCodeActivity.newIntent(this@SignupActivity, "SignupA", this@SignupActivity.signupStruct)
                                        startActivity(signupCodeIntent)
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "ExistUsername" -> {
                                                this@SignupActivity.signupA_usernameCheckTextView.text = "این شماره همراه قبلا رزرو شده است"
                                                this@SignupActivity.signupA_usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughRedLight))
                                                AlertClass.showAlertMessage(this@SignupActivity, "AuthProfile", errorType, "error", null)
                                            }
                                            "SMSSendError", "CallSendError" -> {
                                                AlertClass.showAlertMessage(this@SignupActivity, "AuthProfile", errorType, "error", null)
                                            }
                                            "ExceedToday" -> {
                                                AlertClass.showAlertMessage(this@SignupActivity, "AuthProfile", errorType, "error", {
                                                    this@SignupActivity.send_type = "call"
                                                })
                                            }
                                            "ExceedCallToday" -> {
                                                AlertClass.showAlertMessage(this@SignupActivity, "AuthProfile", errorType, "error", {
                                                    this@SignupActivity.send_type = ""
                                                    signupA_sendCode.isEnabled = false
                                                    signupA_sendCode.background = ActivityCompat.getDrawable(this@SignupActivity, R.drawable.concough_border_outline_gray_style)

                                                })
                                            }
                                            else -> {
                                                AlertClass.showAlertMessage(this@SignupActivity, "ErrorResult", errorType, "error", null)
                                            }
                                        }
                                    }
                                }

                            } catch (exc: Exception) {

                            }
                        }
                    } else {
                        AlertClass.showTopMessage(this@SignupActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                    }
                }
            }, { error ->
                uiThread {
                    AlertClass.hideLoadingMessage(loadingProgress)
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@SignupActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@SignupActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                            }
                        }
                    }

                }
            })
        }
    }
}
