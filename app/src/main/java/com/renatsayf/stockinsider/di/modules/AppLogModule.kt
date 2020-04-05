package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.utils.AppLog
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class AppLogModule @Inject constructor(private val context: Context)
{
    @Provides
    @Singleton
    fun provideAppLog() : AppLog
    {
        return AppLog(context)
    }
}