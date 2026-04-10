@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.schedule.setReminderAlarm
import com.renatsayf.stockinsider.utils.LOGS_FILE_NAME
import com.renatsayf.stockinsider.utils.appendTextToFile
import com.renatsayf.stockinsider.utils.printIfDebug
import com.renatsayf.stockinsider.utils.startOneTimeBackgroundWork
import com.renatsayf.stockinsider.utils.timeToFormattedString


class AlarmReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = this::class.java.name
    }

    override fun onReceive(context: Context, intent: Intent) {

        val message = "${System.currentTimeMillis().timeToFormattedString()} ->> Alarm has been triggered ******"
        message.printIfDebug()
        context.appendTextToFile(LOGS_FILE_NAME, message)

        context.setReminderAlarm(FireBaseConfig.trackingPeriod)

        context.startOneTimeBackgroundWork(System.currentTimeMillis())
    }

}