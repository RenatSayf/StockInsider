package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.AppDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

//TODO Hilt step 5
@InstallIn(SingletonComponent::class)
@Module
object RoomDataBaseModule
{
    @Provides
    fun provideRoomDataBase(@ApplicationContext context: Context) : AppDao
    {
        return AppDataBase.getInstance(context).appDao()
    }
}