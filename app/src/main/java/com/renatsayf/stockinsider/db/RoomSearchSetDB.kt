package com.renatsayf.stockinsider.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomSearchSet::class], version = 1, exportSchema = false)
abstract class RoomSearchSetDB : RoomDatabase()
{
    abstract fun searchSetDao() : SearchSetDao

//    companion object
//    {
//        @Volatile
//        private var instance : RoomSearchSetDB? = null
//
//        fun getInstance(context : Context) : RoomSearchSetDB
//        {
//            return instance ?: synchronized(this){
//                instance ?: buildDataBase(context).also {
//                    instance = it
//                }
//            }
//        }
//
//        private fun buildDataBase(context : Context) : RoomSearchSetDB
//        {
//            return Room.databaseBuilder(context, RoomSearchSetDB::class.java, DATABASE).build()
//        }
//    }


}