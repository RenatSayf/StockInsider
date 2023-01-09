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
import com.renatsayf.stockinsider.utils.currentCountryCode
import com.renatsayf.stockinsider.utils.getGoogleAdId
import com.renatsayf.stockinsider.utils.getYandexAdId
import com.yandex.mobile.ads.common.InitializationListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AdViewModel @Inject constructor(private val app: Application) : AndroidViewModel(app) {

    private var status: Boolean = false
    private var googleAd: InterstitialAd? = null
    private var yandexAd: com.yandex.mobile.ads.interstitial.InterstitialAd? = null
    private val googleAdRequest = AdRequest.Builder().build()
    private val yandexAdRequest = com.yandex.mobile.ads.common.AdRequest.Builder().build()

    init {

        if (app.currentCountryCode == CountryCode.RU.name) {
            com.yandex.mobile.ads.common.MobileAds.initialize(app, object : InitializationListener {
                override fun onInitializationCompleted() {
                    if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
                    yandexAd = com.yandex.mobile.ads.interstitial.InterstitialAd(app).apply {
                        setAdUnitId(app.getYandexAdId(index = 0))
                        loadAd(yandexAdRequest)
                    }
                }
            })
        }
        else {
            MobileAds.initialize(app, object : OnInitializationCompleteListener {

                override fun onInitializationComplete(status: InitializationStatus) {

                    this@AdViewModel.status = true
                    if (BuildConfig.DEBUG) println("*************** Google mobile ads has been initialized *****************")
                    val adUnitId = app.getGoogleAdId(index = 0)


                }
            })

        }
    }

    fun loadAd(id: String) {

        if (status) {
            InterstitialAd.load(app, id, googleAdRequest, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    googleAd = p0

                }
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    if (BuildConfig.DEBUG) {
                        Exception(p0.message).printStackTrace()
                    }
                    com.yandex.mobile.ads.common.MobileAds.initialize(app, object : InitializationListener {
                        override fun onInitializationCompleted() {
                            if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
                            yandexAd = com.yandex.mobile.ads.interstitial.InterstitialAd(app).apply {
                                setAdUnitId(id)
                                loadAd(yandexAdRequest)
                            }
                        }
                    })
                }
            })
        }
        else {
            com.yandex.mobile.ads.common.MobileAds.initialize(app, object : InitializationListener {
                override fun onInitializationCompleted() {
                    if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
                    yandexAd = com.yandex.mobile.ads.interstitial.InterstitialAd(app).apply {
                        setAdUnitId(id)
                        loadAd(yandexAdRequest)
                    }
                }
            })
        }
    }





}


