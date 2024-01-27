package com.renatsayf.stockinsider.ui.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import com.renatsayf.stockinsider.utils.getTimeOffsetIfWeekEnd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val app: Application,
    private val repositoryImpl: DataRepositoryImpl
) : AndroidViewModel(app) {

    sealed class State {
        data class Initial(val set: RoomSearchSet) : State()
        data class DataReceived(val deals: List<Deal>) : State()
        data class DataSorted(val dealsMap: Map<String, List<Deal>>) : State()
        data class DataError(val throwable: Throwable) : State()
    }

    private var _state = MutableLiveData<State>()
    val state: LiveData<State> = _state
    fun setState(state: State) {
        _state.value = state
        if (state is State.Initial) {
            dbSearchSet = state.set
        }
    }

    var dbSearchSet: RoomSearchSet? = null
        private set

    fun getDealListFromNet(set: SearchSet) {
        viewModelScope.launch {
            try {

                if (set.filingPeriod.toInt() < 4) {
                    set.filingPeriod = getTimeOffsetIfWeekEnd(set.filingPeriod.toInt()).toString()
                    set.tradePeriod = getTimeOffsetIfWeekEnd(set.tradePeriod.toInt()).toString()
                }
                val result = repositoryImpl.getTradingListFromNetAsync(set).await()
                result.onSuccess { list ->
                    _state.value = State.DataReceived(list)
                    val companies = list.filter { deal ->
                        deal.ticker != null && deal.company != null
                    }.map { deal ->
                        Company(ticker = deal.ticker!!, company = deal.company!!)
                    }
                    viewModelScope.launch {
                        repositoryImpl.insertCompanies(companies)
                    }
                }
                result.onFailure { exception ->
                    _state.value = State.DataError(exception)
                }
            }
            catch (e: Exception) {
                _state.value = State.DataError(e)
            }
        }
    }

    fun getDisplayedQueryName(set: RoomSearchSet): String {
        val actions = when {
            set.isPurchase && set.isSale -> app.getString(R.string.text_purchases_and_sales)
            !set.isPurchase && !set.isSale -> app.getString(R.string.text_purchases_and_sales)
            set.isPurchase && !set.isSale -> app.getString(R.string.text_purchases)
            else -> app.getString(R.string.text_sales)
        }
        val array = app.resources.getStringArray(R.array.list_for_filing_date)
        val period = when (set.filingPeriod) {
            0 -> array[0]
            1 -> array[1]
            2 -> array[2]
            3 -> array[3]
            4 -> array[3]
            5 -> array[3]
            6 -> array[3]
            7 -> array[4]
            14 -> array[5]
            30 -> array[6]
            60 -> array[7]
            90 -> array[8]
            180 -> array[9]
            365 -> array[10]
            730 -> array[11]
            1461 -> array[12]
            else -> ""
        }
        return "$actions ${period.lowercase()}."
    }

}
