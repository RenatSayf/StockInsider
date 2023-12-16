@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.ad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.ui.settings.KEY_IS_AD_DISABLED
import com.renatsayf.stockinsider.utils.appPref
import com.renatsayf.stockinsider.utils.printIfDebug
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class YandexAdsViewModel @Inject constructor(
    private val app: Application
): AndroidViewModel(app) {

    private val isDisabled = app.appPref.getBoolean(KEY_IS_AD_DISABLED, false)

    fun yandexAdsInitialize() {
        MobileAds.initialize(app.applicationContext, object : InitializationListener {
            override fun onInitializationCompleted() {
                "*************** Yandex mobile ads has been initialized *****************".printIfDebug()
            }
        })
    }

    fun loadInterstitialAd(adId: AdsId = AdsId.INTERSTITIAL_1, isOnExit: Boolean = false, listener: InterstitialAdListener) {
        if (!isDisabled) {

            InterstitialAdLoader(app.applicationContext).apply {
                val id = if (BuildConfig.DEBUG) {
                    AdsId.TEST_INTERSTITIAL_AD_ID.value
                } else {
                    adId.value
                }
                val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
                setAdLoadListener(object : InterstitialAdLoadListener {
                    override fun onAdLoaded(p0: InterstitialAd) {
                        listener.onInterstitialAdLoaded(p0, isOnExit)
                    }

                    override fun onAdFailedToLoad(p0: AdRequestError) {
                        listener.onAdFailed(p0)
                        "*************** loadInterstitialAd() ${p0.description} ***************".printIfDebug()
                    }
                })
                loadAd(adRequestConfiguration)
            }
        }
        else {
            listener.onAdFailed(AdRequestError(-1, "Ads are disabled"))
        }
    }

    fun loadRewardedAd(adId: AdsId, isOnExit: Boolean = false, listener: RewardedAdListener) {
        if (!isDisabled) {
            RewardedAdLoader(app.applicationContext).apply {
                val id = if (BuildConfig.DEBUG) {
                    AdsId.TEST_REWARDED_AD_ID.value
                } else {
                    adId.value
                }
                val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
                setAdLoadListener(object : RewardedAdLoadListener {
                    override fun onAdLoaded(p0: RewardedAd) {
                        listener.onRewardedAdLoaded(p0, isOnExit)
                    }

                    override fun onAdFailedToLoad(p0: AdRequestError) {
                        listener.onAdFailed(p0)
                        "*************** loadRewardedAd() ${p0.description} ***************".printIfDebug()
                    }
                })
                loadAd(adRequestConfiguration)
            }
        }
        else {
            listener.onAdFailed(AdRequestError(-1, "Ads are disabled"))
        }
    }

    interface InterstitialAdListener {
        fun onInterstitialAdLoaded(ad: InterstitialAd, isOnExit: Boolean)
        fun onAdFailed(error: AdRequestError)
    }

    interface RewardedAdListener {
        fun onRewardedAdLoaded(ad: RewardedAd, isOnExit: Boolean)
        fun onAdFailed(error: AdRequestError)
    }

}