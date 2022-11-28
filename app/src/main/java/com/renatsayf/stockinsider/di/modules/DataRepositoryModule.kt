package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.network.NetRepository
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
    fun provideDataRepository(netRepository: NetRepository, db: AppDao) : DataRepositoryImpl
    {
        return DataRepositoryImpl(netRepository, db)
    }
}