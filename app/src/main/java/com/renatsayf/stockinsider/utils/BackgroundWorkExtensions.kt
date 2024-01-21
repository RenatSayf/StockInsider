package com.renatsayf.stockinsider.utils

import android.content.Context
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.ui.settings.Constants

fun Context.setAlarm(scheduler: Scheduler, periodInMinute: Long, isTest: Boolean = Constants.TEST_MODE): Long? {
    val pendingIntent = scheduler.isAlarmSetup(false)
    return if (pendingIntent == null) {
        val nextFillingTime = AppCalendar().getNextStartTime(periodInMinute, isTestMode = isTest)
        val formattedString = nextFillingTime.timeToFormattedString()
        "*************** Alarm time will be in $formattedString *********************".printIfDebug()
        when(scheduler.scheduleOne(nextFillingTime, 0)) {
            true -> nextFillingTime
            else -> null
        }
    }
    else {
        null
    }
}

fun Fragment.setAlarm(scheduler: Scheduler, periodInMinute: Long, isTest: Boolean = false): Long? {
    return requireContext().setAlarm(scheduler, periodInMinute, isTest)
}



