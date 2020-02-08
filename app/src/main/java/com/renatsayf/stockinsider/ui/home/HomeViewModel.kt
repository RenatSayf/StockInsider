package com.renatsayf.stockinsider.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.db.AppDao
import kotlinx.coroutines.*

class HomeViewModel : ViewModel()
{
     private var _searchSet = MutableLiveData<RoomSearchSet>().apply {
        value = searchSet?.value
    }
    var searchSet : LiveData<RoomSearchSet> = _searchSet
    fun setSearchSet(set : RoomSearchSet)
    {
        _searchSet.value = set
    }

    fun getSearchSetByName(db : AppDao, setName : String)
    {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            withContext(Dispatchers.Main) {
                try
                {
                    val set = db.getSetByName(setName)
                    _searchSet.postValue(set)
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveDefaultSearch(db : AppDao, set : RoomSearchSet)
    {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try
                {
                    val res = db.insertOrUpdateSearchSet(set)
                    res
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        }
    }




}