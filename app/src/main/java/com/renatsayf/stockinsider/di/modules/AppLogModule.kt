package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.utils.AppLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
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