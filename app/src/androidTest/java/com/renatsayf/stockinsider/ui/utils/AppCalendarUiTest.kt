package com.renatsayf.stockinsider.ui.utils

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.getNextStartTime
import com.renatsayf.stockinsider.utils.timeToFormattedString
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.TimeZone


@RunWith(AndroidJUnit4ClassRunner::class)
class AppCalendarUiTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private var systemTime: Long = 0

    @Before
    fun setUp() {
        scenario = rule.scenario
        systemTime = Calendar.getInstance().apply {
            timeInMillis += TimeZone.getDefault().rawOffset
        }.timeInMillis
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun getNextStartTime_period_0() {

        val periodInMinute = FireBaseConfig.trackingPeriod
        val startTimeLong = AppCalendar().getNextStartTime(periodInMinute, isTestMode = true)
        val startTimeStr = startTimeLong.timeToFormattedString()
        println("****************** getNextStartTime($periodInMinute): $startTimeStr *******************")
        println("****************** systemTime: ${System.currentTimeMillis().timeToFormattedString()} *******************")

        val offset = startTimeLong - System.currentTimeMillis()
        val offsetInMinute = offset / 1000 / 60
        println("****************** offset: $offsetInMinute *******************")

        val workerPeriod = FireBaseConfig.trackingPeriod
        val actualResult = offsetInMinute in (workerPeriod - 1)..(workerPeriod + 1)
        Assert.assertEquals(true, actualResult)

        Thread.sleep(5000)
    }

    @Test
    fun getNextStartTime_period_not_0() {

        val periodInMinute = 30L
        val startTimeLong = AppCalendar().getNextStartTime(periodInMinute, isTestMode = true)
        val startTimeStr = startTimeLong.timeToFormattedString()
        println("****************** getNextStartTime($periodInMinute): $startTimeStr *******************")
        println("****************** systemTime: ${System.currentTimeMillis().timeToFormattedString()} *******************")

        val offset = startTimeLong - System.currentTimeMillis()
        val offsetInMinute = offset / 1000 / 60
        println("****************** offset: $offsetInMinute *******************")

        val actualResult = offsetInMinute in 28..31
        Assert.assertEquals(true, actualResult)

        Thread.sleep(5000)
    }
}