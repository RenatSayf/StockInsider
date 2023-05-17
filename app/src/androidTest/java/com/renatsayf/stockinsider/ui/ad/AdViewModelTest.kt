package com.renatsayf.stockinsider.ui.ad

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.renatsayf.stockinsider.ui.testing.TestActivity
import com.renatsayf.stockinsider.utils.appPref
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class AdViewModelTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var viewModel: AdViewModel

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity {
            viewModel = ViewModelProvider(it)[AdViewModel::class.java]
        }
    }

    @After
    fun tearDown() {
        rule.scenario.close()
    }

    @Test
    fun loadGoogleAd_not_disabled() {

        var isRunning = true

        scenario.onActivity { activity ->

            Thread.sleep(5000)
            activity.appPref.edit().putBoolean(AdViewModel.KEY_IS_AD_DISABLED, false).apply()
            Thread.sleep(5000)

            viewModel.loadGoogleAd(indexId = 0, isOnExit = false, listener = object : AdViewModel.GoogleAdListener {
                override fun onGoogleAdLoaded(
                    ad: com.google.android.gms.ads.interstitial.InterstitialAd,
                    isOnExit: Boolean
                ) {
                    Assert.assertTrue(true)
                    ad.apply {
                        show(activity)
                        fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdShowedFullScreenContent() {
                                println("******************* Ad is shown *****************************")
                                Assert.assertTrue(true)
                                isRunning = false
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                println("******************* Ad failed to show *****************************")
                                Assert.assertTrue(false)
                                isRunning = false
                            }
                        }
                    }
                }

                override fun onGoogleAdFailed(error: LoadAdError) {
                    println("******************* LoadAdError: ${error.message} *****************************")
                    Assert.assertTrue(true)
                    isRunning = false
                }
            })
        }
        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun loadYandexAd_not_disabled() {

        var isRunning = true

        scenario.onActivity { activity ->

            activity.appPref.edit().putBoolean(AdViewModel.KEY_IS_AD_DISABLED, false).apply()
            Thread.sleep(5000)

            viewModel.loadYandexAd(indexId = 0, isOnExit = false, listener = object: AdViewModel.YandexAdListener {
                override fun onYandexAdLoaded(ad: InterstitialAd, isOnExit: Boolean) {
                    Assert.assertTrue(true)
                    ad.apply {
                        show()
                        setInterstitialAdEventListener(object : InterstitialAdEventListener {
                            override fun onAdLoaded() {}

                            override fun onAdFailedToLoad(p0: AdRequestError) {
                                println("******************* AdRequestError: ${p0.description} *****************************")
                                Assert.assertTrue(false)
                                isRunning = false
                            }

                            override fun onAdShown() {
                                println("******************* Ad is shown *****************************")
                                Assert.assertTrue(true)
                                isRunning = false
                            }

                            override fun onAdDismissed() {}

                            override fun onAdClicked() {}

                            override fun onLeftApplication() {}

                            override fun onReturnedToApplication() {}

                            override fun onImpression(p0: ImpressionData?) {}

                        })
                    }
                }
                override fun onYandexAdFailed(error: AdRequestError) {
                    println("******************* AdRequestError: ${error.description} **********************")
                    Assert.assertTrue(false)
                    isRunning = false
                }
            })
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