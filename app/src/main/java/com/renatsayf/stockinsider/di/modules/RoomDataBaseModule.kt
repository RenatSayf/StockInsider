package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.AppDataBase
import com.renatsayf.stockinsider.db.RoomDBProvider
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class RoomDataBaseModule @Inject constructor(private val context: Context)
{
    @Provides
    fun provideContext() : Context
    {
        return context
    }

    @Provides
    fun provideRoomDataBase() : AppDao
    {
        return AppDataBase.getInstance(context).searchSetDao()
    }
}