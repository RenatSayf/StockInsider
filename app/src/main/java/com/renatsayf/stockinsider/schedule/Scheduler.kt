@file:Suppress("UnnecessaryVariable")

package com.renatsayf.stockinsider.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import javax.inject.Inject


class Scheduler @Inject constructor(private val context: Context): IScheduler {

    companion object {
        private const val REQUEST_CODE = 2455563
        const val ONE_SHOOT_ACTION = "$REQUEST_CODE.one_shoot_action"
        const val REPEAT_SHOOT_ACTION = "$REQUEST_CODE.repeat_shoot_action"
        val SET_NAME = "${this::class.java.simpleName}.setName"
    }

    private val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun createPendingIntent(action: String, intentName: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(SET_NAME, intentName)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return pendingIntent
    }


    override fun scheduleOne(startTime: Long, overTime: Long, name: String): Boolean {
        val pendingIntent = createPendingIntent(ONE_SHOOT_ACTION, name, name.hashCode())
        alarmManager.apply {
            setExact(AlarmManager.RTC, startTime + overTime, pendingIntent)
        }
        val isAlarmSetup = isAlarmSetup(name, isRepeat = false)
        return isAlarmSetup != null
    }

    override fun scheduleRepeat(overTime: Long, interval: Long, name: String): Boolean {
        val pendingIntent = createPendingIntent(REPEAT_SHOOT_ACTION, name, name.hashCode())
        return try {
            alarmManager.apply {
                setRepeating(AlarmManager.RTC, System.currentTimeMillis() + overTime, interval, pendingIntent)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun isAlarmSetup(name: String, isRepeat: Boolean): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).let {
            it.action = when(isRepeat) {
                true -> REPEAT_SHOOT_ACTION
                else -> ONE_SHOOT_ACTION
            }
            it.putExtra(SET_NAME, name)
        }
        val pendingIntent: PendingIntent? = PendingIntent.getBroadcast(context, name.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        return pendingIntent
    }

    override fun cancel(pendingIntent: PendingIntent) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }


}