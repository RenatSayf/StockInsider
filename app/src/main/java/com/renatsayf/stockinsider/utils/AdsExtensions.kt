package com.renatsayf.stockinsider.utils

import android.app.Activity
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.BuildConfig
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
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
    yandexAd: InterstitialAd?,
    callback: () -> Unit
    ) {
    yandexAd?.let { ad ->
        ad.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {}

            override fun onAdFailedToShow(p0: AdError) {
                if (BuildConfig.DEBUG) {
                    val exception = Exception("**************** ${p0.description} *******************")
                    exception.printStackTrace()
                }
                callback.invoke()
            }

            override fun onAdDismissed() {
                callback.invoke()
            }

            override fun onAdClicked() {}

            override fun onAdImpression(p0: ImpressionData?) {}

        })
        ad.show(this)
    }?: run {
        callback.invoke()
    }
}

fun Fragment.showInterstitialAd(
    yandexAd: InterstitialAd?,
    callback: () -> Unit
) {
    requireActivity().showInterstitialAd(yandexAd, callback)
}

fun Activity.showRewardedAd(
    ad: RewardedAd?,
    onShown: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    ad?.setAdEventListener(object : RewardedAdEventListener {
        override fun onAdShown() {
            onShown.invoke()
        }

        override fun onAdFailedToShow(p0: AdError) {
            onShown.invoke()
        }

        override fun onAdDismissed() {
            onDismissed.invoke()
        }

        override fun onAdClicked() {}

        override fun onAdImpression(p0: ImpressionData?) {}

        override fun onRewarded(p0: Reward) {}
    })
    ad?.show(this)
}

fun Fragment.showRewardedAd(
    ad: RewardedAd?,
    onShown: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    requireActivity().showRewardedAd(ad, onShown, onDismissed)
}

























