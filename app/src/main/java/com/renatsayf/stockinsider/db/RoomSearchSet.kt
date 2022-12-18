package com.renatsayf.stockinsider.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.renatsayf.stockinsider.models.SearchSet
import java.io.Serializable

@Entity(
        tableName = "search_set",
        indices = [
                Index(
                        value = [
                                "set_name",
                                "ticker"
                        ], unique = true
                )]
)
data class RoomSearchSet(

        @ColumnInfo(name = "set_name")
        var queryName: String,

        @ColumnInfo(name = "company_name")
        var companyName: String,

        @ColumnInfo(name = "ticker")
        var ticker: String,

        @ColumnInfo(name = "filing_period")
        var filingPeriod: Int,

        @ColumnInfo(name = "trade_period")
        var tradePeriod: Int,

        @ColumnInfo(name = "is_purchase", defaultValue = "1")
        var isPurchase: Boolean = true,

        @ColumnInfo(name = "is_sale", defaultValue = "0")
        var isSale: Boolean = false,

        @ColumnInfo(name = "trade_min")
        var tradedMin: String,

        @ColumnInfo(name = "trade_max")
        var tradedMax: String,

        @ColumnInfo(name = "is_officer")
        var isOfficer: Boolean,

        @ColumnInfo(name = "is_director")
        var isDirector: Boolean,

        @ColumnInfo(name = "is_ten_percent")
        var isTenPercent: Boolean,

        @ColumnInfo(name = "group_by")
        var groupBy: Int,

        @ColumnInfo(name = "sort_by")
        var sortBy: Int,

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id", defaultValue = "0")
        var id: Long = 0
) : Serializable {

        @ColumnInfo(name = "target")
        var target : String? = null

        @ColumnInfo(name = "is_tracked", defaultValue = "0")
        var isTracked : Boolean = false

        @ColumnInfo(name = "is_default", defaultValue = "0")
        var isDefault : Boolean = false

        fun toSearchSet(): SearchSet {
                return SearchSet(queryName).apply {
                        ticker = formatTicker(this@RoomSearchSet.ticker)
                        filingPeriod = getFilingOrTradeValue(this@RoomSearchSet.filingPeriod)
                        tradePeriod = getFilingOrTradeValue(this@RoomSearchSet.tradePeriod)
                        isPurchase = booleanToString(this@RoomSearchSet.isPurchase)
                        isSale = booleanToString(this@RoomSearchSet.isSale)
                        excludeDerivRelated = "1"
                        tradedMin = this@RoomSearchSet.tradedMin
                        tradedMax = this@RoomSearchSet.tradedMax
                        isOfficer = getOfficerValue(this@RoomSearchSet.isOfficer)
                        isDirector = booleanToString(this@RoomSearchSet.isDirector)
                        isTenPercent = booleanToString(this@RoomSearchSet.isTenPercent)
                        groupBy = getGroupingValue(this@RoomSearchSet.groupBy)
                        sortBy = getSortingValue(this@RoomSearchSet.sortBy)
                }
        }
        fun generateQueryName(): String {

                val nameLength = 26
                val tickers = if (this.ticker.length > nameLength) {
                        this.ticker.substring(0, nameLength).plus("...")
                } else this.ticker

                return (tickers.ifEmpty { "All" })
                        //.plus("/period"+roomSearchSet.filingPeriod)
                        .plus(if (isPurchase) "/Pur" else "")
                        .plus(if (isSale) "/Sale" else "")
                        .plus(if (tradedMin.isNotEmpty()) "/min$tradedMin" else "")
                        .plus(if (tradedMax.isNotEmpty()) "/max$tradedMax" else "")
                        .plus(if (isOfficer) "/Officer" else "")
                        .plus(if (isDirector) "/Dir" else "")
                        .plus(if (isTenPercent) "/10%Owner" else "")
        }

        private fun formatTicker(string: String) : String
        {
                val pattern = Regex("\\s")
                return string.trim().replace(pattern, "+")
        }

        private fun getFilingOrTradeValue(position: Int) : String
        {
                val timePeriods = listOf(0, 1, 2, 3, 7, 14, 30, 60, 90, 180, 365, 730, 1461)
                val filingValue : String
                return try
                {
                        filingValue = timePeriods[position].toString()
                        filingValue
                }
                catch (e: Exception)
                {
                        e.printStackTrace()
                        ""
                }
        }

        private fun booleanToString(value: Boolean) : String
        {
                return if (value) "1" else ""
        }

        private fun getOfficerValue(value: Boolean) : String
        {
                return if (value) "1&iscob=1&isceo=1&ispres=1&iscoo=1&iscfo=1&isgc=1&isvp=1" else ""
        }

        private fun getGroupingValue(position: Int): String
        {
                val groupingValues = listOf(0, 2)
                return groupingValues[position].toString()
        }

        private fun getSortingValue(position: Int) : String
        {
                val sortingValues = listOf(0, 1, 2, 8)
                return sortingValues[position].toString()
        }

        fun getFullCopy(): RoomSearchSet {
                val copy = this.copy()
                return copy.apply {
                        target = this@RoomSearchSet.target
                        isTracked = this@RoomSearchSet.isTracked
                        isDefault = this@RoomSearchSet.isDefault
                }
        }

        override fun equals(other: Any?): Boolean {
                return try {
                        val otherSet = other as? RoomSearchSet
                        otherSet?.ticker == this.ticker &&
                                otherSet.companyName == this.companyName &&
                                otherSet.queryName == this.queryName &&
                                otherSet.filingPeriod == this.filingPeriod &&
                                otherSet.tradePeriod == this.tradePeriod &&
                                otherSet.isPurchase == this.isPurchase &&
                                otherSet.isSale == this.isSale &&
                                otherSet.tradedMin == this.tradedMin &&
                                otherSet.tradedMax == this.tradedMax &&
                                otherSet.isOfficer == this.isOfficer &&
                                otherSet.isDirector == this.isDirector &&
                                otherSet.isTenPercent == this.isTenPercent &&
                                otherSet.groupBy == this.groupBy &&
                                otherSet.sortBy == this.sortBy &&
                                otherSet.target == this.target &&
                                otherSet.isTracked == this.isTracked &&
                                otherSet.isDefault == this.isDefault

                } catch (e: ClassCastException) {
                        false
                }
        }

        override fun hashCode(): Int {
                var result = queryName.hashCode()
                result = 31 * result + companyName.hashCode()
                result = 31 * result + ticker.hashCode()
                result = 31 * result + filingPeriod
                result = 31 * result + tradePeriod
                result = 31 * result + isPurchase.hashCode()
                result = 31 * result + isSale.hashCode()
                result = 31 * result + tradedMin.hashCode()
                result = 31 * result + tradedMax.hashCode()
                result = 31 * result + isOfficer.hashCode()
                result = 31 * result + isDirector.hashCode()
                result = 31 * result + isTenPercent.hashCode()
                result = 31 * result + groupBy
                result = 31 * result + sortBy
                result = 31 * result + id.hashCode()
                result = 31 * result + (target?.hashCode() ?: 0)
                result = 31 * result + isTracked.hashCode()
                result = 31 * result + isDefault.hashCode()
                return result
        }
}
