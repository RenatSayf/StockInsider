package com.renatsayf.stockinsider.di.modules

import android.app.Activity
import com.renatsayf.stockinsider.db.RoomDBProvider
import com.renatsayf.stockinsider.db.SearchSetDao
import dagger.Module
import dagger.Provides

@Module
class RoomDataBaseModule
{
    @Provides
    fun provideRoomDataBase(activity : Activity) : SearchSetDao
    {
        return RoomDBProvider().getDataBase(activity)
    }
}