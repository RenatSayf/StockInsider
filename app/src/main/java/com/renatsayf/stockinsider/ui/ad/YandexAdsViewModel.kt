@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.ad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.renatsayf.stockinsider.BuildConfig
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

    fun yandexAdsInitialize() {
        MobileAds.initialize(app.applicationContext, object : InitializationListener {
            override fun onInitializationCompleted() {
                "*************** Yandex mobile ads has been initialized *****************".printIfDebug()
            }
        })
    }

    fun loadInterstitialAd(adId: AdsId = AdsId.INTERSTITIAL_1) {
        InterstitialAdLoader(app.applicationContext).apply {
            val id = if (BuildConfig.DEBUG) {
                AdsId.TEST_INTERSTITIAL_AD_ID.value
            } else {
                adId.value
            }
            val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    _interstitialAd.value = Result.success(ad)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    _interstitialAd.value = Result.failure(Throwable(error.description))
                    "*************** loadInterstitialAd() ${error.description} ***************".printIfDebug()
                }
            })
            loadAd(adRequestConfiguration)
        }
    }

    private var _interstitialAd = MutableLiveData<Result<InterstitialAd>>()
    val interstitialAd: LiveData<Result<InterstitialAd>> = _interstitialAd

    fun loadRewardedAd(adId: AdsId = AdsId.REWARDED_1) {
        RewardedAdLoader(app.applicationContext).apply {
            val id = if (BuildConfig.DEBUG) {
                AdsId.TEST_REWARDED_AD_ID.value
            } else {
                adId.value
            }
            val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(ad: RewardedAd) {
                    _rewardedAd.value = Result.success(ad)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    _rewardedAd.value = Result.failure(Throwable(error.description))
                    "*************** loadRewardedAd() ${error.description} ***************".printIfDebug()
                }
            })
            loadAd(adRequestConfiguration)
        }
    }

    private var _rewardedAd = MutableLiveData<Result<RewardedAd>>()
    val rewardedAd: LiveData<Result<RewardedAd>> = _rewardedAd

}