package com.concough.android.singletons

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.util.Log

/**
 * Created by abolfazl on 7/3/17.
 */
class FontCacheSingleton {

    private var _zhikanRTF: Typeface?
    private var _zhikanBTF: Typeface?
    private var _zhikanLTF: Typeface?

    private constructor(context: Context) {
//        this._zhikanRTF = Typeface.createFromAsset(context.assets, "fonts/IRANYekanMobileRegular.ttf")
//        this._zhikanBTF = Typeface.createFromAsset(context.assets, "fonts/IRANYekanMobileBold.ttf")
//        this._zhikanLTF = Typeface.createFromAsset(context.assets, "fonts/IRANYekanMobileLight.ttf")
        this._zhikanRTF = Typeface.createFromAsset(context.assets, "fonts/IRANSansMobile_Medium.ttf")
        this._zhikanBTF = Typeface.createFromAsset(context.assets, "fonts/IRANSansMobile_Bold.ttf")
        this._zhikanLTF = Typeface.createFromAsset(context.assets, "fonts/IRANSansMobile_Light.ttf")
    }

    companion object Factory {
        private var sharedInstance: FontCacheSingleton? = null

        @JvmStatic
        fun getInstance(context: Context): FontCacheSingleton {
            if (sharedInstance == null)
                sharedInstance = FontCacheSingleton(context)

            return sharedInstance!!
        }
    }

    public val Regular: Typeface
        get() {
            Log.d("TEST", this._zhikanRTF.toString())
            return this._zhikanRTF!!
        }
    public val Bold: Typeface
        get() = this._zhikanBTF!!

    public val Light: Typeface
        get() = this._zhikanLTF!!

}