package com.renatsayf.stockinsider.service.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.result.ResultFragment
import com.renatsayf.stockinsider.utils.getFormattedDateTime
import javax.inject.Inject


class ServiceNotification @Inject constructor() : Notification()
{
    companion object
    {
        private val CHANNEL_ID : String = "${this::class.java.simpleName}.service_notification"
        const val NOTIFICATION_ID : Int = 15917
        val ARG_ID = "${this::class.java.simpleName}.ARG_ID"

        val notify: (Context, String, RoomSearchSet?) -> Unit = { context: Context, message: String, set ->

//            val time = Calendar.getInstance().time.getFormattedDateTime(0)
//            val message = "According to the ${set?.queryName} search query, $message results were found \n" +
//                    if (BuildConfig.DEBUG) time.plus(" (в.мест)") else ""

            val setId = set?.id?.toInt() ?: 0
            val notificationId = 5555 + setId
            val pendingIntent = NavDeepLinkBuilder(context)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.nav_result)
                .setArguments(Bundle().apply {
                    putSerializable(ResultFragment.ARG_SEARCH_SET, set)
                    putInt(ARG_ID, notificationId)
                })
                .createPendingIntent()

            ServiceNotification()
                .createNotification(context = context, pendingIntent = pendingIntent, text = message)
                .show(notificationId)
        }

        fun cancelNotifications(context : Context, id: Int)
        {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id)
        }
    }

    private var notification: Notification? = null

    private var context: Context? = null

    fun createNotification(context : Context,
                           pendingIntent : PendingIntent?,
                           text : String,
                           smallIconRes: Int = R.drawable.ic_stock_hause_gold,
                           largeIconRes: Int = R.drawable.icon_logo
                           ) : ServiceNotification
    {
        this.context = context

        val bitmap = BitmapFactory.decodeResource(context.resources, largeIconRes)

        notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setPriority(NotificationManager.IMPORTANCE_NONE)
            .setCategory(CATEGORY_RECOMMENDATION)
            .setLargeIcon(bitmap)
            .setSmallIcon(smallIconRes)
            .setColor(context.getColor(R.color.colorPrimary))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = this.context!!.getString(R.string.discription_text_to_notification)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        return this
    }

    fun show(id : Int = NOTIFICATION_ID)
    {
        when {
            this.context != null -> {
                val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                this.notification?.let {
                    notificationManager.notify(id, this.notification)
                }
            }
            else ->
            {
                val message = "*******${this.javaClass.simpleName}.show() - context is null ***************************"
                throw Throwable(message)
            }
        }
    }

}