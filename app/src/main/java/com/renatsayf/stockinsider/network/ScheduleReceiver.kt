package com.renatsayf.stockinsider.network

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import java.util.*
import javax.inject.Inject


class ScheduleReceiver @Inject constructor() : BroadcastReceiver()
{
    override fun onReceive(context : Context?, intent : Intent?)
    {
        val action = intent?.action

        return
    }

    fun setNetSchedule(context : Context) : PendingIntent
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, ScheduleReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 53)
        }

        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000L,
                60000L,
                alarmIntent
                                 )
        return alarmIntent
    }

    fun cancelNetSchedule(context : Context)
    {
        val intent = Intent(context, ScheduleReceiver::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }
}