package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.startOneTimeBackgroundWork
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {

            val scheduler = Scheduler(context.applicationContext)
            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                val nextTime = AppCalendar.getNextFillingTimeByDefaultTimeZone()
                scheduler.scheduleOne(nextTime, 0, Scheduler.SET_NAME)
                context.startOneTimeBackgroundWork()
            }
        }
    }


}