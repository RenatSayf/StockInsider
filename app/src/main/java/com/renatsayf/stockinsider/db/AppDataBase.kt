package com.renatsayf.stockinsider.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

private const val DB_VERSION = 19

@Database(
    entities = [RoomSearchSet::class, Company::class],
    version = DB_VERSION,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 18, to = 19)]
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        private const val DATABASE = "stock-insider.db"

        @Volatile
        private var instance: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return instance ?: synchronized(this) {
                buildDataBase(context).also {
                    instance = it
                }
            }.apply {
                val version = this.openHelper.writableDatabase.version
                if (version == 19) {
                    updateFilingPeriods(this)
                }
            }
        }

        private fun buildDataBase(context: Context): AppDataBase {

//            val migration18to19 = object : Migration(18, 19) {
//                override fun migrate(db: SupportSQLiteDatabase) {
//                    val version = db.version
//                    if (version < 19) {
//                        context.deleteDatabase(DATABASE)
//                    }
//                }
//            }
//
//            val migration19to20 = object : Migration(19, 20) {
//                override fun migrate(db: SupportSQLiteDatabase) {
//                    db.execSQL(updateFilingPeriod_3)
//                }
//            }

            return Room.databaseBuilder(context, AppDataBase::class.java, DATABASE).apply {
                createFromAsset("database/$DATABASE")
                allowMainThreadQueries()
            }.build()
        }
    }

}

private fun updateFilingPeriods(appDataBase: AppDataBase) {
    val database = appDataBase.openHelper.writableDatabase
    database.execSQL(updateFilingPeriod_3)
    database.execSQL(updateFilingPeriod_7)
    database.execSQL(updateFilingPeriod_14)
}

private const val updateFilingPeriod_3 = """UPDATE search_set SET 
filing_period = 3, trade_period = 7 
WHERE set_name = 'pur_more1_for_3' OR set_name = 'pur_more5_for_3' OR set_name = 'sale_more1_for_3' OR set_name = 'sale_more5_for_3'"""

private const val updateFilingPeriod_7 = """UPDATE search_set SET 
filing_period = 7, trade_period = 11 
WHERE set_name = 'purchases_more_1' OR set_name = 'purchases_more_5' OR set_name = 'sales_more_1' OR set_name = 'sales_more_5'"""

private const val updateFilingPeriod_14 = """UPDATE search_set SET 
filing_period = 14, trade_period = 18 
WHERE set_name = 'pur_more1_for_14' OR set_name = 'pur_more5_for_14' OR set_name = 'sale_more1_for_14' OR set_name = 'sale_more5_for_14'"""

private const val QUERY = """INSERT INTO search_set (
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

private const val QUERY17 =
    """UPDATE search_set SET set_name = 'NASDAQ top 10 stocks' WHERE set_name == 'Top 10 NASDAQ stocks'"""