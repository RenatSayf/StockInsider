package com.renatsayf.stockinsider.utils

import com.renatsayf.stockinsider.db.Companies
import org.junit.Assert
import org.junit.Test


class ExtensionsKtTest {

    @Test
    fun sortByAnotherList_Test() {
        val targetList = listOf<Companies>(
            Companies("AA", "Alcoa Corp"),
            Companies("AAPL", "Apple Inc."),
            Companies("BAC", "Bank of America Corp"),
            Companies("MSFT", "Microsoft Corp")
        )

        val anotherList = listOf(
            "BAC",
            "MSFT",
            "AA",
            "AAPL"
        )
        val actualList = sortByAnotherList(targetList, anotherList)
        val actualResult = actualList.map {
            it.ticker
        }
        Assert.assertEquals(anotherList, actualResult)
    }
}