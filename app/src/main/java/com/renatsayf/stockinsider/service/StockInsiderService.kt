package com.renatsayf.stockinsider.service

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.IBinder
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.network.Scheduler
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.Utils
import io.reactivex.Completable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StockInsiderService : Service()
{
    @Inject
    lateinit var scheduler : Scheduler

    @Inject
    lateinit var utils : Utils

    @Inject
    lateinit var notification : ServiceNotification

    private lateinit var calendar: Calendar
    private lateinit var timeZone: TimeZone
    private lateinit var timer: Timer

    companion object
    {
        const val SERVICE_ID = 5879
        const val PREFERENCE_KEY = "com.renatsayf.stockinsider.service.StockInsiderService"
        var isStopService = false
        var iShowMessage : IShowMessage? = null
        fun setMessageListener(iMessageListener : IShowMessage)
        {
            iShowMessage = iMessageListener
        }
    }

    interface IShowMessage
    {
        fun showMessage(text : String)
    }

    override fun onBind(p0 : Intent?) : IBinder?
    {
        return null
    }

    override fun onStartCommand(intent : Intent?, flags : Int, startId : Int) : Int
    {

        return START_STICKY
    }

    override fun onCreate()
    {
        super.onCreate()
        App().component.inject(this)

        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClass(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val appNotification = notification.createNotification(this, pendingIntent, getString(R.string.base_notification_text), R.drawable.ic_stock_hause_cold, R.color.colorGold)

        appNotification.notification?.let{
            startForeground(ServiceNotification.NOTIFICATION_ID, appNotification.notification)
        }

        timeZone = TimeZone.getTimeZone(this.getString(R.string.app_time_zone))
        calendar = Calendar.getInstance(timeZone)
        timer = Timer("appTimer")
        timer.schedule(ServiceTask(this, timeZone), 5000, IsFilingTime.LOAD_INTERVAL)

    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        isStopService = true

        notification.createNotification(this, null, this.getString(R.string.text_not_memory), R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        timer.cancel()
        timer.purge()
        when(iShowMessage)
        {
            null -> throw Throwable("${this.javaClass.simpleName} setMessageListener() not enabled ")
            else ->
            {
                notification.cancelNotifications(this)
                iShowMessage?.showMessage(this.getString(R.string.text_search_is_disabled))
            }
        }
    }


}