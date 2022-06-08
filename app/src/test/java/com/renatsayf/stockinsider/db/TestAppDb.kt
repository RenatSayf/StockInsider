package com.renatsayf.stockinsider.db

import android.content.Context
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper

open class TestAppDb: AppDataBase() {

    companion object {

        @Volatile
        private var instance : TestAppDb? = null

        fun getInstance(context : Context) : TestAppDb
        {
            return instance ?: synchronized(this){
                instance ?: buildDataBase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDataBase(context : Context) : TestAppDb
        {
            return Room.inMemoryDatabaseBuilder(context, TestAppDb::class.java)
                .allowMainThreadQueries()
                .build()
        }
    }

    override fun searchSetDao(): AppDao {
        TODO("XXXXX")
    }

    override fun createOpenHelper(config: DatabaseConfiguration?): SupportSQLiteOpenHelper {
        return super.getOpenHelper()
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        return super.getInvalidationTracker()
    }

    override fun clearAllTables() {

    }


}