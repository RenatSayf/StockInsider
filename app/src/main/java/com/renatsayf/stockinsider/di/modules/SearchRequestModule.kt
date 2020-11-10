package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.network.SearchRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object SearchRequestModule
{
    @Provides
    @Singleton
    fun provideSearchRequest(): SearchRequest
    {
        return SearchRequest()
    }
}