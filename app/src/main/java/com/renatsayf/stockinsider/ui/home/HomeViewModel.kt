package com.renatsayf.stockinsider.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.db.SearchSetDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun getSearchSetByName(db : SearchSetDao, setName : String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val set = withContext(Dispatchers.Main) {
                db.getSetByName(setName)
            }
            try
            {
                _searchSet.postValue(set)
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }
        }
    }

    fun saveSearchByName(db : SearchSetDao, setName : RoomSearchSet) : Long
    {
        var res : Long = Long.MIN_VALUE
        CoroutineScope(Dispatchers.IO).launch {
            res = withContext(Dispatchers.Main) {
                db.insertOrUpdateSearchSet(setName)
            }
        }
        return res
    }




}