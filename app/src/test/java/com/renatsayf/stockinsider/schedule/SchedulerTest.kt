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
        val isOne = scheduler.scheduleOne(startTime = System.currentTimeMillis(), overTime = 60000, setName = setName, requestCode = setName.hashCode())

        Assert.assertTrue(isOne)

        var pendingIntent = scheduler.isAlarmSetup(setName, requestCode = setName.hashCode(), isRepeat = false)

        Assert.assertTrue(pendingIntent != null)

        if (pendingIntent != null) {
            scheduler.cancel(pendingIntent)

            pendingIntent = scheduler.isAlarmSetup(setName, requestCode = setName.hashCode(), isRepeat = false)
            Assert.assertTrue(pendingIntent == null)
        }
        else throw NullPointerException("********** pendingIntent is null *************")
    }

    @Test
    fun scheduleRepeat() {
        val setName = "default_set"

        val isRepeat = scheduler.scheduleRepeat(overTime = 30000, interval = 120000, setName = setName, requestCode = setName.hashCode())

        Assert.assertTrue(isRepeat)

        var pendingIntent = scheduler.isAlarmSetup(setName, requestCode = setName.hashCode(), isRepeat = true)

        if (pendingIntent != null) {
            scheduler.cancel(pendingIntent)

            pendingIntent = scheduler.isAlarmSetup(setName, requestCode = setName.hashCode(), isRepeat = true)
            Assert.assertTrue(pendingIntent == null)
        }
        else throw NullPointerException("********** pendingIntent is null *************")
    }

    @Test
    fun scheduleRepeat_two_alarms() {
        val setName1 = "name1"
        val setName2 = "name2"

        val isRepeat1 = scheduler.scheduleRepeat(overTime = 30000, interval = 120000, setName = setName1, requestCode = setName1.hashCode())
        val isRepeat2 = scheduler.scheduleRepeat(overTime = 30000, interval = 120000, setName = setName2, requestCode = setName2.hashCode())

        Assert.assertTrue(isRepeat1 == isRepeat2)

        var pendingIntent1 = scheduler.isAlarmSetup(setName1, requestCode = setName1.hashCode(), isRepeat = true)
        var pendingIntent2 = scheduler.isAlarmSetup(setName2, requestCode = setName2.hashCode(), isRepeat = true)

        Assert.assertTrue(pendingIntent1 != null && pendingIntent2 != null)
        Assert.assertTrue(pendingIntent1 != pendingIntent2)

        if (pendingIntent1 != null) {
            scheduler.cancel(pendingIntent1)

            pendingIntent1 = scheduler.isAlarmSetup(setName1, requestCode = setName1.hashCode(), isRepeat = true)
            Assert.assertTrue(pendingIntent1 == null)
        }
        else throw NullPointerException("********** pendingIntent is null *************")

        if (pendingIntent2 != null) {
            scheduler.cancel(pendingIntent2)

            pendingIntent2 = scheduler.isAlarmSetup(setName2, requestCode = setName2.hashCode(), isRepeat = true)
            Assert.assertTrue(pendingIntent2 == null)
        }
        else throw NullPointerException("********** pendingIntent is null *************")
    }
}