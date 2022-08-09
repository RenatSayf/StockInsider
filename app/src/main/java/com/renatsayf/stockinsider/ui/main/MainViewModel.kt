package com.renatsayf.stockinsider.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val repository: DataRepositoryImpl) : ViewModel()
{
    sealed class State {
        data class Initial(val set: RoomSearchSet): State()
    }

    private var _state = MutableLiveData<State>().apply {

    }
    val state: LiveData<State> = _state
    fun setState(state: State) {
        _state.value = state
    }

    private var _searchSet = MutableLiveData<RoomSearchSet>().apply {
        value = searchSet?.value
    }
    var searchSet : LiveData<RoomSearchSet>? = _searchSet
    fun setSearchSet(set : RoomSearchSet)
    {
        _searchSet.value = set
    }

    private suspend fun getUserSearchSets() : MutableList<RoomSearchSet>
    {
        return repository.getUserSearchSetsFromDbAsync() as MutableList<RoomSearchSet>
    }

    private var _searchSetList = MutableLiveData<MutableList<RoomSearchSet>>().apply {
        viewModelScope.launch {
            value = getUserSearchSets()
        }
    }
    //val searchSets: LiveData<MutableList<RoomSearchSet>> = _searchSetList

    fun getSearchSetByName(setName: String): LiveData<RoomSearchSet> {
        val set = MutableLiveData<RoomSearchSet>()
        viewModelScope.launch {
            val searchSet = repository.getSearchSetFromDbAsync(setName)
            set.value = searchSet
        }
        return set
    }

    fun getSearchSetList(): LiveData<List<RoomSearchSet>> {
        val list = MutableLiveData<List<RoomSearchSet>>()
        viewModelScope.launch {
            list.value = getUserSearchSets()
        }
        return list
    }

    fun getSearchSetsByTarget(target: String): LiveData<List<RoomSearchSet>> {
        val sets = MutableLiveData<List<RoomSearchSet>>()
        viewModelScope.launch {
            sets.value = repository.getSearchSetsByTarget(target)

        }
        return sets
    }

    fun saveSearchSet(set: RoomSearchSet): StateFlow<Long>
    {
        val id = MutableStateFlow<Long>(-1)
        viewModelScope.launch {
            val res = repository.saveSearchSetAsync(set).await()
            id.emit(res)
        }
        return id
    }

    fun deleteSearchSet(set: RoomSearchSet)  {
        viewModelScope.launch {
            val res = repository.deleteSearchSetAsync(set)
            if (res > 0) {
                _searchSetList.value = getUserSearchSets()
                _searchSetList
            }
        }
    }

    private var _companies = MutableLiveData<Array<Companies>>().apply {
        viewModelScope.launch {
            val c = getCompanies()
            postValue(c)
        }
    }
    var companies : LiveData<Array<Companies>> = _companies

    private suspend fun getCompanies() : Array<Companies>? = run {
        repository.getCompaniesFromDbAsync()
    }

    override fun onCleared()
    {
        repository.destructor()
        super.onCleared()
    }



}