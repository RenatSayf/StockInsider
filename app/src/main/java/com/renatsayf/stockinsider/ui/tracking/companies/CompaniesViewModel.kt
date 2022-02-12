package com.renatsayf.stockinsider.ui.tracking.companies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class CompaniesViewModel @Inject constructor(
    private val repository: DataRepositoryImpl
): ViewModel() {

    sealed class State {
        object Initial: State()
        object OnAdding: State()
    }

    private var _state = MutableLiveData<State>(State.Initial)
    val state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }

    fun getCompaniesByTicker(tickers: List<String>) : LiveData<List<Companies>?> {
        val companies = MutableLiveData<List<Companies>?>(null)
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val result = repository.getCompanyByTicker(tickers)
                companies.value = result
            }
        }
        return companies
    }

    fun getAllSimilarCompanies(pattern: String): LiveData<List<Companies>?> {
        val companies = MutableLiveData<List<Companies>?>(null)
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val result = repository.getAllSimilar(pattern)
                companies.value = result
            }
        }
        return companies
    }
}