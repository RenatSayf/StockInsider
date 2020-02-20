package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.utils.Utils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UtilsModule
{
    @Provides
    @Singleton
    fun provideUtils() : Utils
    {
        return Utils()
    }
}