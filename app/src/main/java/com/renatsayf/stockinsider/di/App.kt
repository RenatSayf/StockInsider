@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.di

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application(), Configuration.Provider {

    companion object {
        var userAgent = ""
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
    }
}
