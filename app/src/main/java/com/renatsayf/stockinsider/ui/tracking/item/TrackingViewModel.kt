package com.renatsayf.stockinsider.ui.tracking.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: DataRepositoryImpl
) : ViewModel() {

    sealed class State {
        data class Edit(val flag: Boolean): State()
        data class OnEdit(val set: RoomSearchSet): State()
        data class OnSave(val set: RoomSearchSet): State()
    }

    private var _state = MutableLiveData<State>()
    val state: LiveData<State> = _state
    fun setState(state: State) {
        _state.value = state
        if (_state.value is State.OnEdit) {
            newSet = (_state.value as State.OnEdit).set
        }
    }

    var newSet: RoomSearchSet? = null

    fun getSearchSetById(id: Long) : StateFlow<RoomSearchSet?> {
        val result = MutableStateFlow<RoomSearchSet?>(null)
        viewModelScope.launch {
            result.value = repository.getSearchSetByIdAsync(id).await()
        }
        return result
    }

    override fun onCleared() {

        newSet = null
        super.onCleared()
    }
}