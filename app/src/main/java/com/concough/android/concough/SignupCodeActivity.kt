package com.concough.android.concough

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import com.concough.android.general.AlertClass
import com.concough.android.rest.AuthRestAPIClass
import com.concough.android.rest.DeviceRestAPIClass
import com.concough.android.settings.PASSWORD_KEY
import com.concough.android.settings.USERNAME_KEY
import com.concough.android.singletons.*
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import com.concough.android.structures.SignupStruct
import com.concough.android.utils.KeyChainAccessProxy
import com.concough.android.vendor.progressHUD.KProgressHUD
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup_code.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SignupCodeActivity : AppCompatActivity() {
    private var signupStruct: SignupStruct? = null
    private var fromWhichActivity: String? = null
    private var loadingProgress: KProgressHUD? = null
    private var countdowntimer: CountDownTimer? = null

    private var send_type: String = "sms"
        set(value) {
            field = value
            when (value) {
                "call" -> SignupCodeA_ResendButton.text = "ارسال کد از طریق تماس"
                "sms" -> SignupCodeA_ResendButton.text = "ارسال کد فعالسازی"
                "" -> SignupCodeA_ResendButton.text = "فردا سعی نمایید..."
            }
        }

    private var timerCounter: Int = 120
        set(value)  {
            field = value
            if (field > 0) {
                timerTextView.visibility = View.VISIBLE

                val minute: Int = value / 60
                val seconds: Int = value % 60

                timerTextView.text = "${FormatterSingleton.getInstance().NumberFormatter.format(minute)}:${FormatterSingleton.getInstance().NumberFormatter.format(seconds)}"
            } else {
                timerTextView.visibility = View.GONE
                countdowntimer?.cancel()
                this@SignupCodeActivity.changeResendButtonState(true)
            }
        }
        get() = field

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
        SignupCodeA_infoTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        SignupCodeA_returnButton.typeface = FontCacheSingleton.getInstance(applicationContext).Regular
        timerTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light

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

        SignupCodeA_infoTextView.text = "ارسال شده به ${this.signupStruct?.username}\n${SignupCodeA_infoTextView.text}"

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

        SignupCodeA_returnButton.setOnClickListener({
            finish()
        })
    }

    override fun onResume() {
        super.onResume()
        changeResendButtonState(false)
        startTimerWithInterval()
    }

    override fun onStop() {
        super.onStop()
        countdowntimer?.cancel()
    }

    private fun startTimerWithInterval() {
        timerCounter = 120
        countdowntimer?.cancel()
        countdowntimer = object : CountDownTimer((timerCounter * 1000).toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timerCounter-=1
            }

            override fun onFinish() {
                timerCounter = 0
            }
        }
        countdowntimer?.start()

    }

    private fun stopCounting() {
        countdowntimer?.cancel()
        timerTextView.visibility = View.GONE
    }

    private fun changeResendButtonState(state: Boolean) {
        SignupCodeA_ResendButton.isEnabled = state

        if (state) {
            SignupCodeA_ResendButton.background = ActivityCompat.getDrawable(this, R.drawable.concough_border_radius_style)
            SignupCodeA_ResendButton.setTextColor(ActivityCompat.getColor(applicationContext, R.color.colorConcoughBlue))
        } else {
            SignupCodeA_ResendButton.background = ActivityCompat.getDrawable(this, R.drawable.concough_border_radius_gray_style)
            SignupCodeA_ResendButton.setTextColor(ActivityCompat.getColor(applicationContext, R.color.colorConcoughGray2))
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
                                            this@SignupCodeActivity.stopCounting()
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
                                        this@SignupCodeActivity.signupStruct?.preSignupId = id
                                        AlertClass.showAlertMessage(this@SignupCodeActivity, "ActionResult", "ResendCodeSuccess", "success", {
                                            this@SignupCodeActivity.changeResendButtonState(false)
                                            this@SignupCodeActivity.startTimerWithInterval()
                                        })
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "ExistUsername" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", {
                                                    this@SignupCodeActivity.stopCounting()
                                                    this@SignupCodeActivity.changeResendButtonState(false)
                                                })
                                            }
                                            "SMSSendError", "CallSendError" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", null)
                                                this@SignupCodeActivity.changeResendButtonState(true)
                                            }
                                            "ExceedToday" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", {
                                                    this@SignupCodeActivity.send_type = "call"
                                                })
                                            }
                                            "ExceedCallToday" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", {
                                                    this@SignupCodeActivity.send_type = ""
                                                    this@SignupCodeActivity.stopCounting()
                                                    this@SignupCodeActivity.changeResendButtonState(false)
                                                })
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

            AuthRestAPIClass.forgotPassword(username, send_type, { data, error ->
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
                                        AlertClass.showAlertMessage(this@SignupCodeActivity, "ActionResult", "ResendCodeSuccess", "success", {
                                            this@SignupCodeActivity.changeResendButtonState(false)
                                            this@SignupCodeActivity.startTimerWithInterval()
                                        })

                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "UserNotExist", "RemoteDBError" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", null)
                                                this@SignupCodeActivity.stopCounting()
                                                this@SignupCodeActivity.changeResendButtonState(false)
                                            }
                                            "SMSSendError", "CallSendError" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", null)
                                                this@SignupCodeActivity.changeResendButtonState(true)
                                            }
                                            "ExceedToday" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", {
                                                    this@SignupCodeActivity.send_type = "call"
                                                })
                                            }
                                            "ExceedCallToday" -> {
                                                AlertClass.showAlertMessage(this@SignupCodeActivity, "AuthProfile", errorType, "error", {
                                                    this@SignupCodeActivity.send_type = ""
                                                    this@SignupCodeActivity.stopCounting()
                                                    this@SignupCodeActivity.changeResendButtonState(false)
                                                })
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

                            this@SignupCodeActivity.getLockedStatus()

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

    private fun getLockedStatus() {
        this@SignupCodeActivity.loadingProgress = AlertClass.showLoadingMessage(this@SignupCodeActivity)
        this@SignupCodeActivity.loadingProgress?.show()

        doAsync {
            DeviceRestAPIClass.deviceCreate(this@SignupCodeActivity, { data, error ->
                uiThread {
                    AlertClass.hideLoadingMessage(this@SignupCodeActivity.loadingProgress)

                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            this@SignupCodeActivity.getLockedStatus()
                        } else {
                            AlertClass.showTopMessage(this@SignupCodeActivity, findViewById(R.id.container), "HTTPError", error.toString(), "error", null)
                        }
                    } else {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val state = data.get("data").asJsonObject.get("state").asBoolean
                                        val device_unique_id = data.get("data").asJsonObject.get("device_unique_id").asString

                                        val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                        if (username != null) {
                                            val androidId = Settings.Secure.getString(applicationContext.contentResolver,
                                                    Settings.Secure.ANDROID_ID)
                                            val deviceModel = Build.MANUFACTURER + " " + Build.MODEL

                                            if (device_unique_id == androidId) {
                                                if (DeviceInformationSingleton.getInstance(applicationContext).setDeviceState(username, "android", deviceModel, state, true)) {
                                                    if (state) {
                                                        // Navigate to Signup More Info Activity
                                                        val moreInfoIntent = SignupMoreInfo1Activity.newIntent(this@SignupCodeActivity)
                                                        startActivity(moreInfoIntent)
                                                        this@SignupCodeActivity.finish()

                                                    } else {
                                                        val moreInfoIntent = StartupActivity.newIntent(this@SignupCodeActivity)
                                                        startActivity(moreInfoIntent)
                                                        this@SignupCodeActivity.finish()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        when (errorType) {
                                            "AnotherDevice" -> {
                                                val username = UserDefaultsSingleton.getInstance(applicationContext).getUsername()
                                                if (username != null) {
                                                    AlertClass.showAlertMessage(this@SignupCodeActivity, "DeviceInfoError", errorType, "error", {
                                                        val device_name = data.get("error_data").asJsonObject.get("device_name").asString
                                                        val device_model = data.get("error_data").asJsonObject.get("device_model").asString

                                                        if (DeviceInformationSingleton.getInstance(applicationContext).setDeviceState(username, device_name, device_model, false, false)) {
                                                            val moreInfoIntent = StartupActivity.newIntent(this@SignupCodeActivity)
                                                            startActivity(moreInfoIntent)
                                                            this@SignupCodeActivity.finish()
                                                        }
                                                    })
                                                }

                                            }
                                            "UserNotExist", "DeviceNotRegistered" -> {
                                                val loginIntent = LoginActivity.newIntent(this@SignupCodeActivity)
                                                startActivity(loginIntent)
                                            }
                                            else -> { }
                                        }

                                    }
                                }
                            } catch (exc: Exception) {

                            }

                        }
                    }
                }
            }, { error ->
                uiThread {
                    AlertClass.hideLoadingMessage(this@SignupCodeActivity.loadingProgress)
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

            this@SignupCodeActivity.stopCounting()
            this@SignupCodeActivity.signupStruct?.password = code
            val i = ResetPasswordActivity.newIntent(this@SignupCodeActivity, this@SignupCodeActivity.signupStruct, false)
            startActivity(i)
        } else {
            AlertClass.showTopMessage(this@SignupCodeActivity,findViewById(R.id.container),"Form","EmptyFields","error",null)
        }
    }
}
