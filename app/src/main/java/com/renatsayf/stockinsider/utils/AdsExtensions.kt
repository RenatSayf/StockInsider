package com.renatsayf.stockinsider.utils

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.renatsayf.stockinsider.BuildConfig
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import java.util.*


fun Application.getCurrentLocale(): Locale {

    return this.resources.configuration.locales[0]
}

val Application.currentCountryCode: String
    get() {
        return getCurrentLocale().country
    }

val Activity.currentCountryCode: String
    get() {
        val locale = this.resources.configuration.locales[0]
        return locale.country
    }

val Fragment.currentCountryCode: String
    get() {
        return requireActivity().currentCountryCode
    }

fun Activity.showAd(
    googleAd: InterstitialAd?,
    yandexAd: com.yandex.mobile.ads.interstitial.InterstitialAd?,
    callback: () -> Unit
    ) {
    googleAd?.let { ad ->
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                callback.invoke()
            }
        }
        ad.show(this)
    }?: run {
        yandexAd?.let { ad ->
            ad.setInterstitialAdEventListener(object : InterstitialAdEventListener {
                override fun onAdLoaded() {}
                override fun onAdFailedToLoad(error: AdRequestError) {
                    if (BuildConfig.DEBUG) {
                        val exception = Exception("**************** ${error.description} *******************")
                        exception.printStackTrace()
                    }
                    callback.invoke()
                }
                override fun onAdShown() {}
                override fun onAdDismissed() {
                    callback.invoke()
                }
                override fun onAdClicked() {}
                override fun onLeftApplication() {}
                override fun onReturnedToApplication() {}
                override fun onImpression(p0: ImpressionData?) {}
            })
            ad.show()
        }?: run {
            callback.invoke()
        }
    }
}

fun Fragment.showAd(
    googleAd: InterstitialAd?,
    yandexAd: com.yandex.mobile.ads.interstitial.InterstitialAd?,
    callback: () -> Unit
) {
    requireActivity().showAd(googleAd, yandexAd, callback)
}

























