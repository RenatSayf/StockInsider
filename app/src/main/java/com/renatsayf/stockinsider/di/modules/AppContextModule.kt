package com.renatsayf.stockinsider.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppContextModule constructor(private val context : Context)
{
    @Provides
    @Singleton
    fun provideAppContext() : Context
    {
        return context
    }
}