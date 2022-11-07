package com.renatsayf.stockinsider.ui.tracking.companies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class CompaniesViewModel @Inject constructor(
    private val repository: DataRepositoryImpl
): ViewModel() {

    sealed class State {
        data class Initial(val ticker: String): State()
        data class Current(val companies: List<Company>): State()
        data class OnUpdate(val companies: List<Company>): State()
        object OnAdding: State()
        data class Error(val message: String): State()
    }

    private var _state = MutableLiveData<State>(State.Initial(""))
    val state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }

    fun getCompaniesByTicker(tickers: List<String>) : LiveData<List<Company>?> {
        val companies = MutableLiveData<List<Company>?>(null)
        viewModelScope.launch {
            companies.value = repository.getCompanyByTicker(tickers)
        }
        return companies
    }

    fun getAllSimilarCompanies(pattern: String): LiveData<List<Company>?> {
        val companies = MutableLiveData<List<Company>?>(null)
        viewModelScope.launch {
            val result = repository.getAllSimilar(pattern)
            companies.value = result
        }
        return companies
    }

    fun addCompanyToSearch(setName: String, value: String): LiveData<Int> {
        val result = MutableLiveData(-1)
        viewModelScope.launch {
            try {
                result.value = repository.updateSearchSetTickerAsync(setName, value).await()
                //_state.value = State.OnAdding
            } catch (e: Exception) {
                result.value = -1
            }
        }
        return result
    }
}