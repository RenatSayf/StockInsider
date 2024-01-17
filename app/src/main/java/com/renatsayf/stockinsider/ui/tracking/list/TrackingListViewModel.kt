package com.renatsayf.stockinsider.ui.tracking.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import com.renatsayf.stockinsider.utils.printStackTraceIfDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    fun getTrackedCountSync(): Int {
        return runBlocking {
            repository.getTrackedCountAsync().await()
        }
    }

    fun getTrackedCountAsync(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val count = repository.getTrackedCountAsync().await()
                onSuccess.invoke(count)
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                onError.invoke(e)
            }
        }
    }


}