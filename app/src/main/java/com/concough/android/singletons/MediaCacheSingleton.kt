package com.concough.android.singletons

import android.content.Context
import android.util.LruCache
import com.concough.android.settings.MEDIA_CACHE_SIZE

/**
 * Created by Owner on 10/4/2017.
 */

class MediaCacheSingleton {
    private var mediaCache: LruCache<String, ByteArray>


    companion object Factory {
        val TAG: String = "MediaCacheSingleton"

        private var sharedInstance : MediaCacheSingleton? = null

        @JvmStatic
        fun  getInstance(context: Context): MediaCacheSingleton {
            if (sharedInstance == null)
                sharedInstance = MediaCacheSingleton(context)



            return sharedInstance!!
        }
    }

    private constructor(context: Context) {
        this.mediaCache = LruCache<String, ByteArray>(MEDIA_CACHE_SIZE)

    }

    operator fun get(key: String) : ByteArray? {
        synchronized(this@MediaCacheSingleton.mediaCache, {
            if (this.mediaCache.snapshot().containsKey(key))
                return this.mediaCache.get(key)
            return null
        })
    }

    operator fun set(key: String, value: ByteArray) {
        synchronized(this@MediaCacheSingleton.mediaCache, {
            if (!this.mediaCache.snapshot().containsKey(key))
                this.mediaCache.put(key, value)
        })
    }

    fun clearAll() {
        this.mediaCache.evictAll()
    }

//    fun get(key: String) = this.mediaCache.get(key)



}
