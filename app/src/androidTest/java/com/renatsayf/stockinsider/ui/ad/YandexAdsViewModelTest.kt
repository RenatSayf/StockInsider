package com.renatsayf.stockinsider.ui.ad

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.renatsayf.stockinsider.ui.settings.KEY_IS_AD_DISABLED
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.appPref
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class YandexAdsViewModelTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var viewModel: YandexAdsViewModel

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity {
            viewModel = ViewModelProvider(it)[YandexAdsViewModel::class.java]
        }
    }

    @After
    fun tearDown() {
        rule.scenario.close()
    }

    @Test
    fun loadYandexAd_not_disabled() {

        var isRunning = true

        scenario.onActivity { activity ->

            activity.appPref.edit().putBoolean(KEY_IS_AD_DISABLED, false).apply()
            Thread.sleep(5000)

            viewModel.loadInterstitialAd(adId = AdsId.TEST_INTERSTITIAL_AD_ID)
            viewModel.interstitialAd.observe(activity) { result ->
                result.onSuccess { ad ->
                    ad.show(activity)
                    Thread.sleep(5000)
                    isRunning = false
                }
                result.onFailure { exception ->
                    throw exception
                }
            }
        }
        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Suppress("DEPRECATION")
    private fun setWiFiDataEnabled(context: Context, enabled: Boolean) {

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled
    }
}