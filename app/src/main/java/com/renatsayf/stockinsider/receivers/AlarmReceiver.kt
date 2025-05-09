@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.LOGS_FILE_NAME
import com.renatsayf.stockinsider.utils.appendTextToFile
import com.renatsayf.stockinsider.utils.getNextStartTime
import com.renatsayf.stockinsider.utils.printIfDebug
import com.renatsayf.stockinsider.utils.startOneTimeBackgroundWork
import com.renatsayf.stockinsider.utils.timeToFormattedString
import java.util.concurrent.TimeUnit
import kotlin.random.Random


open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null && intent != null) {

            val message = "${System.currentTimeMillis().timeToFormattedString()} ->> Alarm has been triggered ******"
            message.printIfDebug()
            context.appendTextToFile(LOGS_FILE_NAME, message)

            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                val scheduler = Scheduler(context.applicationContext)
                val nextFillingTime = AppCalendar().getNextStartTime()
                val randomOverTime = TimeUnit.MINUTES.toMillis(Random.nextLong(0, 12))
                scheduler.scheduleOne(nextFillingTime, randomOverTime)

                val nextCheckTime = nextFillingTime + randomOverTime
                context.appendTextToFile(
                    LOGS_FILE_NAME,
                    content = "${System.currentTimeMillis().timeToFormattedString()} ->> next check time ${nextCheckTime.timeToFormattedString()}"
                )

                context.startOneTimeBackgroundWork(System.currentTimeMillis())
            }
        }
    }

}