package com.renatsayf.stockinsider.di

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.renatsayf.stockinsider.service.WorkTask
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors

//TODO Hilt step 4
@HiltAndroidApp
class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        val workRequest = WorkTask.createPeriodicTask(this, "${this::class.java.name}.WORK_TASK")
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "${this.hashCode()}.WORK",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
    }
}
