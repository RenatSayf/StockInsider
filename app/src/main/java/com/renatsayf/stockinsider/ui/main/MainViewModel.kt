package com.renatsayf.stockinsider.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Deferred

class MainViewModel @ViewModelInject constructor(private val repositoryImpl: DataRepositoryImpl) : ViewModel()
{
    private var subscribe: Disposable? = null

    private var _searchSet = MutableLiveData<RoomSearchSet>().apply {
        value = searchSet?.value
    }
    var searchSet : LiveData<RoomSearchSet> = _searchSet
    fun setSearchSet(set : RoomSearchSet)
    {
        _searchSet.value = set
    }

    fun getAllSearchSets() : List<RoomSearchSet>
    {
        return repositoryImpl.getAllSearchSetsFromDbAsync()
    }

    fun getUserSearchSets() : List<RoomSearchSet>
    {
        return repositoryImpl.getUserSearchSetsFromDbAsync()
    }

    suspend fun getSearchSetAsync(setName: String) : RoomSearchSet = run {
        repositoryImpl.getSearchSetFromDbAsync(setName)
    }

    suspend fun saveSearchSet(set: RoomSearchSet) : Long
    {
        return repositoryImpl.saveSearchSetAsync(set)
    }

    fun deleteSearchSet(set: RoomSearchSet) = run {
        repositoryImpl.deleteSearchSetAsync(set)
    }

    private var _companies = MutableLiveData<Array<Companies>>().apply {
        value = getCompanies()
    }
    var companies : LiveData<Array<Companies>> = _companies

    private fun getCompanies() : Array<Companies>? = run{
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

    override fun onCleared()
    {
        subscribe?.dispose()
        super.onCleared()
    }
}