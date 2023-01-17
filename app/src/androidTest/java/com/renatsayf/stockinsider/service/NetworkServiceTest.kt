package com.renatsayf.stockinsider.service

import android.content.ComponentName
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.checkTestPort
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class NetworkServiceTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var activity: TestActivity


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
        scenario.onActivity {
            activity = it
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun onCreate() {

        NetworkService.injectDependenciesToTest(listOf(testSet))
        val networkService = NetworkService()

        val componentName = networkService.start(activity)
        Assert.assertTrue(componentName is ComponentName)

        val isStarted = NetworkService.isStarted
        Assert.assertEquals(false, isStarted)

        Thread.sleep(35000)
        val completed = NetworkService.isCompleted
        Assert.assertEquals(true, completed)

        val started = NetworkService.isStarted
        Assert.assertEquals(false, started)

        Thread.sleep(3000)
    }
}