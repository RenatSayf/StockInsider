@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.di

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.utils.printIfDebug
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig


@HiltAndroidApp
class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        val apiKey = getString(R.string.ya_metrica_api_key)
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(this, config)
        AppMetrica.enableActivityAutoTracking(this)

        MobileAds.initialize(this, object : InitializationListener {
            override fun onInitializationCompleted() {
                "*************** Yandex mobile ads has been initialized *****************".printIfDebug()
            }
        })
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
}
