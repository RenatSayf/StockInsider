package com.renatsayf.stockinsider.service

import android.app.Service
import android.content.Intent
import android.icu.util.TimeZone
import android.os.IBinder
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.network.Scheduler
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

    private lateinit var mTimer : Timer

    companion object
    {
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
        val (_, isAfterFiling) = IsFilingTime.checking(timeZone)
        when (isAfterFiling)
        {
            true -> mTimer = scheduler.setNetSchedule(this, true)
            else -> mTimer = scheduler.setNetSchedule(this, false)
        }
        iShowMessage?.showMessage(this.getString(R.string.text_searching_is_created))
    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        isStopService = true
        scheduler.cancelNetSchedule(this, mTimer)
        notification.notify(this, null, this.getString(R.string.text_not_memory), R.drawable.ic_public_green)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        when(isStopService)
        {
            false -> {
                val serviceIntent = Intent(this, StockInsiderService::class.java)
                this.startService(serviceIntent)
                return
            }
        }
        when(iShowMessage)
        {
            null -> throw Throwable("${this.javaClass.simpleName} setMessageListener() not enabled ")
            else ->
            {
                scheduler.cancelNetSchedule(this, mTimer)
                notification.cancelNotifications(this)
                iShowMessage?.showMessage(this.getString(R.string.text_search_is_disabled))
            }
        }
    }



}