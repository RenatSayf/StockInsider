package com.renatsayf.stockinsider.schedule

import android.app.PendingIntent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.TestActivity
import com.renatsayf.stockinsider.receivers.TestReceiver
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class SchedulerTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private var scenario: ActivityScenario<TestActivity>? = null

    @Before
    fun setUp() {
        scenario = rule.scenario
    }

    @After
    fun tearDown() {
        rule.scenario.close()
    }

    @Test
    fun scheduleOne() {

        var scheduler: Scheduler? = null

        scenario?.onActivity { activity ->

            scheduler = Scheduler(activity, TestReceiver().javaClass)
            val result = scheduler!!.scheduleOne(System.currentTimeMillis() + 5000, 0, "XXX")
            Assert.assertTrue(result)
        }

        Thread.sleep(5000)

        scenario?.onActivity {
            var pendingIntent = scheduler!!.isAlarmSetup("XXX", false)
            Assert.assertTrue(pendingIntent is PendingIntent)

            scheduler!!.cancel(pendingIntent!!)
            pendingIntent = scheduler!!.isAlarmSetup("XXX", false)
            Assert.assertEquals(null, pendingIntent)
        }
        Thread.sleep(10000)
    }

}