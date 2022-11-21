package com.renatsayf.stockinsider.di

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.renatsayf.stockinsider.service.WorkTask
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
    }
}
