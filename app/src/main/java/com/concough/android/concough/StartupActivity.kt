package com.concough.android.concough

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        val i: Intent = SignupActivity.newIntent(this@StartupActivity)
        startActivity(i)

//        Log.d(TAG, "Finishing This")
        finish()
    }

    companion object {
        private val TAG = "StartupActivity"

    }
}