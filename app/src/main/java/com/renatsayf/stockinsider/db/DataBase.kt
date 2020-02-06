package com.renatsayf.stockinsider.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [RoomSearchSet::class], version = 2, exportSchema = false)
abstract class DataBase : RoomDatabase()
{
    abstract fun searchSetDao() : SearchSetDao

    companion object
    {
        private const val DATABASE = "stock-insider"

        private val MIGRATION_2_3 : Migration = object : Migration(2, 3)
        {
            override fun migrate(database : SupportSQLiteDatabase)
            {
                database.execSQL("CREATE TABLE IF NOT EXISTS companies(_id INTEGER PRIMARY KEY NOT NULL, ticker TEXT, company_name TEXT)")
            }
        }

        @Volatile
        private var instance : DataBase? = null

        fun getInstance(context : Context) : DataBase
        {
            return instance ?: synchronized(this){
                instance ?: buildDataBase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDataBase(context : Context) : DataBase
        {
            return Room.databaseBuilder(context, DataBase::class.java, DATABASE)
                //.addMigrations(MIGRATION_2_3)
                .build()
        }
    }


}