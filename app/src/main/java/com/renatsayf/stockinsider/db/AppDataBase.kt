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
            return Room.databaseBuilder(context, AppDataBase::class.java, DATABASE)
                .addMigrations(object : Migration(DB_VERSION - 1, DB_VERSION) {
                        override fun migrate(database: SupportSQLiteDatabase)
                        {
                            if (database.needUpgrade(DB_VERSION))
                            {
                                //migrationFrom10To11(database)
                                migrationUpdateDbFile(context, database)
                            }
                        }
                    }
                )
                .createFromAsset("database/$DATABASE")//.fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }

        private fun migrationFrom10To11(database: SupportSQLiteDatabase)
        {
            val queryAddColumn = """ALTER TABLE user_search_sets ADD 'is_tracked' INTEGER NOT NULL DEFAULT 0"""
            database.execSQL(queryAddColumn)

            val queryAddEntry = """INSERT INTO user_search_sets (set_name, 
company_name, 
ticker, 
filing_period, 
trade_period, 
is_purchase, 
is_sale, 
trade_min, 
trade_max, 
is_officer, 
is_director, 
is_ten_percent, 
group_by, 
sort_by,
is_tracked) VALUES (
"Top 10 NASDAQ stocks",
 "",
 "AAPL MSFT AMZN TSLA NVDA GOOG FB NFLX INTC CSCO",
 1, 3, 0, 1, "", "", 1, 1, 1, 0, 3, 1)"""

            database.execSQL(queryAddEntry)
            return
        }

        private fun migrationUpdateDbFile(context: Context, database: SupportSQLiteDatabase)
        {
            val path = database.path
            val dbFile = File(path)
            if (dbFile.exists())
            {
                dbFile.delete()
                //получаем локальную бд из assets как поток
                val inStream = context.assets.open("database/$DATABASE")
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