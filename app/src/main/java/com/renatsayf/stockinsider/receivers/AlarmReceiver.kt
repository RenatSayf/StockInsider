package com.renatsayf.stockinsider.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.hypertrack.hyperlog.HyperLog
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.IsFilingTime
import com.renatsayf.stockinsider.utils.Utils
import javax.inject.Inject

class AlarmReceiver : BroadcastReceiver()
{
    companion object
    {
        val TAG : String = this::class.java.simpleName
    }

    @Inject
    lateinit var utils : Utils

    init
    {
        MainActivity.appComponent.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        context?.let {
            val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
            val calendar = Calendar.getInstance(timeZone)
            //println("******  Alarm fired at ${utils.getFormattedDateTime(0, calendar.time)}  **************************")
            HyperLog.d(TAG, "******  Alarm fired at ${utils.getFormattedDateTime(0, calendar.time)}  **************************")

            val isAlarmSettings = it.getSharedPreferences(MainActivity.IS_ALARM_KEY, Context.MODE_PRIVATE)
                .getBoolean(StockInsiderService.PREFERENCE_KEY, false)
            val checking = IsFilingTime.checking(timeZone)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            when(checking.isFilingTime && isAlarmSettings)
            {
                true ->
                {
                    calendar.timeInMillis += IsFilingTime.LOAD_INTERVAL
                    AlarmPendingIntent.create(
                            context,
                            calendar[Calendar.HOUR_OF_DAY],
                            calendar[Calendar.MINUTE]
                    ).let {res ->
                        alarmManager.apply {
                            setExact(AlarmManager.RTC, res.time, res.instance)
                        }
                        //println("******  Alarm setup at ${utils.getFormattedDateTime(0, calendar.time)}  **************************")
                        HyperLog.d(TAG, "******  Alarm setup at ${utils.getFormattedDateTime(0, calendar.time)}  **************************")
                    }
                }
                false ->
                {
                    AlarmPendingIntent.instance?.let {intent ->
                        alarmManager.cancel(intent)
                        HyperLog.d(TAG, "********  Alarm is canceled  *********************")
                    }
                }
            }
        }

    }

}