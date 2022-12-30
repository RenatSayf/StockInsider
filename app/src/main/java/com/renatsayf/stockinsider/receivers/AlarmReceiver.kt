package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import com.renatsayf.stockinsider.schedule.IScheduler
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.service.ServiceNotification
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.timeToFormattedString
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {

            val period = FireBaseViewModel.workerPeriod
            val scheduler = Scheduler(context)
            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                ServiceNotification.notify(context, 0, null)
                val nextTime = AppCalendar.getNextFillingTime(1)
                val one = scheduler.scheduleOne(nextTime, 0, Scheduler.SET_NAME)
                if (one) {
                    println("****************** Alarm is setup in ${nextTime.timeToFormattedString()} ************************************")
                }
                else {
                    println("*************************** Alarm is null ***************************")
                }
            }
        }
    }


}