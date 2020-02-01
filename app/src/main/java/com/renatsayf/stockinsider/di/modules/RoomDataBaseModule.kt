package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.db.RoomDBProvider
import com.renatsayf.stockinsider.db.SearchSetDao
import dagger.Module
import dagger.Provides

@Module
class RoomDataBaseModule
{
    @Provides
    fun provideRoomDataBase(context : Context) : SearchSetDao
    {
        return RoomDBProvider().getDao(context)
    }
}