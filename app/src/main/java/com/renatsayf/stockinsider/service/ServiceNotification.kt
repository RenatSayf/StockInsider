package com.renatsayf.stockinsider.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.renatsayf.stockinsider.R
import javax.inject.Inject

class ServiceNotification @Inject constructor()
{
    private val chanelId : String = "channel_com.renatsayf.stockinsider.network"

    fun build(context : Context, pendingIntent : PendingIntent, text : String, iconResource : Int) : Notification
    {
        val notification = NotificationCompat.Builder(context, chanelId)
            .setSmallIcon(iconResource)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .build()
        return notification
    }
}