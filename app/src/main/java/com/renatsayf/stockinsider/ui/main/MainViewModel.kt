package com.renatsayf.stockinsider.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*

class MainViewModel @ViewModelInject constructor(
        private val db: AppDao,
        private val repositoryImpl: DataRepositoryImpl
) : ViewModel()
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

    fun getSearchSetAsync(setName: String) : RoomSearchSet = runBlocking {
        val set = async {
            db.getSetByName(setName)
        }
        set.await()
    }

    fun saveDefaultSearchAsync(set : RoomSearchSet)
    {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try
                {
                    db.insertOrUpdateSearchSet(set)
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        }
    }

    private var _companies = MutableLiveData<Array<Companies>>().apply {
        value = getCompaniesAsync()
    }
    var companies : LiveData<Array<Companies>> = _companies

    private fun getCompaniesAsync() : Array<Companies>? = runBlocking{
        val companies = async {
             db.getAllCompanies().toTypedArray()
        }
        companies.await()
    }

    fun getDealListAsync(set: SearchSet)
    {
        subscribe = repositoryImpl.searchRequest.getTradingScreen(set)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
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