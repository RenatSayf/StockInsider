package com.renatsayf.stockinsider.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(private val repositoryImpl: DataRepositoryImpl) : ViewModel() {

    sealed class State {
        object Initial : State()
        data class DataReceived(val deals: List<Deal>) : State()
        data class DataSorted(val dealsMap: Map<String, List<Deal>>) : State()
        data class DataError(val throwable: Throwable) : State()
    }

    private val composite = CompositeDisposable()

    private var _state = MutableLiveData<State>().apply {
        value = State.Initial
    }
    val state: LiveData<State> = _state
    fun setState(state: State) {
        _state.value = state
    }

    fun getDealList(set: SearchSet) {
        composite.add(
            repositoryImpl.getTradingScreenFromNetAsync(set)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    _state.value = State.DataReceived(list)
                    val companies = list.filter { deal ->
                        deal.ticker != null && deal.company != null
                    }.map { deal ->
                        Company(ticker = deal.ticker!!, company = deal.company!!)
                    }
                    viewModelScope.launch {
                        repositoryImpl.insertCompanies(companies)
                    }
                }, { t ->
                    _state.value = State.DataError(t)
                })
        )
    }

    fun getDealListFromNet(set: SearchSet) {
        viewModelScope.launch {
            try {
                val dealList = repositoryImpl.getTradingListFromNetAsync(set).await()
                _state.value = State.DataReceived(dealList)
                val companies = dealList.filter { deal ->
                    deal.ticker != null && deal.company != null
                }.map { deal ->
                    Company(ticker = deal.ticker!!, company = deal.company!!)
                }
                viewModelScope.launch {
                    repositoryImpl.insertCompanies(companies)
                }
            }
            catch (e: Exception) {
                _state.value = State.DataError(e)
            }
        }
    }

    override fun onCleared() {
        composite.apply {
            dispose()
            clear()
        }
        super.onCleared()
    }
}
