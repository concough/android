package com.concough.android.singletons

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import android.os.Build
import com.concough.android.settings.CONNECT_TIMEOUT
import com.concough.android.settings.READ_TIMEOUT
import com.concough.android.utils.TLSSocketFactory
import java.util.concurrent.TimeUnit


class RetrofitSSLClientSingleton private constructor() {
    private var builder: OkHttpClient.Builder = OkHttpClient.Builder()

    companion object {
        private const val TAG = "SynchronizationSingleton"
        private var sharedInstance: RetrofitSSLClientSingleton? = null

        @JvmStatic
        fun getInstance(): RetrofitSSLClientSingleton {
            if (sharedInstance == null) {
                sharedInstance = RetrofitSSLClientSingleton()
            }

            return sharedInstance!!
        }
    }

    init {
        this.builder
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)

        if (Build.VERSION.SDK_INT in 16..21) {
            try {
                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build()

                val specs = ArrayList<ConnectionSpec>()
                specs.add(cs)
                specs.add(ConnectionSpec.COMPATIBLE_TLS)
                specs.add(ConnectionSpec.CLEARTEXT)

                this.builder.sslSocketFactory(TLSSocketFactory()).connectionSpecs(specs)
            } catch (exc: Exception) {
//                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc)
            }
        }
    }

    public fun getBuilder(): OkHttpClient.Builder {
        return this.builder
    }
}
