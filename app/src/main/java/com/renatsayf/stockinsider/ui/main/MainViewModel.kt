package com.renatsayf.stockinsider.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val repositoryImpl: DataRepositoryImpl, private val app: Application) : AndroidViewModel(app)
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

    private var subscribe: Disposable? = null

    private var _searchSet = MutableLiveData<RoomSearchSet>().apply {
        value = searchSet?.value
    }
    var searchSet : LiveData<RoomSearchSet> = _searchSet
    fun setSearchSet(set : RoomSearchSet)
    {
        _searchSet.value = set
    }

//    fun get_UserSearchSets() {
//        viewModelScope.launch {
//            val sets = repositoryImpl.getUserSearchSetsFromDbAsync()
//            _state.value = State.Current(sets)
//        }
//    }

    private suspend fun getUserSearchSets() : MutableList<RoomSearchSet>
    {
        return repositoryImpl.getUserSearchSetsFromDbAsync() as MutableList<RoomSearchSet>
    }

//    suspend fun getSearchSetAsync(setName: String) : RoomSearchSet = run {
//        repositoryImpl.getSearchSetFromDbAsync(setName)
//    }

    private var _searchSetList = MutableLiveData<MutableList<RoomSearchSet>>().apply {
        viewModelScope.launch {
            value = getUserSearchSets()
        }
    }
    val searchSetList: LiveData<MutableList<RoomSearchSet>> = _searchSetList

    fun getCurrentSearchSet(setName: String) {
        viewModelScope.launch {
            val searchSet = repositoryImpl.getSearchSetFromDbAsync(setName)
            _state.value = State.Initial(searchSet)
        }
    }

    fun saveSearchSet(set: RoomSearchSet)
    {
        viewModelScope.launch {
            val id = repositoryImpl.saveSearchSetAsync(set)
        }
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

    fun getDealList(set: SearchSet)
    {
        subscribe = repositoryImpl.getTradingScreenFromNetAsync(set)
            .subscribe({ list ->
                           dealList.value = list
                       }, { t ->
                           documentError.value = t
                       })
    }

    var dealList: MutableLiveData<ArrayList<Deal>> = MutableLiveData()

    var documentError : MutableLiveData<Throwable> = MutableLiveData()

    init
    {
        val setName = app.getString(R.string.text_current_set_name)
        getCurrentSearchSet(setName)
    }

    override fun onCleared()
    {
        subscribe?.dispose()
        repositoryImpl.destructor()
        super.onCleared()
    }
}