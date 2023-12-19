package com.renatsayf.stockinsider.ui.ad.admob

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.ui.settings.isAdsDisabled
import com.renatsayf.stockinsider.utils.printIfDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@Suppress("ObjectLiteralToLambda")
@HiltViewModel
class AdMobViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {

    private val isDisabled = app.isAdsDisabled
    private var adRequest = AdRequest.Builder().build()

    fun googleAdsInitialize() {
        com.google.android.gms.ads.MobileAds.initialize(app.applicationContext, object : OnInitializationCompleteListener {
            override fun onInitializationComplete(p0: InitializationStatus) {
                "*************** Google mobile ads has been initialized *****************".printIfDebug()
            }
        })
    }

    fun loadInterstitialAd(adId: AdMobIds) {
        if (!isDisabled) {
            val id = if (BuildConfig.DEBUG) {
                AdMobIds.TEST_INTERSTITIAL
            } else {
                adId
            }
            InterstitialAd.load(app.applicationContext, id.value, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    "*************** loadInterstitialAd() ${error.message} ***************".printIfDebug()
                    _interstitialAd.value = Result.failure(Throwable(error.message))
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    _interstitialAd.value = Result.success(ad)
                }
            })
        }
    }

    private var _interstitialAd = MutableLiveData<Result<InterstitialAd>>()
    val interstitialAd: LiveData<Result<InterstitialAd>> = _interstitialAd
}