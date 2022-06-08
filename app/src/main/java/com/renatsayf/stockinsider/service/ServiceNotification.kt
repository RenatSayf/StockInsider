package com.renatsayf.stockinsider.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationCompat
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.utils.Utils
import javax.inject.Inject


class ServiceNotification @Inject constructor() : Notification()
{
    companion object
    {
        private val CHANNEL_ID : String = "${this::class.java.simpleName}.service_notification"
        const val NOTIFICATION_ID : Int = 15917

        val notify: (Context, ArrayList<Deal>) -> Unit = { context: Context, list: ArrayList<Deal> ->

            val time = Utils().getFormattedDateTime(0, Calendar.getInstance().time)
            val message = "The request has been performed at \n" +
                    "$time (в.мест) \n" +
                    "${list.size} results found"
            val intent = Intent(context, MainActivity::class.java).apply {
                putParcelableArrayListExtra(Deal.KEY_DEAL_LIST, list)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK //or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            ServiceNotification().createNotification(context = context, pendingIntent = pendingIntent, text = message).show()
        }
    }

    var notification: Notification? = null

    private var context: Context? = null

    fun createNotification(context : Context,
                           pendingIntent : PendingIntent?,
                           text : String,
                           iconResource: Int = R.drawable.ic_notification_logo
                           ) : ServiceNotification
    {
        this.context = context

        notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setPriority(NotificationManager.IMPORTANCE_NONE)
            .setCategory(CATEGORY_RECOMMENDATION)
            .setSmallIcon(iconResource)
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

    fun cancelNotifications(context : Context)
    {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}