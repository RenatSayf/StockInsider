package com.renatsayf.stockinsider

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.ui.testing.TestReceiver
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.setAlarm
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)
    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var scheduler: Scheduler

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity { activity ->
            scheduler = Scheduler(activity.applicationContext, TestReceiver().javaClass)
            val pendingIntent = scheduler.isAlarmSetup(Scheduler.SET_NAME, false)
            pendingIntent?.let {
                scheduler.cancel(it)
            }
        }
    }

    @After
    fun tearDown() {
        scenario.onActivity {
            val pendingIntent = scheduler.isAlarmSetup(Scheduler.SET_NAME, false)
            pendingIntent?.let {
                scheduler.cancel(it)
            }
        }
        rule.scenario.close()
    }

    @Test
    fun setAlarm_if_not_exist() {

        scenario.onActivity { activity ->
            val actualResult = activity.setAlarm(scheduler)
            Assert.assertEquals(true, actualResult)
        }
        Thread.sleep(5000)
    }

    @Test
    fun setAlarm_if_already_exist() {

        scenario.onActivity { activity ->
            val actualResult = activity.setAlarm(scheduler)
            Assert.assertEquals(true, actualResult)
        }

        Thread.sleep(5000)

        scenario.onActivity { activity ->
            val actualResult = activity.setAlarm(scheduler)
            Assert.assertEquals(false, actualResult)
        }
    }
}