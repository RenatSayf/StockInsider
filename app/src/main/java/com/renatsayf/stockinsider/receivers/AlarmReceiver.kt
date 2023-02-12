@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.getNextStartTime
import com.renatsayf.stockinsider.utils.startOneTimeBackgroundWork


open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null && intent != null) {

            if (BuildConfig.DEBUG) {
                println(" *********** ${this::class.java.simpleName}.onReceive has been triggered *******************")
            }
            val scheduler = Scheduler(context.applicationContext)
            val nextFillingTime = AppCalendar().getNextStartTime()
            scheduler.scheduleOne(nextFillingTime, 0, Scheduler.SET_NAME)

            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                context.startOneTimeBackgroundWork(System.currentTimeMillis())
            }
        }
    }

}