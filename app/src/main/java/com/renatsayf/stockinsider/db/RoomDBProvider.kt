package com.renatsayf.stockinsider.db

import android.content.Context
import javax.inject.Inject

class RoomDBProvider @Inject constructor()
{
    fun getDao(context : Context) : SearchSetDao
    {
        return RoomSearchSetDB.getInstance(context).searchSetDao()
    }
}