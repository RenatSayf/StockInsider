package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object NotificationModule
{
    @Provides
    fun provideNotification(): ServiceNotification
    {
        return ServiceNotification()
    }
}