package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object DataRepositoryModule
{
    @Provides
    @Singleton
    fun provideDataRepository(searchRequest: SearchRequest) : DataRepositoryImpl
    {
        return DataRepositoryImpl(searchRequest)
    }
}