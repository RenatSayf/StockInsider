package com.renatsayf.stockinsider.service.notifications

import android.app.Notification
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.ui.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class RequestNotificationTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private var notification: RequestNotification? = null

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity { activity ->
            notification = RequestNotification(activity)
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun show() {

        val actualResult = notification?.create()
        notification?.show()
        Assert.assertTrue(actualResult is Notification)
        Thread.sleep(10000)
        notification?.cancel()
    }
}