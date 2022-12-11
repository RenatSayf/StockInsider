package com.renatsayf.stockinsider.utils

import com.renatsayf.stockinsider.db.Company
import org.junit.Assert
import org.junit.Test


class ExtensionsKtTest {

    @Test
    fun sortByAnotherList_Test() {
        val targetList = listOf(
            Company("AA", "Alcoa Corp"),
            Company("AAPL", "Apple Inc."),
            Company("BAC", "Bank of America Corp"),
            Company("MSFT", "Microsoft Corp")
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
            Company("AA", "Alcoa Corp"),
            Company("AAPL", "Apple Inc."),
            Company("BAC", "Bank of America Corp"),
            Company("MSFT", "Microsoft Corp")
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
            Company("AA", "Alcoa Corp"),
            Company("AAPL", "Apple Inc."),
            Company("BAC", "Bank of America Corp"),
            Company("MSFT", "Microsoft Corp")
        )
        val anotherList = emptyList<String>()
        val actualList = sortByAnotherList(targetList, anotherList)
        Assert.assertEquals(targetList, actualList)
    }

    @Test
    fun convertDefaultWithoutTime() {

        val dateString = "2022-12-09 21:57:45"
        val actualResult = dateString.convertDefaultWithoutTime()
        Assert.assertEquals("2022-12-09", actualResult)
    }

}