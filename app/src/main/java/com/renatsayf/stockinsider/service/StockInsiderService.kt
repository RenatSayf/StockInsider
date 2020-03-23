package com.renatsayf.stockinsider.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color.red
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.IBinder
import android.widget.Chronometer
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.network.Scheduler
import com.renatsayf.stockinsider.ui.main.MainFragment
import com.renatsayf.stockinsider.utils.IsFilingTime
import java.util.*
import javax.inject.Inject

class StockInsiderService : Service()
{
    @Inject
    lateinit var scheduler : Scheduler

    @Inject
    lateinit var serviceNotification : ServiceNotification

    @Inject
    lateinit var notification : ServiceNotification

    private lateinit var db : AppDao
    private lateinit var chronometer: Chronometer

    private lateinit var mTimer : Timer

    companion object
    {
        const val SERIVICE_ID = 5879
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
        val timeZone = TimeZone.getTimeZone(this.getString(R.string.app_time_zone))
        val (isFilingTime, isAfterFiling) = IsFilingTime.checking(timeZone)


        val calendar = Calendar.getInstance(timeZone)

        chronometer = Chronometer(this)
        chronometer.start()
        chronometer.setOnChronometerTickListener {

        }
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClass(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val appNotification = notification.createNotification(this, pendingIntent, getString(R.string.base_notification_text), R.drawable.ic_stock_hause_cold, R.color.colorGold)
        appNotification.notification?.let{
            startForeground(SERIVICE_ID, appNotification.notification)
        }

    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        isStopService = true
        scheduler.cancelNetSchedule(this, mTimer)
        notification.createNotification(this, null, this.getString(R.string.text_not_memory), R.drawable.ic_stock_hause_cold, R.color.colorRed).show()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        chronometer.stop()
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