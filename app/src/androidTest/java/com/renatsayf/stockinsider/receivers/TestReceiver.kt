package com.renatsayf.stockinsider.receivers

import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.service.ServiceNotification
import org.junit.Assert

class TestReceiver : AlarmReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {

            if (intent.action == Scheduler.ONE_SHOOT_ACTION || intent.action == Scheduler.REPEAT_SHOOT_ACTION) {
                ServiceNotification.notify(context, 0, null)
            }
            Assert.assertEquals(intent.action, Scheduler.ONE_SHOOT_ACTION)
        }
    }
}