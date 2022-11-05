package com.renatsayf.stockinsider.ui.tracking.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TrackingListViewModel @Inject constructor(private val repository: DataRepositoryImpl) : ViewModel() {

    sealed class State {
        data class Initial(val list: List<RoomSearchSet>): State()
    }

    private var _state = MutableLiveData<State>()
    val state: LiveData<State> = _state
    fun setState(state: State) {
        _state.value = state
    }

}