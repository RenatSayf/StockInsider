package com.renatsayf.stockinsider.ui.tracking.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: DataRepositoryImpl
) : ViewModel() {

    sealed class State {
        data class Initial(val set: RoomSearchSet, val editFlag: Boolean): State()
        data class OnEdit(val set: RoomSearchSet): State()
        data class OnSave(val set: RoomSearchSet): State()
    }

    private var _state = MutableLiveData<State>()
    val state: LiveData<State> = _state
    fun setState(state: State) {
        _state.value = state
        when (_state.value) {
            is State.OnEdit -> {
                _newSet = (_state.value as State.OnEdit).set
            }
            is State.Initial -> {
                _newSet = (_state.value as State.Initial).set
            }
            else -> {}
        }
    }

    private var _newSet: RoomSearchSet? = null
    val newSet: RoomSearchSet?
        get() {
            return _newSet
        }

    fun getSearchSetById(id: Long) : LiveData<RoomSearchSet?> {

        val result = MutableLiveData<RoomSearchSet?>(null)
        viewModelScope.launch {
            result.value = repository.getSearchSetByIdAsync(id).await()
        }
        return result
    }


}