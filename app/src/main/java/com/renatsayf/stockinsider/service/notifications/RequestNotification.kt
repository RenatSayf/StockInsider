package com.renatsayf.stockinsider.service.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R

class RequestNotification(private val context: Context) {

    companion object {
        const val NOTIFY_ID = 3333555
    }

    private val channelID = "${this::class.java.simpleName}.555555"
    private val notificationManager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = context.getString(R.string.discription_text_to_notification)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = descriptionText
            }
            this.createNotificationChannel(channel)
        }
    }

    fun create(): Notification? {

        return try {

            val message = context.getString(R.string.text_search_is_going)
            val title = context.getString(R.string.app_name)

            return NotificationCompat.Builder(context, channelID)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_stock_hause_cold)
                .setColor(context.getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(message)
                .setProgress(0, 0, true)
                .setChannelId(channelID)
                .build()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            null
        }
    }

    fun show() {
        val notification = create()
        notificationManager.notify(NOTIFY_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFY_ID)
    }
}