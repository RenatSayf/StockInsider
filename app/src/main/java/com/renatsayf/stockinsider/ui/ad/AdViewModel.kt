@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.ui.ad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.models.CountryCode
import com.renatsayf.stockinsider.utils.appPref
import com.renatsayf.stockinsider.utils.currentCountryCode
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AdViewModel @Inject constructor(private val app: Application) : AndroidViewModel(app) {

    companion object {
        const val KEY_IS_AD_DISABLED = "KEY_IS_AD_DISABLED"
    }

    private var status: Boolean = false
    private val isDisabled = app.appPref.getBoolean(KEY_IS_AD_DISABLED, false)
    private val googleAdRequest = AdRequest.Builder().build()
    private val yandexAdRequest = com.yandex.mobile.ads.common.AdRequest.Builder().build()

    private val googleAdIds = listOf(
        "ca-app-pub-1615119435047194/7629529654",
        "ca-app-pub-1615119435047194/6049672910",
        "ca-app-pub-1615119435047194/9457497746"
    )
    private val googleTestAdId = "ca-app-pub-3940256099942544/1033173712"

    private val yandexAdIds = listOf(
        "R-M-711877-1",
        "R-M-711877-2",
        "R-M-711877-3"
    )
    private val yandexTestAdId = "demo-interstitial-yandex"

    init {

        if (app.currentCountryCode == CountryCode.RU.name) {
            com.yandex.mobile.ads.common.MobileAds.initialize(app, object : InitializationListener {
                override fun onInitializationCompleted() {
                    if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
                }
            })
        }
        else {
            MobileAds.initialize(app, object : OnInitializationCompleteListener {

                override fun onInitializationComplete(status: InitializationStatus) {
                    this@AdViewModel.status = true
                    if (BuildConfig.DEBUG) println("*************** Google mobile ads has been initialized *****************")
                }
            })
        }
    }

    fun loadAd(indexId: Int = 0, isOnExit: Boolean = false, listener: Listener) {

        if (!isDisabled) {
            if (status) {
                val googleAdId = getGoogleAdId(indexId)
                InterstitialAd.load(app, googleAdId, googleAdRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(p0: InterstitialAd) {
                        listener.onGoogleAdLoaded(p0, isOnExit)
                    }
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        if (BuildConfig.DEBUG) {
                            Exception(p0.message).printStackTrace()
                        }
                        val yandexAdId = getYandexAdId(indexId)
                        com.yandex.mobile.ads.interstitial.InterstitialAd(app).apply {
                            setAdUnitId(yandexAdId)
                            loadAd(yandexAdRequest)
                            setInterstitialAdEventListener(object : InterstitialAdEventListener {
                                override fun onAdLoaded() {
                                    listener.onYandexAdLoaded(this@apply, isOnExit)
                                }

                                override fun onAdFailedToLoad(p0: AdRequestError) {
                                    listener.onYandexAdFailed(p0)
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
                })
            }
            else {
                val yandexAdId = getYandexAdId(indexId)
                com.yandex.mobile.ads.interstitial.InterstitialAd(app).apply {
                    setAdUnitId(yandexAdId)
                    loadAd(yandexAdRequest)
                    setInterstitialAdEventListener(object : InterstitialAdEventListener {
                        override fun onAdLoaded() {
                            listener.onYandexAdLoaded(this@apply, isOnExit)
                        }

                        override fun onAdFailedToLoad(p0: AdRequestError) {
                            listener.onYandexAdFailed(p0)
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
        } else {
            listener.onAdDisabled()
        }
    }

    private fun getGoogleAdId(index: Int = 0): String {
        return if (BuildConfig.DEBUG) {
            googleTestAdId
        } else {
            try {
                googleAdIds[index]
            } catch (e: IndexOutOfBoundsException) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                ""
            }
        }
    }

    private fun getYandexAdId(index: Int = 0): String {
        return if (BuildConfig.DEBUG) {
            yandexTestAdId
        } else {
            try {
                yandexAdIds[index]
            } catch (e: IndexOutOfBoundsException) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                ""
            }
        }
    }

    interface Listener {
        fun onGoogleAdLoaded(ad: InterstitialAd, isOnExit: Boolean)
        fun onYandexAdLoaded(ad: com.yandex.mobile.ads.interstitial.InterstitialAd, isOnExit: Boolean)
        fun onGoogleAdFailed(error: LoadAdError)
        fun onYandexAdFailed(error: AdRequestError)
        fun onAdDisabled()
    }



}


