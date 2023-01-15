package com.renatsayf.stockinsider.ui.ad

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.appPref
import com.yandex.mobile.ads.common.AdRequestError
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

            setWiFiDataEnabled(activity, true)
            Thread.sleep(5000)

            activity.appPref.edit().putBoolean(AdViewModel.KEY_IS_AD_DISABLED, false).apply()

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

                override fun onGoogleAdFailed(error: LoadAdError) {
                    Assert.assertTrue(false)
                }

                override fun onYandexAdFailed(error: AdRequestError) {
                    Assert.assertTrue(false)
                }

                override fun onAdDisabled() {
                    Assert.assertTrue(true)
                }
            })
        }
        Thread.sleep(25000)
    }

    @Test
    fun loadAd_When_ad_is_disabled() {

        scenario.onActivity { activity ->

            setWiFiDataEnabled(activity, true)
            Thread.sleep(5000)

            val isDisable = true
            activity.appPref.edit().putBoolean(AdViewModel.KEY_IS_AD_DISABLED, isDisable).apply()
            val viewModel = ViewModelProvider(activity)[AdViewModel::class.java]

            viewModel.loadAd(0, false, object : AdViewModel.Listener {
                override fun onGoogleAdLoaded(ad: InterstitialAd, isOnExit: Boolean) {
                    Assert.assertTrue(false)
                }

                override fun onYandexAdLoaded(ad: com.yandex.mobile.ads.interstitial.InterstitialAd, isOnExit: Boolean) {
                    Assert.assertTrue(false)
                }

                override fun onGoogleAdFailed(error: LoadAdError) {
                    Assert.assertTrue(false)
                }

                override fun onYandexAdFailed(error: AdRequestError) {
                    Assert.assertTrue(false)
                }

                override fun onAdDisabled() {
                    Assert.assertTrue(true)
                    activity.appPref.edit().putBoolean(AdViewModel.KEY_IS_AD_DISABLED, !isDisable).apply()
                }
            })
        }
        Thread.sleep(10000)
    }

    @Test
    fun loadAd_when_internet_is_disabled() {

        scenario.onActivity { activity ->

            setWiFiDataEnabled(activity, false)
            Thread.sleep(5000)

            activity.appPref.edit().putBoolean(AdViewModel.KEY_IS_AD_DISABLED, false).apply()

            val viewModel = ViewModelProvider(activity)[AdViewModel::class.java]
            Thread.sleep(5000)

            viewModel.loadAd(0, false, object : AdViewModel.Listener {
                override fun onGoogleAdLoaded(ad: InterstitialAd, isOnExit: Boolean) {
                    Assert.assertTrue(false)
                    setWiFiDataEnabled(activity, true)
                    Thread.sleep(5000)
                }

                override fun onYandexAdLoaded(
                    ad: com.yandex.mobile.ads.interstitial.InterstitialAd,
                    isOnExit: Boolean
                ) {
                    Assert.assertTrue(false)
                    setWiFiDataEnabled(activity, true)
                    Thread.sleep(5000)
                }

                override fun onGoogleAdFailed(error: LoadAdError) {
                    println("********************** onYandexAdFailed error: ${error.message} *************************")
                    Assert.assertTrue(true)
                    setWiFiDataEnabled(activity, true)
                    Thread.sleep(5000)
                }

                override fun onYandexAdFailed(error: AdRequestError) {
                    println("********************** onYandexAdFailed error: ${error.description} *************************")
                    Assert.assertTrue(true)
                    setWiFiDataEnabled(activity, true)
                    Thread.sleep(5000)
                }

                override fun onAdDisabled() {
                    Assert.assertTrue(false)
                    setWiFiDataEnabled(activity, true)
                    Thread.sleep(5000)
                }
            })
        }
        Thread.sleep(30000)
    }

    @Suppress("DEPRECATION")
    private fun setWiFiDataEnabled(context: Context, enabled: Boolean) {

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enabled
    }
}