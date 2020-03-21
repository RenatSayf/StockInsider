package com.renatsayf.stockinsider.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.utils.Utils
import javax.inject.Inject

class FetchFilingsDataReceiver @Inject constructor() : BroadcastReceiver()
{
    companion object
    {
        const val CODE: Int = 4258
        const val ACTION_LOAD_DATA: String = "com.renatsayf.stockinsider.receivers.action.LOAD_DATA"
    }

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var utils: Utils


    init {
        App().component.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        val message = "***********************************\n" +
                "Сработал onReceive в ${this.javaClass.simpleName}"
        println(message)
        context?.let { notification.notify(it, null, message, R.drawable.ic_stock_hause_cold) }
    }
}