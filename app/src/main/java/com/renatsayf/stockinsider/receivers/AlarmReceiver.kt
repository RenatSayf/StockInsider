package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.utils.IsFilingTime
import javax.inject.Inject

class AlarmReceiver @Inject constructor() : BroadcastReceiver()
{
    companion object
    {
        const val ACTION_START_ALARM = "com.renatsayf.stockinsider.receivers.action.START_ALARM"
        const val ACTION_END_ALARM = "com.renatsayf.stockinsider.receivers.action.END_ALARM"
    }

    @Inject
    lateinit var notification : ServiceNotification

    init {
        App().component.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        println("-------------------------------------------------------------\n" +
                "Сработал onReceive")
        intent?.let {
            println("Intent action = ${intent.action}")
            val timeZone = TimeZone.getTimeZone(context?.getString(R.string.app_time_zone))
            IsFilingTime.checking(timeZone)
            when(intent.action)
            {
                ACTION_START_ALARM ->
                {
                    val message = "Сработал будильник ACTION_START_ALARM"
                    context?.let { it1 -> notification.notify(it1, null, message, R.string.app_time_zone) }
                    println(message)
                }
                ACTION_END_ALARM ->
                {
                    val message = "Сработал будильник ACTION_END_ALARM"
                    context?.let { it1 -> notification.notify(it1, null, message, R.string.app_time_zone) }
                    println(message)
                }
                else -> return
            }
        }
    }

}