package com.concough.android.concough

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.concough.android.vendor.progressHUD.KProgressHUD


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

//        val vView: ImageView = findViewById(R.id.imageView8) as ImageView
//
//        vView.setImageResource(R.raw.konkoor)


//        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.konkoor));
//        mVideoView.setMediaController( MediaController (this))
//        mVideoView.requestFocus();


//        val hud = AlertClass.showUpdatingMessage(this, 100)
//        hud.setProgress(60)
//        hud.show()
//        scheduleDismiss(hud)

//        AlertClass.showAlertMessage(this, "", "", "", {
//            Log.d(TAG, "Worked")
//        })

//        val view = snack.getView() as Snackbar.SnackbarLayout
//        view.addView(vie)
//        snack.show()

//        testA_set_keystore.setOnClickListener {
//            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString("ali", "123456")
//        }

//        AlertClass.showAlertMessageCustom(this, "ایا مطمينید", "پاک میشود", "بله", "خیر", {
//
//        })
//        testA_read_keystore.setOnClickListener {
//            //            val d = KeyChainAccessProxy.getInstance(applicationContext).getValueAsString("ali2")
////            Log.d("TEST", d)
////            Toast.makeText(this@TestActivity, d, Toast.LENGTH_LONG).show()
////        }
////
////
////            MediaRestAPIClass.downloadEsetImage(applicationContext, 1, testA_imageView, completion = { data, error ->
////                if (error == HTTPErrorType.Success) {
////                    Log.d(TAG, "image loaded")
////
////                }
////            }, failure = {error ->
////
////            })
//            Log.d(TAG, "")
//        }
    }

    override fun onStart() {
        super.onStart()
//        AlertClass.showTopMessage(this, findViewById(R.id.tttt), "ActionResult", "ResendCodeSuccess", "", {
//
//        })

    }

    override fun onResume() {
        super.onResume()

    }

    private fun scheduleDismiss(hud: KProgressHUD) {
        val handler = Handler()
        handler.postDelayed(Runnable { hud.dismiss() }, 2000)
    }
}