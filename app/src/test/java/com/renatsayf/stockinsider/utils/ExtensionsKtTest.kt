package com.renatsayf.stockinsider.utils

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.models.Deal
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random


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

    @Test
    fun getMapValuesSize() {
        val deals = List(3, init = {
            Deal("date $it")
        })
        val map = mapOf(
            "1" to deals,
            "2" to deals
        )
        val actualSize = map.getValuesSize()
        Assert.assertEquals(6, actualSize)
    }

    @Test
    fun getMapValuesSize_empty_map() {
        val emptyMap = emptyMap<String, List<Deal>>()
        val actualSize = emptyMap.getValuesSize()
        Assert.assertEquals(0, actualSize)
    }

    @Test
    fun minutesToMillis() {
        val randomLong = Random.nextLong(0, 12)
        val actual = TimeUnit.MINUTES.toMillis(12)
        Assert.assertEquals(720000, actual)
    }

}