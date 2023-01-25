package com.renatsayf.stockinsider.utils

import android.content.Context
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.schedule.Scheduler

fun Context.setAlarm(scheduler: Scheduler, periodInMinute: Long): Boolean {
    val pendingIntent = scheduler.isAlarmSetup(Scheduler.SET_NAME, false)
    return if (pendingIntent == null) {
        val nextFillingTime = AppCalendar().getNextStartTime(periodInMinute)
        val formattedString = nextFillingTime.timeToFormattedString()
        if (BuildConfig.DEBUG) println("*************** Alarm time will be in $formattedString *********************")
        scheduler.scheduleOne(nextFillingTime, 0, Scheduler.SET_NAME)
    }
    else {
        false
    }
}

fun Fragment.setAlarm(scheduler: Scheduler, periodInMinute: Long): Boolean {
    return requireContext().setAlarm(scheduler, periodInMinute)
}



