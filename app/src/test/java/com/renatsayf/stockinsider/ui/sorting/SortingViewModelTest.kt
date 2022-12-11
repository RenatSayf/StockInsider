package com.renatsayf.stockinsider.ui.sorting

import com.renatsayf.stockinsider.models.Deal
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert



class SortingViewModelTest {

    private lateinit var viewModel: SortingViewModel
    private val size = 10
    private val dealList = List(size) { index ->
        Deal("2022-12-0${index} 1$index:2$index:44").apply {
            tradeDate = "2022-12-0${index} 1$index:2$index:44"
            company = when(index) {
                in 0..3 -> {
                    ticker = "AAA"
                    "AAAAAAA BB"
                }
                in 4..7 -> {
                    ticker = "SSS"
                    "SSSSSSSSS FFFFF"
                }
                else -> {
                    ticker = "HHH"
                    "HHHHHHHH LLL"
                }
            }
            insiderName = when(index) {
                in 0..2 -> "Backer Richard"
                in 3..4 -> "Hill W Bryan"
                in 5..7 -> "Fisker Henrik"
                else -> "Dixon Robert D"
            }
            volumeStr = "${10.0 * (index + 1)}"
        }
    }.shuffled()

    @Before
    fun setUp() {
        viewModel = SortingViewModel()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun doSort_Insider_Volume_Desc() {

        val sorting = SortingViewModel.Sorting(
            groupingBy = SortingViewModel.Sorting.GroupingBy.INSIDER,
            sortingBy = SortingViewModel.Sorting.SortingBy.VOLUME,
            orderBy = SortingViewModel.Sorting.OrderBy.DESC
        )
        val actualMap = viewModel.doSort(dealList, sorting)
        actualMap.forEach {
            val actualResult = it.value.first().volume > it.value.last().volume
            Assert.assertTrue(actualResult)
        }
    }

    @Test
    fun doSort_empty_list() {
        val sorting = SortingViewModel.Sorting(
            groupingBy = SortingViewModel.Sorting.GroupingBy.INSIDER,
            sortingBy = SortingViewModel.Sorting.SortingBy.VOLUME,
            orderBy = SortingViewModel.Sorting.OrderBy.DESC
        )
        val actualMap = viewModel.doSort(emptyList(), sorting)
        Assert.assertTrue(actualMap.isEmpty())
    }

    @Test
    fun doSort_groupBy_ticker_sortBy_volume_asc(){
        val sorting = SortingViewModel.Sorting(
            groupingBy = SortingViewModel.Sorting.GroupingBy.TICKER,
            sortingBy = SortingViewModel.Sorting.SortingBy.VOLUME,
            orderBy = SortingViewModel.Sorting.OrderBy.ASC
        )
        val actualMap = viewModel.doSort(dealList, sorting)
        actualMap.forEach {
            val actualResult = it.value.first().volume < it.value.last().volume
            Assert.assertTrue(actualResult)
        }
    }

    @Test
    fun doSort_Filling_date_Volume_Desc() {
        val sorting = SortingViewModel.Sorting(
            groupingBy = SortingViewModel.Sorting.GroupingBy.FILLING_DATE,
            sortingBy = SortingViewModel.Sorting.SortingBy.VOLUME,
            orderBy = SortingViewModel.Sorting.OrderBy.DESC
        )
        val actualMap = viewModel.doSort(dealList, sorting)
        val actualKeySize = actualMap.keys.size
        Assert.assertEquals(10, actualKeySize)

        val actualDateStart = actualMap.keys.first()
        val actualDateEnd = actualMap.keys.last()
        Assert.assertEquals("2022-12-09 19:29:44", actualDateStart)
        Assert.assertEquals("2022-12-00 10:20:44", actualDateEnd)
    }

    @Test
    fun doSort_Ticker_Filing_date_Asc() {
        val sorting = SortingViewModel.Sorting(
            groupingBy = SortingViewModel.Sorting.GroupingBy.TICKER,
            sortingBy = SortingViewModel.Sorting.SortingBy.FILLING_DATE,
            orderBy = SortingViewModel.Sorting.OrderBy.ASC
        )
        val list = mutableListOf<Deal>()
        repeat(10) {
            list.addAll(dealList)
        }
        val actualMap = viewModel.doSort(list, sorting)
        actualMap.forEach { (key, list) ->
            list.forEach {
                val ticker = it.ticker
                Assert.assertEquals(key, ticker)
            }
        }
    }
}