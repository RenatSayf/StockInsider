package com.renatsayf.stockinsider.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.receivers.AlarmReceiver

object AlarmPendingIntent
{
    data class ResultIntent(val time: Long, val instance: PendingIntent)

    private const val requestCode : Int = 1578
    const val ACTION_START_ALARM = "com.renatsayf.stockinsider.utils.action.START_ALARM"
    private val TAG = this.javaClass.simpleName
    val IS_ALARM_SETUP_KEY = "$TAG.is_alarm_setup_key"

    fun create(context: Context) : ResultIntent
    {
        val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
        val alarmCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = System.currentTimeMillis()
        }
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_START_ALARM
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        savePreferences(context, IS_ALARM_SETUP_KEY)
        return ResultIntent(alarmCalendar.timeInMillis, alarmIntent)
    }

    fun create(context: Context, calendar: Calendar) : ResultIntent
    {
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.action = ACTION_START_ALARM
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        savePreferences(context, IS_ALARM_SETUP_KEY)
        return ResultIntent(calendar.timeInMillis, alarmIntent)
    }

    private fun savePreferences(context: Context, key: String)
    {
        with(context.getSharedPreferences(MainActivity.APP_SETTINGS, Context.MODE_PRIVATE).edit())
        {
            putBoolean(key, true)
            apply()
        }
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