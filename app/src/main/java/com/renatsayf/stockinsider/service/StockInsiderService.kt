package com.renatsayf.stockinsider.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.network.ScheduleReceiver
import javax.inject.Inject

class StockInsiderService : Service()
{
    private val serviceId : Int = this.hashCode()

    @Inject
    lateinit var scheduleReceiver : ScheduleReceiver

    @Inject
    lateinit var serviceNotification : ServiceNotification

    @Inject
    lateinit var notification : ServiceNotification

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
        scheduleReceiver.setNetSchedule(this, ScheduleReceiver.REQUEST_CODE)
        iShowMessage?.showMessage(this.getString(R.string.text_searching_is_created))
    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        isStopService = true
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
                scheduleReceiver.cancelNetSchedule(this, ScheduleReceiver.REQUEST_CODE)
                iShowMessage?.showMessage(this.getString(R.string.text_search_is_disabled))
            }
        }
    }



}