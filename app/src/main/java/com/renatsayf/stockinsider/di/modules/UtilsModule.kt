package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UtilsModule
{
    @Provides
    @Singleton
    fun provideUtils() : Utils
    {
        return Utils()
    }
}