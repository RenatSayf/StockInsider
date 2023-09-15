@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.ad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.utils.appPref
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.impl.kv
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AdViewModel @Inject constructor(private val app: Application): AndroidViewModel(app) {

    companion object {
        const val KEY_IS_AD_DISABLED = "KEY_IS_AD_DISABLED"
    }

    private var yandexInitStatus: Boolean = false
    private val isDisabled = app.appPref.getBoolean(KEY_IS_AD_DISABLED, false)
    private val yandexAdRequest = com.yandex.mobile.ads.common.AdRequest.Builder().build()

    fun init(){
        MobileAds.initialize(app, object : InitializationListener {
            override fun onInitializationCompleted() {
                this@AdViewModel.yandexInitStatus = true
                if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
            }
        })
    }

    fun loadInterstitialAd(adId: AdsId = AdsId.INTERSTITIAL_1, isOnExit: Boolean = false, listener: YandexAdListener) {
        if (!isDisabled) {

            InterstitialAd(app).apply {
                if (BuildConfig.DEBUG) {
                    setAdUnitId(AdsId.TEST_INTERSTITIAL_AD_ID.value)
                }
                else {
                    setAdUnitId(adId.value)
                }
                loadAd(yandexAdRequest)
                setInterstitialAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdLoaded() {
                        listener.onYandexAdLoaded(this@apply, isOnExit)
                    }

                    override fun onAdFailedToLoad(error: AdRequestError) {
                        listener.onYandexAdFailed(error)
                        if (BuildConfig.DEBUG) println("*************** loadInterstitialAd() ${error.description} ***************")
                    }

                    override fun onAdShown() {}

                    override fun onAdDismissed() {}

                    override fun onAdClicked() {}

                    override fun onLeftApplication() {}

                    override fun onReturnedToApplication() {}

                    override fun onImpression(p0: ImpressionData?) {}
                })
            }
        }
        else {
            listener.onYandexAdFailed(AdRequestError(-1, "Ads are disabled"))
        }
    }

    fun loadRewardedAd(adId: AdsId, isOnExit: Boolean = false, listener: YandexAdListener) {
        if (!isDisabled) {
            com.yandex.mobile.ads.rewarded.RewardedAd(app).apply {
                if (BuildConfig.DEBUG) {
                    setAdUnitId(AdsId.TEST_REWARDED_AD_ID.value)
                }
                else {
                    setAdUnitId(adId.value)
                }
                loadAd(yandexAdRequest)
                setRewardedAdEventListener(object : RewardedAdEventListener {
                    override fun onAdLoaded() {
                        listener.onYandexAdLoaded(this@apply, isOnExit)
                    }

                    override fun onAdFailedToLoad(error: AdRequestError) {
                        listener.onYandexAdFailed(error)
                        if (BuildConfig.DEBUG) println("*************** loadRewardedAd() ${error.description} ***************")
                    }

                    override fun onAdShown() {}

                    override fun onAdDismissed() {}

                    override fun onAdClicked() {}

                    override fun onLeftApplication() {}

                    override fun onReturnedToApplication() {}

                    override fun onImpression(p0: ImpressionData?) {}

                    override fun onRewarded(p0: Reward) {}
                })
            }
        }
        else {
            listener.onYandexAdFailed(AdRequestError(-1, "Ads are disabled"))
        }
    }

    interface YandexAdListener {
        fun onYandexAdLoaded(ad: kv, isOnExit: Boolean)
        fun onYandexAdFailed(error: AdRequestError)
    }

}