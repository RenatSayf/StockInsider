package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.utils.AppLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppLogModule
{
    @Provides
    @Singleton
    fun provideAppLog(@ApplicationContext context: Context) : AppLog
    {
        return AppLog(context)
    }
}