package com.renatsayf.stockinsider.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.FileOutputStream

private const val DB_VERSION = 10

@Database(entities = [RoomSearchSet::class, Companies::class], version = DB_VERSION, exportSchema = false)
abstract class AppDataBase : RoomDatabase()
{
    abstract fun searchSetDao() : AppDao

    companion object
    {
        private const val DATABASE = "stock-insider.db"
        private var mContext: Context? = null

        private val MIGRATION : Migration = object : Migration(DB_VERSION - 1, DB_VERSION)
        {
            override fun migrate(database : SupportSQLiteDatabase)
            {
                val version = database.version
                if (database.needUpgrade(DB_VERSION))
                {
                    val path = database.path
                    val dbFile = File(path)
                    if (dbFile.exists() && mContext != null)
                    {
                        dbFile.delete()
                        //получаем локальную бд из assets как поток
                        val inStream = mContext!!.assets.open("database/$DATABASE")
                        //открываем пустой файл
                        val outStream = FileOutputStream(path)
                        // побайтово копируем данные в него
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (inStream.read(buffer).also {length = it} > 0)
                        {
                            outStream.write(buffer, 0, length)
                        }
                        outStream.flush()
                        outStream.close()
                        inStream.close()
                    }
                }
            }
        }

        @Volatile
        private var instance : AppDataBase? = null

        fun getInstance(context : Context) : AppDataBase
        {
            mContext = context
            return instance ?: synchronized(this){
                instance ?: buildDataBase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDataBase(context : Context) : AppDataBase
        {
            return Room.databaseBuilder(context, AppDataBase::class.java, DATABASE)
                .addMigrations(MIGRATION)
                .createFromAsset("database/$DATABASE")//.fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
    }


}