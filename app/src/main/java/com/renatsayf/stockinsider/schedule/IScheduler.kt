package com.renatsayf.stockinsider.schedule

import android.icu.util.Calendar

interface IScheduler {
    fun scheduleOne(startTime: Long, overTime: Long = 30000L, setName: String): Boolean
    fun scheduleRepeat(overTime: Long, interval: Long, setName: String): Boolean
    fun isAlarmSetup(setName: String): Boolean
    fun cancel(requestCode: Int, action: String, setName: String)

}