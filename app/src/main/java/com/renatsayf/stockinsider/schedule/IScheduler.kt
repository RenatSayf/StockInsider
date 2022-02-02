package com.renatsayf.stockinsider.schedule

import android.app.PendingIntent
import android.icu.util.Calendar

interface IScheduler {
    fun scheduleOne(startTime: Long, overTime: Long = 30000L, setName: String): Boolean
    fun scheduleRepeat(overTime: Long, interval: Long, setName: String): Boolean
    fun isAlarmSetup(setName: String, isRepeat: Boolean = false): PendingIntent?
    fun cancel(pendingIntent: PendingIntent): Boolean

}