package com.renatsayf.stockinsider.ui.utils

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.ui.testing.TestActivity
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
class BackgroundWorkExtensionsKtTest {

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

        val startTime = getNextStartTime(0)
        val stringTime = startTime.timeToFormattedString()
        println("****************** getNextStartTime(0): $stringTime *******************")
        println("****************** systemTime: ${systemTime.timeToFormattedString()} *******************")

        val offset = startTime - systemTime
        val offsetInMinute = offset / 1000 / 60
        println("****************** offset: $offsetInMinute *******************")

        val actualResult = offsetInMinute in 58..61
        Assert.assertEquals(true, actualResult)

        Thread.sleep(5000)
    }

    @Test
    fun getNextStartTime_period_not_0() {

        val periodInMinute = 1L
        val startTime = getNextStartTime(periodInMinute)
        val stringTime = startTime.timeToFormattedString()
        println("****************** getNextStartTime(0): $stringTime *******************")
        println("****************** systemTime: ${systemTime.timeToFormattedString()} *******************")

        val offset = startTime - systemTime
        val offsetInMinute = offset / 1000 / 60
        println("****************** offset: $offsetInMinute *******************")

        val actualResult = offsetInMinute in 1..2
        Assert.assertEquals(true, actualResult)

        Thread.sleep(5000)
    }
}