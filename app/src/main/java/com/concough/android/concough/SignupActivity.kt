package com.concough.android.concough

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.concough.android.extensions.isValidPhoneNumber
import com.concough.android.rest.AuthRestAPIClass
import com.concough.android.singletons.FontCacheSingleton
import com.concough.android.structures.HTTPErrorType
import kotlinx.android.synthetic.main.activity_login.*

import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.coroutines.experimental.CoroutineStart
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SignupActivity : AppCompatActivity() {
    private var isUsernameValid = false
    private var mainUsernameText: String = ""

    companion object {
        val TAG = "SignupActivity"

        fun newIntent(packageContext: Context): Intent {
            val i = Intent(packageContext, SignupActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            return i
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        this.mainUsernameText = usernameCheckTextView.text.toString()

        // Hiding Action Bar
        supportActionBar?.hide()

        // Setting Font of Controls
        signupTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Bold
        sendCode.typeface = FontCacheSingleton.getInstance(applicationContext).Regular
        usernameEdit.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        loginHintTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        loginButton.typeface = FontCacheSingleton.getInstance(applicationContext).Regular
        hintTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light
        coutryCodeTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Bold
        usernameCheckTextView.typeface = FontCacheSingleton.getInstance(applicationContext).Light

        SignupAloading.visibility = View.GONE

        // -- Listeners
        sendCode.setOnClickListener {
        }

        loginButton.setOnClickListener {
            val i = LoginActivity.newIntent(this@SignupActivity)

            startActivity(i)
            finish()
        }

        usernameEdit.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                usernameCheckTextView.visibility = View.VISIBLE

                if (s.toString().length > 0) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        usernameEdit.textDirection = View.TEXT_DIRECTION_LTR
                        usernameEdit.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    } else {
                        usernameEdit.gravity = Gravity.END
                    }

                    var username = s.toString()
                    if (username.isValidPhoneNumber) {
                        Log.d(TAG, "Is Correct")

                        if (username.startsWith("0")) {
                            username = username.substring(1)
                        }
                        username = "98$username"
                        usernameCheckTextView.text = ""

                        SignupAloading.visibility = View.VISIBLE
                        doAsync {

                            AuthRestAPIClass.checkUsername(username, completion = {
                                data, error ->

                                uiThread {
                                    SignupAloading.visibility = View.GONE

                                    usernameCheckTextView.visibility = View.VISIBLE
                                    Log.d(TAG, data.toString())
                                    Log.d(TAG, error.toString())

                                    if (error == HTTPErrorType.Success) {
                                        val status = data?.get("status")?.asString
                                        status.let {
                                            when (status) {
                                                "OK" -> {
                                                    isUsernameValid = true
                                                    usernameCheckTextView.text = "شماره همراه وارد شده صحیح است"
                                                    usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughGreen))

                                                }
                                                "Error" -> {
                                                    isUsernameValid = false
                                                    val error_type = data?.get("error_type")?.asString
                                                    error_type.let {
                                                        when(error_type) {
                                                            "ExistUsername" -> {
                                                                usernameCheckTextView.text = "این شماره همراه قبلا رزرو شده است"
                                                                usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughRedLight))

                                                            }
                                                            else -> {
                                                                usernameCheckTextView.text = mainUsernameText
                                                                usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughGray))

                                                                // Show Alert to user
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // show error Alert
                                    }

                                }

                            }, failure = {
                                error ->
                                Log.d(TAG, error.toString())
                                uiThread {
                                    SignupAloading.visibility = View.GONE

                                    val t = Toast.makeText(this@SignupActivity, "Farid koni ast", Toast.LENGTH_LONG)
                                    t.show()
                                }
                            })
                        }
                    } else {
                        usernameCheckTextView.text = "شماره همراه وارد شده صحیح نمی باشد"
                        usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughRedLight))

                    }

                } else {
                    usernameCheckTextView.text = mainUsernameText
                    usernameCheckTextView.setTextColor(resources.getColor(R.color.colorConcoughGray))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        usernameEdit.textDirection = View.TEXT_DIRECTION_RTL
                        usernameEdit.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    } else {
                        usernameEdit.gravity = Gravity.START
                    }
                }
            }

        })
    }
}
