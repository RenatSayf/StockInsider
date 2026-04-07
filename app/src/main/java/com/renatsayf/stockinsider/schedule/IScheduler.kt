package com.renatsayf.stockinsider.schedule

import android.app.PendingIntent

interface IScheduler {
    fun scheduleOne(startTime: Long, overTime: Long = 30000L): Boolean
    fun isAlarmSetup(isRepeat: Boolean): PendingIntent?
    fun cancel(pendingIntent: PendingIntent)

}