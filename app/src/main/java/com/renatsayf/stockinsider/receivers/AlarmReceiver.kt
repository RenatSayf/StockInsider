@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.service.NetIntentService
import com.renatsayf.stockinsider.service.NetworkService
import com.renatsayf.stockinsider.service.notifications.RequestNotification
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.getNextStartTime
import dagger.hilt.android.AndroidEntryPoint


open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (BuildConfig.DEBUG) {
            println(" *********** ${this::class.java.simpleName}.onReceive has been triggered *******************")
        }

        if (context != null && intent != null) {

            val scheduler = Scheduler(context.applicationContext)
            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                val nextTime = getNextStartTime(3)
                scheduler.scheduleOne(nextTime, 0, Scheduler.SET_NAME)

                NetIntentService.start(context)
            }
        }
    }


}