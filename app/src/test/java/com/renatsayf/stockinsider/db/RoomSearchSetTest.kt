package com.renatsayf.stockinsider.db

import org.junit.Assert
import org.junit.Test



class RoomSearchSetTest {

    @Test
    fun generateQueryName_empty_ticker() {

        val searchSet = RoomSearchSet(
            queryName = "",
            companyName = "",
            ticker = "",
            filingPeriod = 3,
            tradePeriod = 3,
            isPurchase = true,
            isSale = false,
            tradedMin = "",
            tradedMax = "",
            isOfficer = true,
            isDirector = true,
            isTenPercent = true,
            groupBy = 1,
            sortBy = 3
        )
        val actualResult = searchSet.generateQueryName()
        val expectedResult = "All/Pur/Officer/Dir/10%Owner"
        Assert.assertEquals(expectedResult, actualResult)
    }

    @Test
    fun generateQueryName_not_empty_ticker() {
        val searchSet = RoomSearchSet(
            queryName = "",
            companyName = "",
            ticker = "BAC MSFT AA AAPL TSLA NVDA GOOG FB NFLX",
            filingPeriod = 3,
            tradePeriod = 3,
            isPurchase = true,
            isSale = false,
            tradedMin = "",
            tradedMax = "",
            isOfficer = true,
            isDirector = true,
            isTenPercent = true,
            groupBy = 1,
            sortBy = 3
        )
        val actualResult = searchSet.generateQueryName()
        val expectedResult = "BAC MSFT AA AAPL TSLA NVDA.../Pur/Officer/Dir/10%Owner"
        Assert.assertEquals(expectedResult, actualResult)
    }
}