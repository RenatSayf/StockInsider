package com.renatsayf.stockinsider.ui.testing

import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import org.junit.Assert

class TestReceiver : AlarmReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {

            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {

                ServiceNotification.notify(context, "This is test notification", null)
            }
            Assert.assertEquals(intent.action, Scheduler.ONE_SHOOT_ACTION)
        }
    }
}