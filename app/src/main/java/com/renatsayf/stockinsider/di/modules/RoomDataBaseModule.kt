package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.RoomDBProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

//TODO Hilt step 5
@InstallIn(ApplicationComponent::class)
@Module
object RoomDataBaseModule
{
    @Provides
    @Singleton
    fun provideRoomDataBase(@ApplicationContext context: Context) : AppDao
    {
        return AppDataBase.getInstance(context).searchSetDao()
    }
}