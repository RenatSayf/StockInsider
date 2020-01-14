package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.models.SearchRequest
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SearchRequestModule
{
    @Provides
    @Singleton
    fun provideSearchRequest(): SearchRequest
    {
        return SearchRequest()
    }
}