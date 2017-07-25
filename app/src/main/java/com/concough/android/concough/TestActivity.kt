package com.concough.android.concough

//
//class TestActivity : AppCompatActivity() {
//
//    companion object {
//        val TAG = "TestActivity"
//
//        fun newIntent(packageContext: Context): Intent {
//            val i = Intent(packageContext, TestActivity::class.java)
//            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            return i
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_test)
//
//        testA_set_keystore.setOnClickListener {
//            KeyChainAccessProxy.getInstance(applicationContext).setValueAsString("ali", "123456")
//        }
//
//        testA_read_keystore.setOnClickListener {
//            val d = KeyChainAccessProxy.getInstance(applicationContext).getValueAsString("ali2")
//            Log.d("TEST", d)
//            Toast.makeText(this@TestActivity, d, Toast.LENGTH_LONG).show()
//        }
//
//
//            MediaRestAPIClass.downloadEsetImage(applicationContext, 1, testA_imageView, completion = {data, error ->
//                if (error == HTTPErrorType.Success) {
//                    Log.d(TAG, "image loaded")
//
//                }
//            }, failure = {error ->
//
//            })
//    }
//}