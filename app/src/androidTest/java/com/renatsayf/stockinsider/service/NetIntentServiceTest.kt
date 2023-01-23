package com.renatsayf.stockinsider.service

import android.content.Intent
import android.content.IntentFilter
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.receivers.AlarmReceiver
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.checkTestPort
import com.renatsayf.stockinsider.utils.setAlarm
import com.renatsayf.stockinsider.utils.timeToFormattedString
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class NetIntentServiceTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var activity: TestActivity
    private lateinit var scheduler: Scheduler

    private val testSet = RoomSearchSet(
        queryName = "Microsoft",
        companyName = "Microsoft",
        ticker = "MSFT",
        filingPeriod = 3,
        tradePeriod = 3,
        tradedMin = "",
        tradedMax = "",
        isOfficer = true,
        isDirector = true,
        isTenPercent = true,
        groupBy = 0,
        sortBy = 0
    ).apply {
        target = com.renatsayf.stockinsider.models.Target.Tracking
        isTracked = true
    }

    init {
        checkTestPort()
    }

    @Before
    fun setUp() {
        scenario = rule.scenario
    }

    @After
    fun tearDown() {
        rule.scenario.close()
    }

    @Test
    fun start() {

        scenario.onActivity { activity ->
            NetIntentService.start(activity)
        }

        Thread.sleep(10000)

        var started = NetIntentService.isStarted
        var completed = NetIntentService.isCompleted

        Assert.assertEquals(true, started)
        Assert.assertEquals(false, completed)

        Thread.sleep(15000)

        started = NetIntentService.isStarted
        completed = NetIntentService.isCompleted

        Assert.assertEquals(false, started)
        Assert.assertEquals(true, completed)

        Thread.sleep(10000)
    }

    @Test
    fun start_from_receiver() {

        scenario.onActivity { activity ->
            NetIntentService.injectDependenciesToTest(listOf(testSet))
            scheduler = Scheduler(activity)

            val alarm = activity.setAlarm(
                scheduler = scheduler,
                periodInMinute = 1
            )
            println("******************* ${this::class.java.simpleName} System time: ${System.currentTimeMillis().timeToFormattedString()} ******************")
            Thread.sleep(1000)
            Assert.assertEquals(true, alarm)
            Thread.sleep(2000)
            activity.finish()
        }

        Thread.sleep(75000)
        val pendingIntent = scheduler.isAlarmSetup(Scheduler.SET_NAME, false)
        pendingIntent?.cancel()
    }
}