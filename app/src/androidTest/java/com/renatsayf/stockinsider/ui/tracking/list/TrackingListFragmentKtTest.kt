package com.renatsayf.stockinsider.ui.tracking.list

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.ui.dialogs.InfoDialog
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.showInfoDialog
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class TrackingListFragmentKtTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setUp() {
        scenario = rule.scenario
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun showOrNotInfoDialog_on_xiaomi() {

        var actualResult = false
        var actualClick = false

        scenario.onActivity { activity ->

            activity.showOrNotInfoDialog(false, "xiaomi") {
                actualResult = true
                activity.showInfoDialog(
                    title = "Title",
                    message = "This is a message",
                    status = InfoDialog.DialogStatus.EXTENDED_WARNING,
                    callback = {
                        actualClick = when(it) {
                            1 -> true
                            else -> false
                        }
                    }
                )
            }
        }
        Thread.sleep(2000)
        Assert.assertEquals(true, actualResult)
        Thread.sleep(2000)
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
        Thread.sleep(500)
        Assert.assertEquals(true, actualClick)
        Thread.sleep(5000)
    }

    @Test
    fun showOrNotInfoDialog_on_pixel() {

        val actualResult = false
        scenario.onActivity { activity ->

            activity.showOrNotInfoDialog(false, "pixel") {
                Assert.assertTrue(false)
            }
        }
        Thread.sleep(2000)
        Assert.assertEquals(false, actualResult)
        Thread.sleep(2000)
    }

    @Test
    fun showOrNotInfoDialog_on_xiaomi_when_isNotShow_true() {

        val actualResult = false
        scenario.onActivity { activity ->

            activity.showOrNotInfoDialog(
                isNotShow = true,
                manufacturer = "xiaomi"
            ) {
                Assert.assertTrue(false)
            }
        }
        Thread.sleep(2000)
        Assert.assertEquals(false, actualResult)
        Thread.sleep(2000)
    }
}