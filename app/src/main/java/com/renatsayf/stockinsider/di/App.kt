@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.di

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        val apiKey = getString(R.string.ya_metrica_api_key)
        val config = YandexMetricaConfig.newConfigBuilder(apiKey).build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)

        MobileAds.initialize(this, object : InitializationListener {
            override fun onInitializationCompleted() {
                if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
            }
        })
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
    }
}
