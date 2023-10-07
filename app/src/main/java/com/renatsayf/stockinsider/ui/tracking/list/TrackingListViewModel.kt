package com.renatsayf.stockinsider.ui.tracking.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
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

    fun getTrackedCount(): LiveData<Result<Int>> {
        val result: MutableLiveData<Result<Int>> = MutableLiveData()
        viewModelScope.launch {
            try {
                val count = repository.getTrackedCountAsync().await()
                result.postValue(Result.success(count))
            } catch (e: Exception) {
                result.postValue(Result.failure(e))
            }
        }
        return result
    }

    fun targetCount(): LiveData<Int?> {
        val result: MutableLiveData<Int?> = MutableLiveData(null)
        viewModelScope.launch {
            result.value = repository.getTargetCountAsync().await()
        }
        return result
    }

    fun getTrackedCountSync(): Int {
        return runBlocking {
            repository.getTrackedCountAsync().await()
        }
    }


}