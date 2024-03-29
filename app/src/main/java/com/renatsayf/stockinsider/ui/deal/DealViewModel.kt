package com.renatsayf.stockinsider.ui.deal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DealViewModel @Inject constructor(
    private val repositoryImpl: DataRepositoryImpl
) : ViewModel() {

    sealed class State {
        object OnLoad : State()
        data class OnData(val data: List<Deal>) : State()
        data class OnError(val error: String) : State()
    }

    private var _state = MutableLiveData<State>()
    var state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }

    fun getInsiderDeals(insider: String) {
        viewModelScope.launch {
            _state.value = State.OnLoad
            val result = repositoryImpl.getInsiderTradingFromNetAsync2(insider).await()
            result.onSuccess { list ->
                _state.value = State.OnData(list)
            }
            result.onFailure { exception ->
                if (BuildConfig.DEBUG) exception.printStackTrace()
                _state.value = State.OnError(exception.message ?: "Unknown error")
            }
        }
    }

    private var _deal = MutableLiveData<Deal>()
    var deal: LiveData<Deal> = _deal
    fun setDeal(value: Deal) {
        _deal.value = value
    }

    fun getDealsByTicker(ticker: String) {
        viewModelScope.launch {
            _state.value = State.OnLoad
            val result = repositoryImpl.getDealsByTicker(ticker).await()
            result.onSuccess { list ->
                _state.value = State.OnData(list)
            }
            result.onFailure { exception ->
                if (BuildConfig.DEBUG) exception.printStackTrace()
                _state.value = State.OnError(exception.message ?: "Unknown error")
            }
        }
    }

}
