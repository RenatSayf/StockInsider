package com.renatsayf.stockinsider.utils

import android.app.Activity
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.BuildConfig
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener


val Activity.currentCountryCode: String
    get() {
        val locale = this.resources.configuration.locales[0]
        return locale.country
    }

val Fragment.currentCountryCode: String
    get() {
        return requireActivity().currentCountryCode
    }

fun Activity.showInterstitialAd(
    yandexAd: com.yandex.mobile.ads.interstitial.InterstitialAd?,
    callback: () -> Unit
    ) {
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

fun Fragment.showInterstitialAd(
    yandexAd: com.yandex.mobile.ads.interstitial.InterstitialAd?,
    callback: () -> Unit
) {
    requireActivity().showInterstitialAd(yandexAd, callback)
}

fun Activity.showRewardedAd(
    ad: RewardedAd?,
    onShown: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    ad?.setRewardedAdEventListener(object : RewardedAdEventListener {
        override fun onAdLoaded() {}

        override fun onAdFailedToLoad(p0: AdRequestError) {}

        override fun onAdShown() {
            onShown.invoke()
        }

        override fun onAdDismissed() {
            onDismissed.invoke()
        }

        override fun onAdClicked() {}

        override fun onLeftApplication() {}

        override fun onReturnedToApplication() {}

        override fun onImpression(p0: ImpressionData?) {}

        override fun onRewarded(p0: Reward) {}
    })
    ad?.show()
}

fun Fragment.showRewardedAd(
    ad: RewardedAd?,
    onShown: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    requireActivity().showRewardedAd(ad, onShown, onDismissed)
}

























