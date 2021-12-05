package com.renatsayf.stockinsider.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.repository.DataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(private val repositoryImpl: DataRepositoryImpl) : ViewModel()
{
    sealed class State
    {
        object Initial: State()
        data class DataReceived(val deals: ArrayList<Deal>): State()
        data class DataError(val throwable: Throwable): State()
    }

    private val composite = CompositeDisposable()

    private var _state = MutableLiveData<State>().apply {
        value = State.Initial
    }
    val state: LiveData<State> = _state
    fun setState(state: State)
    {
        _state.value = state
    }

    //region Управление видимостью кнопки addAlarmImgView
    private val _addAlarmVisibility = MutableLiveData<Int>().apply {
        value = addAlarmVisibility?.value
    }
    val addAlarmVisibility: LiveData<Int> = _addAlarmVisibility
    fun setAddAlarmVisibility(visibility: Int)
    {
        _addAlarmVisibility.value = visibility
    }
    //endregion


    //region Управление видимостью кнопки alarmOnImgView
    private val _alarmOnVisibility = MutableLiveData<Int>().apply {
        value = alarmOnVisibility?.value
    }
    val alarmOnVisibility: LiveData<Int> = _alarmOnVisibility
    fun setAlarmOnVisibility(visibility: Int)
    {
        _alarmOnVisibility.value = visibility
    }
    //endregion

    //region Сохранение состояния заголовка ToolBar
    private val _toolBarTitle = MutableLiveData<String>().apply {
        value
    }

    fun setToolBarTitle(title: String)
    {
        _toolBarTitle.value = title
    }
    //endregion

    fun getDealList(set: SearchSet)
    {
        composite.add(
            repositoryImpl.getTradingScreenFromNetAsync(set)
                .subscribe({ list ->
                    _state.value = State.DataReceived(list)
                }, { t ->
                    _state.value = State.DataError(t)
                })
        )
    }

    override fun onCleared()
    {
        composite.apply {
            dispose()
            clear()
        }
        super.onCleared()
    }
}
