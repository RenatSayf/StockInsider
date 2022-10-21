package com.renatsayf.stockinsider.utils

import com.renatsayf.stockinsider.db.Companies
import org.junit.Assert
import org.junit.Test


class ExtensionsKtTest {

    @Test
    fun sortByAnotherList_Test() {
        val targetList = listOf(
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

    @Test
    fun sortByAnotherList_Not_Equals_Size() {
        val targetList = listOf(
            Companies("AA", "Alcoa Corp"),
            Companies("AAPL", "Apple Inc."),
            Companies("BAC", "Bank of America Corp"),
            Companies("MSFT", "Microsoft Corp")
        )
        val anotherList = listOf(
            "BAC",
            "NVDA",
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

    @Test
    fun sortByAnotherList_Empty_anotherList() {
        val targetList = listOf(
            Companies("AA", "Alcoa Corp"),
            Companies("AAPL", "Apple Inc."),
            Companies("BAC", "Bank of America Corp"),
            Companies("MSFT", "Microsoft Corp")
        )
        val anotherList = emptyList<String>()
        val actualList = sortByAnotherList(targetList, anotherList)
        Assert.assertEquals(targetList, actualList)
    }
}