package com.renatsayf.stockinsider.ui.deal

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
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

    private var composite = CompositeDisposable()

    fun getInsiderDeals(insider: String): LiveData<ArrayList<Deal>> {
        _state.value = State.OnLoad
        val deals = MutableLiveData<ArrayList<Deal>>()
        composite.add(repositoryImpl.getInsiderTradingFromNetAsync(insider)
            .subscribe({ list ->
                deals.value = list
                _state.value = State.OnData(list)
            }, { t ->
                if (BuildConfig.DEBUG) t.printStackTrace()
                deals.value = arrayListOf()
                _state.value = State.OnError(t.message ?: "Unknown error")
            })
        )
        return deals
    }

    fun getInsiderDeals2(insider: String) {
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

    private var _chart = MutableLiveData<Drawable>()
    var chart: LiveData<Drawable> = _chart
    fun setChart(chart: Drawable) {
        _chart.value = chart
    }

    fun getTradingByTicker(ticker: String): LiveData<ArrayList<Deal>> {
        _state.value = State.OnLoad
        val deals = MutableLiveData<ArrayList<Deal>>()
        composite.add(repositoryImpl.getTradingByTickerAsync(ticker)
            .subscribe({ list ->
                deals.value = list
                _state.value = State.OnData(list)
            }, { t ->
                if (BuildConfig.DEBUG) t.printStackTrace()
                deals.value = arrayListOf()
                _state.value = State.OnError(t.message ?: "Unknown error")
            })
        )
        return deals
    }

    override fun onCleared() {
        super.onCleared()
        composite.apply {
            dispose()
            clear()
        }
        repositoryImpl.destructor()
    }
}
