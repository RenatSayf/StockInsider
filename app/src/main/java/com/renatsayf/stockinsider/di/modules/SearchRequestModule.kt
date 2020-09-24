package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.network.SearchRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object SearchRequestModule
{
    @Provides
    @Singleton
    fun provideSearchRequest(@ApplicationContext context: Context): SearchRequest
    {
        return SearchRequest()
    }
}