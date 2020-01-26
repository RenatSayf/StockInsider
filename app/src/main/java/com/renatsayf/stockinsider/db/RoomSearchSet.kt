package com.renatsayf.stockinsider.db

import androidx.room.*

@Fts4
@Entity(tableName = "search_set", indices = [Index("creation_date")])
data class RoomSearchSet(
        @PrimaryKey
        @ColumnInfo(name = "creation_date")
        val creationDate : String,

        @ColumnInfo(name = "company_name")
        val companyName : String,

        @ColumnInfo(name = "ticker")
        val ticker : String,

        @ColumnInfo(name = "filing_period")
        val filingPeriod : Int,

        @ColumnInfo(name = "trade_period")
        val tradePeriod : Int,

        @ColumnInfo(name = "is_purchase")
        val isPurchase : Boolean,

        @ColumnInfo(name = "is_sale")
        val isSale : Boolean,

        @ColumnInfo(name = "trade_min")
        val tradedMin : Int,

        @ColumnInfo(name = "trade_max")
        val tradedMax : Int,

        @ColumnInfo(name = "is_officer")
        val isOfficer : Boolean,

        @ColumnInfo(name = "is_director")
        val isDirector : Boolean,

        @ColumnInfo(name = "is_ten_percent")
        val isTenPercent : Boolean,

        @ColumnInfo(name = "group_by")
        val groupBy : String,

        @ColumnInfo(name = "sort_by")
        val sortBy : String
                        )
{

}