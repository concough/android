package com.concough.android.singletons

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.concough.android.concough.MainApplication
import com.concough.android.concough.R

/**
 * Created by abolfazl on 12/11/18.
 */
class NotificationSingleton {
    private lateinit var context: Context

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
    private var currentNotificationID: Int = 0

    companion object Factory {
        val TAG: String = "NotificationSingleton"

        private var sharedInstance : NotificationSingleton? = null

        @JvmStatic
        fun  getInstance(context: Context): NotificationSingleton {
            if (sharedInstance == null)
                sharedInstance = NotificationSingleton(context)

            return sharedInstance!!
        }
    }

    private constructor(context: Context) {
        this.context = context
        notificationManager = this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    }

    private fun sendNotification() {
        val notificationIntent = Intent(this.context, MainApplication::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

//        notificationBuilder = NotificationCompat.Builder(this@EntrancePackageDownloader)
        notificationBuilder?.setContentIntent(contentIntent)

        val notification = notificationBuilder!!.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
        currentNotificationID++
        var notificationId: Int = currentNotificationID
        if (notificationId == Integer.MAX_VALUE - 1) {
            notificationId = 0
        }
        notificationManager?.notify(notificationId, notification)
    }

    public fun simpleNotification(message: String, subMessage: String) {
        notificationBuilder = NotificationCompat
                .Builder(this.context)
                .setContentTitle(message)
                .setContentText(subMessage)
                .setSmallIcon(R.drawable.logo_white_transparent_notification)

        sendNotification()
    }

}