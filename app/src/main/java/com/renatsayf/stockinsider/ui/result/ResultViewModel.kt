package com.renatsayf.stockinsider.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ResultViewModel @Inject constructor(private val repositoryImpl: DataRepositoryImpl) : ViewModel() {

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

}
