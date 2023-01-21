package com.renatsayf.stockinsider.service

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.network.MockApi
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.*
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AppWorkerUiTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)
    private lateinit var scenario: ActivityScenario<TestActivity>

    private lateinit var dao: AppDao
    private lateinit var repository: INetRepository

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
        scenario.onActivity { activity ->
            dao = RoomDataBaseModule.provideRoomDataBase(activity)
            repository = NetRepositoryModule.provideSearchRequest(MockApi(activity))
            activity.cancelBackgroundWork()
        }
    }

    @After
    fun tearDown() {
        try {
            rule.scenario.onActivity { activity ->
                activity.cancelBackgroundWork()
            }
            rule.scenario.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun startOneTimeBackgroundWork_setup_and_cancel_work() {

        scenario.onActivity { activity ->

            val function = ServiceNotification.notify
            AppWorker.injectDependenciesToTest(dao, repository, listOf(testSet), function)

            val startTime = AppCalendar.currentTimeByDefaultTimeZone + 120000
            val operation = activity.startOneTimeBackgroundWork(startTime)
            val done = operation.result.isDone
            Assert.assertEquals(false, done)

            Thread.sleep(2000)

            var actualResult = activity.haveWorkTask()
            Assert.assertEquals(true, actualResult)

            val state = activity.cancelBackgroundWork()
            state.observe(activity) { s ->
                println("************************* state: $s *************************")
                Assert.assertEquals("SUCCESS", s.toString())
            }
            Thread.sleep(2000)

            actualResult = activity.haveWorkTask()
            Assert.assertEquals(false, actualResult)
        }

        Thread.sleep(15000)
    }

    @Test
    fun startOneTimeBackgroundWork_setup_and_run() {

        scenario.onActivity { activity ->

            val function = ServiceNotification.notify
            AppWorker.injectDependenciesToTest(dao, repository, listOf(testSet), function)

            val startTime = AppCalendar.currentTimeByDefaultTimeZone + 2000
            activity.startOneTimeBackgroundWork(startTime)

            Thread.sleep(2000)

            val actualResult = activity.haveWorkTask()
            Assert.assertEquals(false, actualResult)
        }

        Thread.sleep(25000)

        scenario.onActivity { activity ->
            val actualResult = activity.haveWorkTask()
            Assert.assertEquals(true, actualResult)
        }

        Thread.sleep(2000)
    }

    @Test
    fun startOneTimeBackgroundWork_setup_and_finish_activity() {

        scenario.onActivity { activity ->
            val function = ServiceNotification.notify
            AppWorker.injectDependenciesToTest(dao, repository, listOf(testSet), function)

            val startTime = AppCalendar.currentTimeByDefaultTimeZone + 2000
            activity.startOneTimeBackgroundWork(startTime)

            Thread.sleep(2000)

            activity.finish()
        }

        Thread.sleep(30000)
    }
}