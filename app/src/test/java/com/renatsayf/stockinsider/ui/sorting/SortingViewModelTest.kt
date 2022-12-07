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
        Deal("2022-12-0${index} 1$index:21:44").apply {
            tradeDate = "2022-12-0${index} 1$index:21:44"
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
    fun doSort() {

        val sorting = SortingViewModel.Sorting(
            groupingBy = SortingViewModel.Sorting.GroupingBy.INSIDER,
            sortingBy = SortingViewModel.Sorting.SortingBy.VOLUME,
            orderBy = SortingViewModel.Sorting.OrderBy.DESC
        )
        val actualList = viewModel.doSort(dealList, sorting)
        val actualResult = actualList[0].volume > actualList[size - 1].volume
        Assert.assertTrue(actualResult)
    }

    @Test
    fun doSort_empty_list() {
        val sorting = SortingViewModel.Sorting(
            groupingBy = SortingViewModel.Sorting.GroupingBy.INSIDER,
            sortingBy = SortingViewModel.Sorting.SortingBy.VOLUME,
            orderBy = SortingViewModel.Sorting.OrderBy.DESC
        )
        val actualList = viewModel.doSort(emptyList(), sorting)
        Assert.assertTrue(actualList.isEmpty())
    }
}