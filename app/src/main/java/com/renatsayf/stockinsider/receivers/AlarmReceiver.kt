package com.renatsayf.stockinsider.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.IsFilingTime

class AlarmReceiver : BroadcastReceiver()
{


    override fun onReceive(context: Context?, intent: Intent?)
    {
        context?.let {
            val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
            val checking = IsFilingTime.checking(timeZone)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            when(checking.isFilingTime)
            {
                true ->
                {
                    val calendar = Calendar.getInstance(timeZone)
                    calendar.timeInMillis += IsFilingTime.LOAD_INTERVAL
                    val alarmIntent = AlarmPendingIntent.create(
                            context,
                            calendar[Calendar.HOUR_OF_DAY],
                            calendar[Calendar.MINUTE]
                    )
                    alarmManager.apply {
                        setExact(AlarmManager.RTC, alarmIntent.time, alarmIntent.intent)
                    }
                }
                false ->
                {
                    AlarmPendingIntent.intent?.let {
                        alarmManager.cancel(it)
                    }
                }
            }

        }

    }

}