package com.concough.android.concough

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.concough.android.utils.EnDeCryptorV18
import com.concough.android.utils.KeyChainAccessProxy
import kotlinx.android.synthetic.main.activity_test.*;

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        testA_set_keystore.setOnClickListener {
            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString("ali", "123456")
        }

        testA_read_keystore.setOnClickListener {
            val d = KeyChainAccessProxy.getInstance(applicationContext).getValueAsString("ali2")
            Log.d("TEST", d)
            Toast.makeText(this@TestActivity, d, Toast.LENGTH_LONG).show()
        }
    }
}
