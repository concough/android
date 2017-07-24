package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.concough.android.rest.MediaRestAPIClass
import com.concough.android.structures.HTTPErrorType
import com.concough.android.utils.EnDeCryptorV18
import com.concough.android.utils.KeyChainAccessProxy
import kotlinx.android.synthetic.main.activity_test.*;
import org.jetbrains.anko.doAsync


class TestActivity : AppCompatActivity() {

    companion object {
        val TAG = "TestActivity"

        fun newIntent(packageContext: Context): Intent {
            val i = Intent(packageContext, TestActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return i
        }
    }

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


            MediaRestAPIClass.downloadEsetImage(applicationContext, 1, testA_imageView, completion = {data, error ->
                if (error == HTTPErrorType.Success) {
                    Log.d(TAG, "image loaded")

                }
            }, failure = {error ->

            })
    }
}