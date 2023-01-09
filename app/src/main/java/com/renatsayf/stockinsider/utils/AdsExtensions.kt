package com.renatsayf.stockinsider.utils

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import java.util.Locale


fun Activity.getCurrentLocale(): Locale {
    return this.resources.configuration.locales[0]
}

fun Fragment.getCurrentLocale(): Locale {
    return requireActivity().getCurrentLocale()
}

val Activity.currentCountryCode: String
    get() {
        return getCurrentLocale().country
    }

val Fragment.currentCountryCode: String
    get() {
        return requireActivity().currentCountryCode
    }

fun Application.getCurrentLocale(): Locale {
    return this.resources.configuration.locales[0]
}

val Application.currentCountryCode: String
    get() {
        return getCurrentLocale().country
    }

fun Application.getGoogleAdId(index: Int = 0): String {
    return if (BuildConfig.DEBUG) {
        this.getString(R.string.test_interstitial_ads_id)
    } else {
        val array = this.resources.getStringArray(R.array.interstitial_ads)
        try {
            array[index]
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            ""
        }
    }
}

fun Application.getYandexAdId(index: Int = 0): String {
    return if (BuildConfig.DEBUG) {
        this.getString(R.string.yandex_demo_ad_id)
    } else {
        val array = this.resources.getStringArray(R.array.yandex_ads)
        try {
            array[index]
        } catch (e: IndexOutOfBoundsException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            ""
        }
    }
}


























