package com.renatsayf.stockinsider.ui.tracking.companies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
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
}