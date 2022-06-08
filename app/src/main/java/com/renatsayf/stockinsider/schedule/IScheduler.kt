package com.renatsayf.stockinsider.schedule

import android.app.PendingIntent
import android.icu.util.Calendar

interface IScheduler {
    fun scheduleOne(startTime: Long, overTime: Long = 30000L, name: String): Boolean
    fun scheduleRepeat(overTime: Long, interval: Long, name: String): Boolean
    fun isAlarmSetup(name: String, isRepeat: Boolean): PendingIntent?
    fun cancel(pendingIntent: PendingIntent)

}