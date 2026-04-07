@file:Suppress("UnnecessaryVariable")

package com.renatsayf.stockinsider.schedule

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.utils.printStackTraceIfDebug
import com.renatsayf.stockinsider.utils.timeToFormattedString
import javax.inject.Inject


class Scheduler @Inject constructor(
    private val context: Context
) : IScheduler {

    companion object {
        private const val ONE_SHOOT_CODE = 2455563
        private const val REPEAT_SHOOT_CODE = 2455564
        const val ONE_SHOOT_ACTION = "$ONE_SHOOT_CODE.one_shoot_action"
        const val REPEAT_SHOOT_ACTION = "$REPEAT_SHOOT_CODE.repeat_shoot_action"
    }

    private val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun createPendingIntent(): PendingIntent {

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ONE_SHOOT_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }


    override fun scheduleOne(startTime: Long, overTime: Long): Boolean {
        if (BuildConfig.DEBUG) {
            println("******************* ${this.javaClass.simpleName}.scheduleOne: ${startTime.timeToFormattedString()} ****************")
        }
        val pendingIntent = createPendingIntent()
        return try {
            alarmManager.apply {
                setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime + overTime, pendingIntent)
            }
            true
        }
        catch (e: SecurityException) {
            alarmManager.apply {
                setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime + overTime, pendingIntent)
            }
            e.printStackTraceIfDebug()
            true
        }
        catch (e: Exception) {
            e.printStackTraceIfDebug()
            false
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun isAlarmSetup(isRepeat: Boolean): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ONE_SHOOT_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        return pendingIntent
    }

    override fun cancel(pendingIntent: PendingIntent) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }


}