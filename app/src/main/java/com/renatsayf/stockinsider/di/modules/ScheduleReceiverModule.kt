package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.network.Scheduler
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ScheduleReceiverModule
{
    @Provides
    @Singleton
    fun provideScheduleReceiver() : Scheduler
    {
        return Scheduler()
    }
}