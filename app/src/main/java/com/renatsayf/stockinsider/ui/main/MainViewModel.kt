package com.renatsayf.stockinsider.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.ResultData
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import com.renatsayf.stockinsider.utils.appPref
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DataRepositoryImpl,
    app: Application? = Application()
) : ViewModel() {

    sealed class State {
        data class Initial(val set: RoomSearchSet) : State()
    }

    private val composite = CompositeDisposable()
    private var _state = MutableLiveData<State>()
    val state: LiveData<State> = _state
    fun setState(state: State) {
        _state.value = state
    }

    private var _searchSet = MutableLiveData<RoomSearchSet>().apply {
        value = searchSet?.value
    }
    var searchSet: LiveData<RoomSearchSet>? = _searchSet
    fun setSearchSet(set: RoomSearchSet) {
        _searchSet.value = set
    }

    private suspend fun getUserSearchSets(): MutableList<RoomSearchSet> {
        return repository.getUserSearchSetsFromDbAsync() as MutableList<RoomSearchSet>
    }

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

    fun addNewSearchSet(set: RoomSearchSet): LiveData<ResultData<Long>> {
        val res = MutableLiveData<ResultData<Long>>(ResultData.Init)
        viewModelScope.launch {
            try {
                val id = repository.addNewSearchSetAsync(set).await()
                res.value = ResultData.Success(id)
            } catch (e: Exception) {
                res.value = ResultData.Error(e.message ?: "Unknown error...")
            }
        }
        return res
    }

    fun saveSearchSet(set: RoomSearchSet): LiveData<Long?> {
        val res = MutableLiveData<Long?>(null)
        viewModelScope.launch {
            try {
                res.value = repository.saveSearchSetAsync(set).await()

            } catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                res.value = -1
            }
        }
        return res
    }

    fun deleteSearchSet(set: RoomSearchSet): StateFlow<Int> {

        val res = MutableStateFlow(-1)
        viewModelScope.launch {
            res.value = repository.deleteSearchSetAsync(set).await()
        }
        return res
    }

    fun deleteSearchSetById(id: Long): StateFlow<Int> {

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
    var companies: LiveData<Array<Company>> = _companies

    private suspend fun getCompanies(): Array<Company>? = run {
        repository.getCompaniesFromDbAsync()
    }

    override fun onCleared() {
        repository.destructor()
        composite.apply {
            dispose()
            clear()
        }
        super.onCleared()
    }

    init {

        FireBaseConfig
        val isAgree = app?.appPref?.getBoolean(MainActivity.KEY_IS_AGREE, false) ?: true
        if (!isAgree) {

            val subscribe = repository.getAllCompaniesName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    if (list.isNotEmpty()) {
                        viewModelScope.launch {
                            repository.insertCompanies(list)
                            val companies = repository.getCompaniesFromDbAsync()
                            companies?.let { list ->
                                _companies.postValue(list)
                            }
                        }
                    }
                }, { t ->
                    if (BuildConfig.DEBUG) t.printStackTrace()
                })

            composite.add(subscribe)
        }
    }


}