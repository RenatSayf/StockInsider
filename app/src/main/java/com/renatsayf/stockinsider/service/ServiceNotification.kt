package com.renatsayf.stockinsider.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import com.renatsayf.stockinsider.R
import javax.inject.Inject

class ServiceNotification @Inject constructor()
{
    companion object
    {
        const val CHANEL_ID: String = "channel_com.renatsayf.stockinsider.service"
        const val NOTIFICATION_ID : Int = 159123545
    }

    fun notify(context : Context, pendingIntent : PendingIntent?, text : String, iconResource : Int)
    {
        val notification = NotificationCompat.Builder(context, CHANEL_ID)
            .setSmallIcon(iconResource)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (VERSION.SDK_INT >= VERSION_CODES.O)
        {
            val name = context.getString(R.string.app_name).plus(CHANEL_ID)
            val descriptionText = context.getString(R.string.discription_text_to_notification)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.cancelAll()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotifications(context : Context)
    {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}