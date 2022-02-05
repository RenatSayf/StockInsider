package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.schedule.IScheduler
import com.renatsayf.stockinsider.schedule.Scheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
object SchedulerModule {

    @Provides
    fun provideScheduler(@ApplicationContext context: Context): IScheduler {
        return Scheduler(context)
    }
}