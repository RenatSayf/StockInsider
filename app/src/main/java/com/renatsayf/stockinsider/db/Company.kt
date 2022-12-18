package com.renatsayf.stockinsider.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "companies", indices = [Index(value = ["ticker", "company_name"], unique = true)])
data class Company(

        @PrimaryKey
        @ColumnInfo(name = "ticker")
        val ticker: String,

        @ColumnInfo(name = "company_name")
        val company: String
)
