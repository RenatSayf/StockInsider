package com.renatsayf.stockinsider.service

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.schedule.Scheduler
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.*
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AppWorkerUiTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)
    private lateinit var scenario: ActivityScenario<TestActivity>
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
    fun start_immediately() {

        AppWorker.injectDependenciesToTest(listOf(testSet))
        scenario.onActivity { activity ->
            activity.startOneTimeBackgroundWork(System.currentTimeMillis())
        }

        Thread.sleep(5000)

        var started = AppWorker.isStarted
        var completed = AppWorker.isCompleted

        Assert.assertEquals(true, started)
        Assert.assertEquals(false, completed)

        Thread.sleep(10000)

        while (!AppWorker.isCompleted) {
            Thread.sleep(2000)
        }

        started = AppWorker.isStarted
        completed = AppWorker.isCompleted

        Assert.assertEquals(false, started)
        Assert.assertEquals(true, completed)
    }

    @Test
    fun start_by_scheduled_from_receiver() {

        Scheduler.workPeriodInMinute = 1

        scenario.onActivity { activity ->
            AppWorker.injectDependenciesToTest(listOf(testSet))
            scheduler = Scheduler(activity)

            val alarm = activity.setAlarm(
                scheduler = scheduler,
                periodInMinute = Scheduler.workPeriodInMinute
            )
            println("******************* ${this::class.java.simpleName} System time: ${System.currentTimeMillis().timeToFormattedString()} ******************")
            Thread.sleep(1000)
            Assert.assertEquals(true, alarm)
            Thread.sleep(2000)
            activity.finish()
        }

        while (!AppWorker.isCompleted) {
            Thread.sleep(2000)
        }
        val pendingIntent = scheduler.isAlarmSetup(Scheduler.SET_NAME, false)
        Assert.assertTrue(pendingIntent != null)
        pendingIntent?.cancel()
    }
}