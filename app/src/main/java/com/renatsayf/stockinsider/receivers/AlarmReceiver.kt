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
import java.util.concurrent.TimeUnit
import kotlin.random.Random


open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null && intent != null) {

            if (BuildConfig.DEBUG) {
                println(" *********** ${this::class.java.simpleName}.onReceive has been triggered *******************")
            }

            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                val scheduler = Scheduler(context.applicationContext)
                val nextFillingTime = AppCalendar().getNextStartTime()
                val randomOverTime = TimeUnit.MINUTES.toMillis(Random.nextLong(0, 12))
                scheduler.scheduleOne(nextFillingTime, randomOverTime)
                context.startOneTimeBackgroundWork(System.currentTimeMillis())
            }
        }
    }

}