package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.concough.android.general.AlertClass
import com.concough.android.rest.ProfileRestAPIClass
import com.concough.android.singletons.*
import com.concough.android.structures.HTTPErrorType
import com.concough.android.structures.NetworkErrorType
import kotlinx.android.synthetic.main.activity_startup.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class StartupActivity : AppCompatActivity() {
    companion object {
        private val TAG = "StartupActivity"
        private val SPLASH_DISPLAY_LENGTH = 5000

        @JvmStatic
        fun newIntent(packageContext: Context): Intent {
            val i = Intent(packageContext, StartupActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)


        ExitFromLockModeButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold
        ResetPasswordButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold

        LoginButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold
        SignUpButton.typeface = FontCacheSingleton.getInstance(this@StartupActivity).Bold

        supportActionBar?.hide()
        StartupA_splash.visibility = View.VISIBLE
        val handler = Handler()
        handler.postDelayed(Runnable {
            StartupA_splash.visibility = View.GONE

        }, 4000)



        LoginButton.setOnClickListener(View.OnClickListener {
            val loginIntent = LoginActivity.newIntent(this@StartupActivity)
            startActivity(loginIntent)
            finish()
        })

        SignUpButton.setOnClickListener(View.OnClickListener {
            val loginIntent = SignupActivity.newIntent(this@StartupActivity)
            startActivity(loginIntent)
            finish()
        })



        this.startup()
    }

    private fun startup() {
//        if (TokenHandlerSingleton.getInstance(applicationContext).isAuthorized()) {
//            if (UserDefaultsSingleton.getInstance(applicationContext).hasProfile()) {
//
//                this@StartupActivity.loadBasketItems()
//                val homeIntent = HomeActivity.newIntent(this@StartupActivity)
//                startActivity(homeIntent)
//                finish()
//
//            } else {
//                this@StartupActivity.getProfile()
//            }
//        } else
        if (TokenHandlerSingleton.getInstance(applicationContext).isAuthenticated()) {
            doAsync {
                TokenHandlerSingleton.getInstance(applicationContext).assureAuthorized(true, { authenticated, error ->
                    if (authenticated) {
                        uiThread {
                            if (UserDefaultsSingleton.getInstance(applicationContext).hasProfile()) {

                                this@StartupActivity.loadBasketItems()
                                val homeIntent = HomeActivity.newIntent(this@StartupActivity)
                                startActivity(homeIntent)
                                finish()
                            } else {
                                this@StartupActivity.getProfile()
                            }
                        }
                    } else {
                        uiThread {
                            val loginIntent = LoginActivity.newIntent(this@StartupActivity)
                            startActivity(loginIntent)
                            finish()
                        }
                    }
                }, { error ->
                    uiThread {
                        //this@StartupActivity.StartupA_centerView.visibility = View.VISIBLE
                        val loginIntent = LoginActivity.newIntent(this@StartupActivity)
                        startActivity(loginIntent)
                        finish()
                    }
                })

            }

        } else {
            val loginIntent = LoginActivity.newIntent(this@StartupActivity)
            startActivity(loginIntent)
            finish()
        }
    }

    private fun getProfile() {
        doAsync {
            ProfileRestAPIClass.getProfileData(this@StartupActivity, { data, error ->
                uiThread {
                    if (error != HTTPErrorType.Success) {
                        if (error == HTTPErrorType.Refresh) {
                            this@StartupActivity.getProfile()
                        } else {
                            val loginIntent = LoginActivity.newIntent(this@StartupActivity)
                            startActivity(loginIntent)
                            finish()
                        }
                    } else {
                        if (data != null) {
                            try {
                                val status = data.get("status").asString
                                when (status) {
                                    "OK" -> {
                                        val profile = data.get("record").asJsonArray[0].asJsonObject
                                        if (profile != null) {
                                            val gender = profile.get("gender").asString
                                            val grade = profile.get("grade").asString
                                            val birthday = profile.get("birthday").asString
                                            val modified = profile.get("modified").asString
                                            val firstname = profile.get("user").asJsonObject.get("first_name").asString
                                            val lastname = profile.get("user").asJsonObject.get("last_name").asString


                                            val birthdayDate = FormatterSingleton.getInstance().UTCDateFormatter.parse(birthday)
                                            val modifiedDate = FormatterSingleton.getInstance().UTCDateFormatter.parse(modified)

                                            if ("" != firstname && "" != lastname && "" != gender && "" != grade) {
                                                UserDefaultsSingleton.getInstance(applicationContext).createProfile(firstname, lastname, grade, gender, birthdayDate, modifiedDate)
                                            }

                                            if (UserDefaultsSingleton.getInstance(applicationContext).hasProfile()) {

                                                this@StartupActivity.loadBasketItems()
                                                val homeIntent = HomeActivity.newIntent(this@StartupActivity)
                                                startActivity(homeIntent)
                                                finish()

                                            } else {
                                                // Profile not created
                                                val moreInfoIntent = SignupMoreInfo1Activity.newIntent(this@StartupActivity)
                                                startActivity(moreInfoIntent)
                                                finish()
                                            }

                                        }
                                    }
                                    "Error" -> {
                                        val errorType = data.get("error_type").asString
                                        val moreInfoIntent = SignupMoreInfo1Activity.newIntent(this@StartupActivity)
                                        startActivity(moreInfoIntent)
                                        finish()

                                    }
                                }
                            } catch (exc: Exception) {

                            }

                        }
                    }
                }
            }, { error ->
                uiThread {
                    if (error != null) {
                        when (error) {
                            NetworkErrorType.NoInternetAccess, NetworkErrorType.HostUnreachable -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "error", null)
                            }
                            else -> {
                                AlertClass.showTopMessage(this@StartupActivity, findViewById(R.id.container), "NetworkError", error.name, "", null)
                            }
                        }

                    }

                }

            })

        }
    }

    private fun loadBasketItems() {
        BasketSingleton.getInstance().loadBasketItems(this@StartupActivity)
    }
}