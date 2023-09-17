@file:Suppress("UnnecessaryVariable")

package com.renatsayf.stockinsider.schedule

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.utils.timeToFormattedString
import javax.inject.Inject


class Scheduler @Inject constructor(
    private val context: Context,
    private val receiverClass: Class<AlarmReceiver> = AlarmReceiver::class.java
) : IScheduler {

    companion object {
        private const val ONE_SHOOT_CODE = 2455563
        private const val REPEAT_SHOOT_CODE = 2455564
        const val ONE_SHOOT_ACTION = "$ONE_SHOOT_CODE.one_shoot_action"
        const val REPEAT_SHOOT_ACTION = "$REPEAT_SHOOT_CODE.repeat_shoot_action"
    }

    private val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun createPendingIntent(action: String, requestCode: Int): PendingIntent {

        val intent = Intent(context, receiverClass).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }


    override fun scheduleOne(startTime: Long, overTime: Long): Boolean {
        if (BuildConfig.DEBUG) {
            println("******************* ${this.javaClass.simpleName}.scheduleOne: ${startTime.timeToFormattedString()} ****************")
        }
        val pendingIntent = createPendingIntent(ONE_SHOOT_ACTION, ONE_SHOOT_CODE)
        return try {
            alarmManager.apply {
                setExact(AlarmManager.RTC_WAKEUP, startTime + overTime, pendingIntent)
            }
            true
        }
        catch (e: SecurityException) {
            alarmManager.apply {
                set(AlarmManager.RTC_WAKEUP, startTime + overTime, pendingIntent)
            }
            true
        }
        catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            false
        }
    }

    override fun scheduleRepeat(overTime: Long, interval: Long): Boolean {
        val pendingIntent = createPendingIntent(REPEAT_SHOOT_ACTION, REPEAT_SHOOT_CODE)
        return try {
            alarmManager.apply {
                setRepeating(AlarmManager.RTC, System.currentTimeMillis() + overTime, interval, pendingIntent)
            }
            true
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            false
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun isAlarmSetup(isRepeat: Boolean): PendingIntent? {
        val intent = Intent(context, receiverClass).apply {
            action = when(isRepeat) {
                true -> REPEAT_SHOOT_ACTION
                else -> ONE_SHOOT_ACTION
            }
        }
        val requestCode = when(isRepeat) {
            true -> REPEAT_SHOOT_CODE
            else -> ONE_SHOOT_CODE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }

    override fun cancel(pendingIntent: PendingIntent) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }


}