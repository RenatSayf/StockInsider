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


open class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context, intent: Intent) {

        val message = "${System.currentTimeMillis().timeToFormattedString()} ->> Alarm has been triggered ******"
        message.printIfDebug()
        context.appendTextToFile(LOGS_FILE_NAME, message)

        val scheduler = Scheduler(context)
        val nextFillingTime = AppCalendar().getNextStartTime()
        scheduler.scheduleOne(nextFillingTime, 0)

        context.startOneTimeBackgroundWork(System.currentTimeMillis())
    }

}