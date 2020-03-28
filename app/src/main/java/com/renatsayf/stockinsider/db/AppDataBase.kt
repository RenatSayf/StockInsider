package com.renatsayf.stockinsider.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject


@Database(entities = [RoomSearchSet::class, Companies::class], version = 1)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun searchSetDao() : AppDao

    companion object
    {
        private const val DATABASE = "stock-insider.db"

        private val MIGRATION : Migration = object : Migration(1, 2)
        {
            override fun migrate(database : SupportSQLiteDatabase)
            {
                val version = database.version
                return
            }
        }

        @Volatile
        private var instance : AppDataBase? = null

        fun getInstance(context : Context) : AppDataBase
        {
            return instance ?: synchronized(this){
                instance ?: buildDataBase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDataBase(context : Context) : AppDataBase
        {
            val dataBase = Room.databaseBuilder(context, AppDataBase::class.java, DATABASE)
                //.addMigrations(MIGRATION)
                .createFromAsset("database/stock-insider.db")//.fallbackToDestructiveMigration()
                .build()
            return dataBase
        }
    }


}