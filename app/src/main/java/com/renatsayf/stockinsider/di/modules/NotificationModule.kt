package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.service.ServiceNotification
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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