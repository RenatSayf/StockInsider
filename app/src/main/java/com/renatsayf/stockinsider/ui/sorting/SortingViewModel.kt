package com.renatsayf.stockinsider.ui.sorting

import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.models.Deal
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.Serializable
import javax.inject.Inject


@HiltViewModel
class SortingViewModel @Inject constructor() : ViewModel() {

    fun doSort(list: List<Deal>, sorting: Sorting): List<Deal> {

        val mutableList = list.toMutableList()

        val groupedMap = when (sorting.groupingBy) {
            Sorting.GroupingBy.TICKER -> {
                val map = mutableList.groupBy {
                    it.ticker
                }
                map
            }
            Sorting.GroupingBy.INSIDER -> {
                val map = mutableList.groupBy {
                    it.insiderName
                }
                map
            }
            else -> {
                val map = mutableList.groupBy {
                    it.filingDate
                }
                map
            }
        }
        val sortedMap = mutableMapOf<String, List<Deal>>()
        groupedMap.forEach { (t, u) ->
            val deals = when (sorting.sortingBy) {

                Sorting.SortingBy.FILLING_DATE -> {
                    if (sorting.orderBy == Sorting.OrderBy.ASC) u.sortedBy {
                        it.filingDate
                    } else u.sortedByDescending {
                        it.filingDate
                    }
                }
                Sorting.SortingBy.TRADE_DATE -> {
                    if (sorting.orderBy == Sorting.OrderBy.ASC) u.sortedBy {
                        it.tradeDate
                    } else u.sortedByDescending {
                        it.tradeDate
                    }
                }
                Sorting.SortingBy.TICKER -> {
                    if (sorting.orderBy == Sorting.OrderBy.ASC) u.sortedBy {
                        it.ticker
                    } else u.sortedByDescending {
                        it.ticker
                    }
                }
                Sorting.SortingBy.VOLUME -> {
                    if (sorting.orderBy == Sorting.OrderBy.ASC) u.sortedBy {
                        it.volume
                    } else u.sortedByDescending {
                        it.volume
                    }
                }
            }
            t?.let {
                sortedMap[it] = deals
            }
        }

        val sortedList = mutableListOf<Deal>()
        sortedMap.forEach { (_, list) ->
            sortedList.addAll(list)
        }
        return sortedList
    }

    data class Sorting(
        var groupingBy: GroupingBy = GroupingBy.NOT,
        var sortingBy: SortingBy = SortingBy.FILLING_DATE,
        var orderBy: OrderBy = OrderBy.ASC
    ) : Serializable {

        enum class GroupingBy {
            NOT, TICKER, INSIDER
        }
        enum class SortingBy {
            FILLING_DATE, TRADE_DATE, TICKER, VOLUME
        }
        enum class OrderBy {
            ASC, DESC
        }
    }
}