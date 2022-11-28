package com.renatsayf.stockinsider.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    fun saveSearchSet(set: RoomSearchSet): LiveData<Long>
    {
        val id = MutableLiveData<Long>(-1)
        viewModelScope.launch {
            try {
                id.value = repository.saveSearchSetAsync(set).await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return id
    }

    fun deleteSearchSet(set: RoomSearchSet): StateFlow<Int>  {

        val res = MutableStateFlow(-1)
        viewModelScope.launch {
            res.value = repository.deleteSearchSetAsync(set).await()
        }
        return res
    }

    fun deleteSearchSetById(id: Long): StateFlow<Int>  {

        val res = MutableStateFlow(-1)
        viewModelScope.launch {
            res.value = repository.deleteSetByIdAsync(id).await()
        }
        return res
    }

    private var _companies = MutableLiveData<Array<Company>>().apply {
        viewModelScope.launch {
            val c = getCompanies()
            postValue(c)
        }
    }
    var companies : LiveData<Array<Company>> = _companies

    private suspend fun getCompanies() : Array<Company>? = run {
        repository.getCompaniesFromDbAsync()
    }

    override fun onCleared()
    {
        repository.destructor()
        super.onCleared()
    }



}