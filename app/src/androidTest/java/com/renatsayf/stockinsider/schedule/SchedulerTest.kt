package com.renatsayf.stockinsider.schedule

import android.app.PendingIntent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.ui.testing.TestReceiver
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class SchedulerTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    private lateinit var scheduler: Scheduler

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity { activity ->
            scheduler = Scheduler(activity.applicationContext, TestReceiver().javaClass)
        }
    }

    @After
    fun tearDown() {
        scenario.onActivity {
            val pendingIntent = scheduler.isAlarmSetup(false)
            pendingIntent?.let {
                scheduler.cancel(it)
            }
        }
        rule.scenario.close()
    }

    @Test
    fun scheduleOne() {

        scenario.onActivity {
            val result = scheduler.scheduleOne(System.currentTimeMillis() + 10000, 0)
            Assert.assertTrue(result)
        }

        Thread.sleep(5000)

        var pendingIntent = scheduler.isAlarmSetup(false)
        Assert.assertTrue(pendingIntent is PendingIntent)

        Thread.sleep(4000)
        scheduler.cancel(pendingIntent!!)

        Thread.sleep(2000)
        pendingIntent = scheduler.isAlarmSetup(false)
        Assert.assertEquals(null, pendingIntent)
        Thread.sleep(2000)
    }

}