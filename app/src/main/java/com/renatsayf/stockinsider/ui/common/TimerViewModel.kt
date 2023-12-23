package com.renatsayf.stockinsider.ui.common

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    private val millisInFuture: Long = if (BuildConfig.DEBUG) TimeUnit.SECONDS.toMillis(30) else
        TimeUnit.SECONDS.toMillis(180)

    private var job: Job? = null

    private val _timeIsUp = MutableSharedFlow<Result<Unit>>()
    val timeIsUp: SharedFlow<Result<Unit>> = _timeIsUp

    private val timer = object : CountDownTimer(millisInFuture, millisInFuture) {
        override fun onTick(p0: Long) {}

        override fun onFinish() {
            job = CoroutineScope(Dispatchers.Main).launch {
                _timeIsUp.emit(Result.success(Unit))
            }
        }
    }

    fun startTimer() {
        timer.start()
    }

    override fun onCleared() {

        job?.cancel()
        super.onCleared()
    }


}