package com.renatsayf.stockinsider.schedule

import android.app.PendingIntent
import android.icu.util.Calendar

interface IScheduler {
    fun scheduleOne(startTime: Long, overTime: Long = 30000L): Boolean
    fun scheduleRepeat(overTime: Long, interval: Long): Boolean
    fun isAlarmSetup(isRepeat: Boolean): PendingIntent?
    fun cancel(pendingIntent: PendingIntent)

}