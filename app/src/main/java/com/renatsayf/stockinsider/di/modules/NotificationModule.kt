package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.service.ServiceNotification
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NotificationModule
{
    @Provides
    @Singleton
    fun provideNotification() : ServiceNotification
    {
        return ServiceNotification()
    }
}