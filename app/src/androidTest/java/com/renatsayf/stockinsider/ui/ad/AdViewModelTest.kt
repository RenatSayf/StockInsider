package com.renatsayf.stockinsider.ui.ad

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.renatsayf.stockinsider.ui.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AdViewModelTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setUp() {
        scenario = rule.scenario
    }

    @After
    fun tearDown() {
        rule.scenario.close()
    }

    @Test
    fun loadAd() {

        scenario.onActivity { activity ->

            val viewModel = ViewModelProvider(activity)[AdViewModel::class.java]

            viewModel.loadAd(0, false, object : AdViewModel.Listener {
                override fun onGoogleAdLoaded(ad: InterstitialAd, isOnExit: Boolean) {
                    ad.show(activity)
                    Assert.assertTrue(true)
                }

                override fun onYandexAdLoaded(ad: com.yandex.mobile.ads.interstitial.InterstitialAd, isOnExit: Boolean) {
                    ad.show()
                    Assert.assertTrue(true)
                }
            })
        }
        Thread.sleep(20000)
    }
}