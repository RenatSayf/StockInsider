package com.renatsayf.stockinsider.utils

import android.app.Activity
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.schedule.Scheduler

fun Activity.setAlarm(scheduler: Scheduler, periodInMinute: Long): Boolean {
    val pendingIntent = scheduler.isAlarmSetup(Scheduler.SET_NAME, false)
    return if (pendingIntent == null) {
        val nextFillingTime = getNextStartTime(periodInMinute)
        val formattedString = nextFillingTime.timeToFormattedString()
        if (BuildConfig.DEBUG) println("*************** Alarm time will be in $formattedString *********************")
        scheduler.scheduleOne(nextFillingTime, 0, Scheduler.SET_NAME)
    }
    else {
        false
    }
}

fun Fragment.setAlarm(scheduler: Scheduler, periodInMinute: Long): Boolean {
    return requireActivity().setAlarm(scheduler, periodInMinute)
}

fun Any.getNextStartTime(periodInMinute: Long = 0L): Long {
    return if (BuildConfig.DEBUG && periodInMinute == 0L) {
        AppCalendar.getNextTestTimeByUTCTimeZone(
            workerPeriod = FireBaseConfig.workerPeriod
        )
    }
    else if (BuildConfig.DEBUG && periodInMinute > 0L) {
        AppCalendar.getNextTestTimeByUTCTimeZone(periodInMinute)
    }
    else AppCalendar.getNextFillingTimeByUTCTimeZone(
        workerPeriod = FireBaseConfig.workerPeriod,
    )
}