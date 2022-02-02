package com.renatsayf.stockinsider.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.utils.AlarmPendingIntent
import com.renatsayf.stockinsider.utils.AppCalendar
import javax.inject.Inject


class Scheduler @Inject constructor(private val context: Context): IScheduler {

    companion object {
        const val REQUEST_CODE = 2455563
        const val ONE_SHOOT_ACTION = "$REQUEST_CODE.one_shoot_action"
        const val REPEAT_SHOOT_ACTION = "$REQUEST_CODE.repeat_shoot_action"
        val SET_NAME = "${this::class.java.simpleName}.setName"
    }

    private val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun createPendingIntent(action: String, setName: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(SET_NAME, setName)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }


    override fun scheduleOne(startTime: Long, overTime: Long, setName: String): Boolean {
        val pendingIntent = createPendingIntent(ONE_SHOOT_ACTION, setName)
        return try {
            alarmManager.apply {
                setExact(AlarmManager.RTC, startTime + overTime, pendingIntent)
            }
            val isAlarmSetup = isAlarmSetup(setName)
            return isAlarmSetup != null
        } catch (e: Exception) {
            false
        }
    }

    override fun scheduleRepeat(overTime: Long, interval: Long, setName: String): Boolean {
        val pendingIntent = createPendingIntent(REPEAT_SHOOT_ACTION, setName)
        return try {
            alarmManager.apply {
                setRepeating(AlarmManager.RTC, System.currentTimeMillis() + overTime, interval, pendingIntent)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun isAlarmSetup(setName: String, isRepeat: Boolean): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).let {
            it.action = when(isRepeat) {
                true -> REPEAT_SHOOT_ACTION
                else -> ONE_SHOOT_ACTION
            }
            it.putExtra(SET_NAME, setName)
        }
        val pendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE)
        }
        return pendingIntent
    }

    override fun cancel(pendingIntent: PendingIntent): Boolean {
        alarmManager.cancel(pendingIntent)
        //pendingIntent.cancel()
        return true
    }


}