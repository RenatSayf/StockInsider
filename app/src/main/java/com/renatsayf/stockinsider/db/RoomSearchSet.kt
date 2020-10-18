package com.renatsayf.stockinsider.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "search_set", indices = [Index("set_name")])
data class RoomSearchSet(

        @PrimaryKey
        @ColumnInfo(name = "set_name")
        var setName : String,

        @ColumnInfo(name = "company_name")
        val companyName : String,

        @ColumnInfo(name = "ticker")
        val ticker : String,

        @ColumnInfo(name = "filing_period")
        val filingPeriod : Int,

        @ColumnInfo(name = "trade_period")
        val tradePeriod : Int,

        @ColumnInfo(name = "is_purchase")
        val isPurchase : Boolean = true,

        @ColumnInfo(name = "is_sale")
        val isSale : Boolean = false,

        @ColumnInfo(name = "trade_min")
        val tradedMin : String,

        @ColumnInfo(name = "trade_max")
        val tradedMax : String,

        @ColumnInfo(name = "is_officer")
        val isOfficer : Boolean,

        @ColumnInfo(name = "is_director")
        val isDirector : Boolean,

        @ColumnInfo(name = "is_ten_percent")
        val isTenPercent : Boolean,

        @ColumnInfo(name = "group_by")
        val groupBy : Int,

        @ColumnInfo(name = "sort_by")
        val sortBy : Int
                        )
