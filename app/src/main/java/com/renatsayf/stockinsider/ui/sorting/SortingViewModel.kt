package com.renatsayf.stockinsider.ui.sorting

import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.utils.convertDefaultWithoutTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.Serializable
import javax.inject.Inject


@HiltViewModel
class SortingViewModel @Inject constructor() : ViewModel() {

    private var _sorting: Sorting = Sorting()
    val sorting: Sorting = _sorting

    fun doSort(list: List<Deal>, sorting: Sorting): Map<String, List<Deal>> {

        _sorting = sorting
        val mutableList = list.toMutableList()

        val sortedList = when (sorting.sortingBy) {

            Sorting.SortingBy.FILLING_DATE -> {
                if (sorting.orderBy == Sorting.OrderBy.ASC) mutableList.sortedBy {
                    it.filingDate
                } else mutableList.sortedByDescending {
                    it.filingDate
                }
            }
            Sorting.SortingBy.TRADE_DATE -> {
                if (sorting.orderBy == Sorting.OrderBy.ASC) mutableList.sortedBy {
                    it.tradeDate
                } else mutableList.sortedByDescending {
                    it.tradeDate
                }
            }
            Sorting.SortingBy.TICKER -> {
                if (sorting.orderBy == Sorting.OrderBy.ASC) mutableList.sortedBy {
                    it.ticker
                } else mutableList.sortedByDescending {
                    it.ticker
                }
            }
            Sorting.SortingBy.VOLUME -> {
                if (sorting.orderBy == Sorting.OrderBy.ASC) mutableList.sortedBy {
                    it.volume
                } else mutableList.sortedByDescending {
                    it.volume
                }
            }
        }

        val groupedMap = when (sorting.groupingBy) {
            Sorting.GroupingBy.TICKER -> {
                val map = sortedList.groupBy {
                    it.ticker
                }
                map
            }
            Sorting.GroupingBy.INSIDER -> {
                val map = sortedList.groupBy {
                    it.insiderName
                }
                map
            }
            else -> {
                val map = sortedList.groupBy {
                    it.filingDate?.convertDefaultWithoutTime()
                }
                map
            }
        }

        val mutableMap = groupedMap.toMutableMap()
        val sortedMap = mutableMapOf<String, List<Deal>>()
        val entries = if (sorting.orderBy == Sorting.OrderBy.ASC) mutableMap.entries.sortedBy {
            it.key
        } else mutableMap.entries.sortedByDescending {
            it.key
        }
        entries.forEach { entry ->
            entry.key?.let {
                sortedMap[it] = entry.value
            }
        }
        return sortedMap
    }

    data class Sorting(
        var groupingBy: GroupingBy = GroupingBy.FILLING_DATE,
        var sortingBy: SortingBy = SortingBy.FILLING_DATE,
        var orderBy: OrderBy = OrderBy.ASC
    ) : Serializable {

        enum class GroupingBy {
            FILLING_DATE, TICKER, INSIDER
        }
        enum class SortingBy {
            FILLING_DATE, TRADE_DATE, TICKER, VOLUME
        }
        enum class OrderBy {
            ASC, DESC
        }
    }
}