package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomDBProvider
import dagger.Module
import dagger.Provides

@Module
class RoomDataBaseModule
{
    @Provides
    fun provideRoomDataBase(context : Context) : AppDao
    {
        return RoomDBProvider().getDao(context)
    }
}