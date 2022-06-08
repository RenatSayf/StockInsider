package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.network.NetworkRepository
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DataRepositoryModule
{
    @Provides
    fun provideDataRepository(networkRepository: NetworkRepository, db: AppDao) : DataRepositoryImpl
    {
        return DataRepositoryImpl(networkRepository, db)
    }
}