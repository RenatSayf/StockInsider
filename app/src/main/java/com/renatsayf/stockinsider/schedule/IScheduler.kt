package com.renatsayf.stockinsider.schedule

import android.app.PendingIntent
import android.icu.util.Calendar

interface IScheduler {
    fun scheduleOne(startTime: Long, overTime: Long = 30000L, setName: String, requestCode: Int): Boolean
    fun scheduleRepeat(overTime: Long, interval: Long, setName: String, requestCode: Int): Boolean
    fun isAlarmSetup(setName: String, isRepeat: Boolean, requestCode: Int): PendingIntent?
    fun cancel(pendingIntent: PendingIntent)

}