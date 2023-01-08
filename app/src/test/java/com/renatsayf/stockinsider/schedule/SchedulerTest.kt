package com.renatsayf.stockinsider.schedule

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 31)
class SchedulerTest {

    private lateinit var scheduler: IScheduler

    @Before
    fun setUp() {

        scheduler = Scheduler(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
    }

    @Test
    fun scheduleOne() {
        val setName = "default_set"
        val isOne = scheduler.scheduleOne(startTime = System.currentTimeMillis(), overTime = 60000, name = setName)

        Assert.assertTrue(isOne)

        var pendingIntent = scheduler.isAlarmSetup(setName, isRepeat = false)

        Assert.assertTrue(pendingIntent != null)

        if (pendingIntent != null) {
            scheduler.cancel(pendingIntent)

            pendingIntent = scheduler.isAlarmSetup(setName, isRepeat = false)
            Assert.assertTrue(pendingIntent == null)
        }
        else throw NullPointerException("********** pendingIntent is null *************")
    }

    @Test
    fun scheduleRepeat() {
        val setName = "default_set"

        val isRepeat = scheduler.scheduleRepeat(overTime = 30000, interval = 120000, name = setName)

        Assert.assertTrue(isRepeat)

        var pendingIntent = scheduler.isAlarmSetup(setName, isRepeat = true)

        if (pendingIntent != null) {
            scheduler.cancel(pendingIntent)

            pendingIntent = scheduler.isAlarmSetup(setName, isRepeat = true)
            Assert.assertTrue(pendingIntent == null)
        }
        else throw NullPointerException("********** pendingIntent is null *************")
    }

}