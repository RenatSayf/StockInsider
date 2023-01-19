@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.service.NetworkService
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.startOneTimeBackgroundWork
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor


@AndroidEntryPoint
open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {

            val scheduler = Scheduler(context.applicationContext)
            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                val nextTime = AppCalendar.getNextFillingTimeByDefaultTimeZone(
                    workerPeriod = FireBaseViewModel.workerPeriod
                )
                scheduler.scheduleOne(nextTime, 0, Scheduler.SET_NAME)

                val networkService = NetworkService()
                networkService.start(context)
            }
        }
    }


}