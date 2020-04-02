package com.renatsayf.stockinsider.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.receivers.AlarmReceiver

object AlarmPendingIntent
{
    data class ResultIntent(val time: Long, val intent: PendingIntent)
    var intent: PendingIntent? = null

    private const val requestCode : Int = 1578
    private const val ACTION_START_ALARM = "com.renatsayf.stockinsider.utils.action.START_ALARM"

    fun create(context: Context, startHour : Int, startMinute : Int) : ResultIntent
    {
        val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
        val alarmCalendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
        }
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_START_ALARM
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT)
        }
        intent = alarmIntent
        return ResultIntent(alarmCalendar.timeInMillis, alarmIntent)
    }
}