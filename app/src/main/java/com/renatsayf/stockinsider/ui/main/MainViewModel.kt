package com.renatsayf.stockinsider.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.utils.AppLog
import kotlinx.coroutines.*

class MainViewModel @ViewModelInject constructor(
        private val db: AppDao,
        private val appLog: AppLog
) : ViewModel()
{
    private var _searchSet = MutableLiveData<RoomSearchSet>().apply {
        value = searchSet?.value
    }
    var searchSet : LiveData<RoomSearchSet> = _searchSet
    fun setSearchSet(set : RoomSearchSet)
    {
        _searchSet.value = set
    }

    fun getSearchSet(setName: String) : RoomSearchSet = runBlocking {
        val set = async {
            db.getSetByName(setName)
        }
        set.await()
    }

    fun saveDefaultSearch(set : RoomSearchSet)
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
        value = getCompanies()
    }
    var companies : LiveData<Array<Companies>> = _companies

    private fun getCompanies() : Array<Companies>? = runBlocking{
        val companies = async {
             db.getAllCompanies().toTypedArray()
        }
        companies.await()
    }


}