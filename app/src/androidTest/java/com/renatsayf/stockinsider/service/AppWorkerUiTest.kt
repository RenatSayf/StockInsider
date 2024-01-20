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

        AppWorker.injectDependenciesToTest(listOf(testSet), trackingPeriodInMinutes = 0, isTestMode = true)
        scenario.onActivity { activity ->
            activity.startOneTimeBackgroundWork(System.currentTimeMillis())
        }

        Thread.sleep(5000)

        var state = AppWorker.state
        Assert.assertEquals(AppWorker.State.Started, state)

        Thread.sleep(10000)

        while (state == AppWorker.State.Started) {
            Thread.sleep(2000)
            state = AppWorker.state
        }

        Assert.assertEquals(AppWorker.State.Completed, state)
    }

    @Test
    fun start_by_scheduled_from_receiver() {

        val workPeriodInMinute: Long = 1

        scenario.onActivity { activity ->
            AppWorker.injectDependenciesToTest(listOf(testSet), trackingPeriodInMinutes = workPeriodInMinute, isTestMode = true)
            scheduler = Scheduler(activity)

            val nextTime = activity.setAlarm(
                scheduler = scheduler,
                periodInMinute = workPeriodInMinute,
                isTest = true
            )
            println("******************* ${this::class.java.simpleName} System time: ${System.currentTimeMillis().timeToFormattedString()} ******************")
            Thread.sleep(1000)
            Assert.assertTrue(nextTime != null)
            Thread.sleep(2000)
            activity.finish()
        }

        var state = AppWorker.state
        while (state != AppWorker.State.Completed && state != AppWorker.State.Failed) {
            Thread.sleep(2000)
            state = AppWorker.state
            if (state is AppWorker.State.Failed) {
                Assert.assertTrue(false)
            }
        }
        val pendingIntent = scheduler.isAlarmSetup(false)
        Assert.assertTrue(pendingIntent != null)
        pendingIntent?.cancel()
    }
}