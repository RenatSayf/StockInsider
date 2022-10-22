package com.renatsayf.stockinsider.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

private const val DB_VERSION = 18

@Database(entities = [RoomSearchSet::class, Company::class],
    version = DB_VERSION,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = DB_VERSION - 1, to = DB_VERSION)])
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
                }.apply {
                    val version = this.mDatabase?.version ?: 1
                    if (DB_VERSION > version) {
                        this.mDatabase?.execSQL(query17)
                    }
                }
            }
        }

        private fun buildDataBase(context : Context) : AppDataBase
        {
            return Room.databaseBuilder(context, AppDataBase::class.java, DATABASE)
                .createFromAsset("database/$DATABASE")
                .allowMainThreadQueries()
                .build()
        }
    }

}


private const val query = """INSERT INTO search_set (
set_name, 
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
target,
is_tracked,
is_default) VALUES (
"DOW JONES top 30 stocks",
 "",
 "AXP AMGN AAPL BA CAT CSCO CVX GS HD HON IBM INTC JNJ KO JPM MCD MMM MRK MSFT NKE PG TRV UNH CRM VZ V WBA WMT DIS DOW",
 1, 3, 1, 0, "", "", 1, 1, 1, 0, 3, "tracking", 1, 1)"""

private const val query17 = """UPDATE search_set SET set_name = 'NASDAQ top 10 stocks' WHERE set_name == 'Top 10 NASDAQ stocks'"""