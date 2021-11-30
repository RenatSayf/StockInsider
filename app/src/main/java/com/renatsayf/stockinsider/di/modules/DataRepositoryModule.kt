package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.network.SearchRequest
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataRepositoryModule
{
    @Provides
    fun provideDataRepository(searchRequest: SearchRequest, db: AppDao) : DataRepositoryImpl
    {
        return DataRepositoryImpl(searchRequest, db)
    }
}