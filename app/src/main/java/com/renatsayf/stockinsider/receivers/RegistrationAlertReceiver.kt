package com.renatsayf.stockinsider.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.di.App
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.Utils
import javax.inject.Inject

class RegistrationAlertReceiver @Inject constructor() : BroadcastReceiver()
{
    companion object
    {
        const val ACTION_START_REGISTRATION = "com.renatsayf.stockinsider.receivers.action.START_REGISTRATION"
        const val ACTION_END_REGISTRATION = "com.renatsayf.stockinsider.receivers.action.END_REGISTRATION"
    }

    @Inject
    lateinit var notification : ServiceNotification

    @Inject
    lateinit var utils: Utils

    private var alarmManager: AlarmManager? = null
    private var loadIntent: PendingIntent? = null


    init {
        App().component.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        println("-------------------------------------------------------------\n" +
                "Сработал onReceive в ${this.javaClass.simpleName}")
        context?.let{
            intent?.let {
                println("Intent action = ${intent.action}")
                val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
                IsFilingTime.checking(timeZone)
                alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                when (intent.action) {
                    ACTION_START_REGISTRATION -> {
                        val message = "Сработал будильник ACTION_START_REGISTRATION"
                        println(message)
                        notification.notify(
                            context,
                            null,
                            message,
                            R.drawable.ic_stock_hause_cold
                        )
                        loadIntent = utils.createPendingIntent(
                            context,
                            FetchFilingsDataReceiver::class.java,
                            FetchFilingsDataReceiver.ACTION_LOAD_DATA,
                            FetchFilingsDataReceiver.CODE,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                        val startTimeInMillis = System.currentTimeMillis() + 10000L
                        alarmManager?.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            startTimeInMillis,
                            IsFilingTime.LOAD_INTERVAL,
                            loadIntent
                        )
                        alarmManager?.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            startTimeInMillis,
                            loadIntent
                        )


                    }
                    ACTION_END_REGISTRATION -> {
                        val message = "Сработал будильник ACTION_END_REGISTRATION"
                        println(message)
                        notification.notify(context,null, message, R.drawable.ic_stock_hause_cold)
                        val loadIntent = utils.createPendingIntent(
                            context,
                            FetchFilingsDataReceiver::class.java,
                            FetchFilingsDataReceiver.ACTION_LOAD_DATA,
                            FetchFilingsDataReceiver.CODE,
                            PendingIntent.FLAG_NO_CREATE
                        )
                        loadIntent?.let {
                            alarmManager?.cancel(it)
                            it.cancel()
                        }
                    }
                    else -> return
                }
            }
        }
    }

}