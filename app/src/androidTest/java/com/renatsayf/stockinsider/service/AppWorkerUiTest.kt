package com.renatsayf.stockinsider.service

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.work.Operation
import androidx.work.await
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.network.MockApi
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import com.renatsayf.stockinsider.utils.checkTestPort
import com.renatsayf.stockinsider.utils.startOneTimeBackgroundWork
import kotlinx.coroutines.runBlocking
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

    @Before
    fun setUp() {
        checkTestPort()
        scenario = rule.scenario
        scenario.onActivity { activity ->
            dao = RoomDataBaseModule.provideRoomDataBase(activity)
            repository = NetRepositoryModule.provideSearchRequest(MockApi(activity))
        }
    }

    @After
    fun tearDown() {
        rule.scenario.close()
    }

    @Test
    fun startOneTimeBackgroundWork() {

        scenario.onActivity { activity ->

            runBlocking {

                val function = ServiceNotification.notify
                AppWorker.injectDependenciesToTest(dao, repository, listOf(testSet), function)
                val operation = activity.startOneTimeBackgroundWork()
                val state = operation.state
                state.observe(activity) { s ->
                    println("************* state = $s ********************")
                }
                val result = operation.result
                val success = result.await()
                Assert.assertEquals(Operation.SUCCESS, success)
            }
        }
        Thread.sleep(3000)
    }
}