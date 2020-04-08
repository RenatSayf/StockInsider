package com.renatsayf.stockinsider.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.receivers.AlarmReceiver

object AlarmPendingIntent
{
    data class ResultIntent(val time: Long, val instance: PendingIntent)

    private const val requestCode : Int = 1578
    private const val ACTION_START_ALARM = "com.renatsayf.stockinsider.utils.action.START_ALARM"
    private val TAG = this.javaClass.simpleName

    fun create(context: Context, timeZone: TimeZone, startHour : Int, startMinute : Int) : ResultIntent
    {
        val alarmCalendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
        }
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_START_ALARM
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return ResultIntent(alarmCalendar.timeInMillis, alarmIntent)
    }

    fun create(context: Context, calendar: Calendar) : ResultIntent
    {
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_START_ALARM
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return ResultIntent(calendar.timeInMillis, alarmIntent)
    }

    fun getAlarmIntent(context: Context) : PendingIntent?
    {
        return Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_START_ALARM
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)
        }
    }

    fun isAlarmSetup(context: Context) : Boolean
    {
        val intent = getAlarmIntent(context)
        AppLog(context).print(TAG, "********  AlarmPendingIntent = ${intent.toString()}  **************")
        return intent != null
    }


}