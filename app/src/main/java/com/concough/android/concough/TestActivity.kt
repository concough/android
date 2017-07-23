package com.concough.android.concough

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

//        testA_set_keystore.setOnClickListener {
//            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString("ali", "123456")
//        }
//
//        testA_read_keystore.setOnClickListener {
//            val d = KeyChainAccessProxy.getInstance(applicationContext).getValueAsString("ali2")
//            Log.d("TEST", d)
//            Toast.makeText(this@TestActivity, d, Toast.LENGTH_LONG).show()
//        }
// Spinner element
    }
}