package com.renatsayf.stockinsider.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val repositoryImpl: DataRepositoryImpl) : ViewModel()
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
        return repositoryImpl.getUserSearchSetsFromDbAsync() as MutableList<RoomSearchSet>
    }

    private var _searchSetList = MutableLiveData<MutableList<RoomSearchSet>>().apply {
        viewModelScope.launch {
            value = getUserSearchSets()
        }
    }
    //val searchSetList: LiveData<MutableList<RoomSearchSet>> = _searchSetList

    fun getCurrentSearchSet(setName: String): LiveData<RoomSearchSet> {
        val set = MutableLiveData<RoomSearchSet>()
        viewModelScope.launch {
            val searchSet = repositoryImpl.getSearchSetFromDbAsync(setName)
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

    fun saveSearchSet(set: RoomSearchSet): LiveData<Long>
    {
        val id = MutableLiveData<Long>()
        viewModelScope.launch {
            id.value = repositoryImpl.saveSearchSetAsync(set)
        }
        return id
    }

    fun deleteSearchSet(set: RoomSearchSet)  {
        viewModelScope.launch {
            val res = repositoryImpl.deleteSearchSetAsync(set)
            if (res > 0) {
                _searchSetList.value = getUserSearchSets()
                _searchSetList
            }
        }
    }

    private var _companies = MutableLiveData<Array<Companies>>().apply {
        CoroutineScope(Dispatchers.IO).launch {
            val c = getCompanies()
            postValue(c)
        }
    }
    var companies : LiveData<Array<Companies>> = _companies

    private suspend fun getCompanies() : Array<Companies>? = run {
        repositoryImpl.getCompaniesFromDbAsync()
    }

    override fun onCleared()
    {
        repositoryImpl.destructor()
        super.onCleared()
    }



}