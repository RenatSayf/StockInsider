package com.renatsayf.stockinsider.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.renatsayf.stockinsider.utils.printIfDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class HardwareButtonsReceiver : BroadcastReceiver() {

    private var _isHomeClicked = MutableSharedFlow<Result<Unit>>()
    val isHomeClicked: SharedFlow<Result<Unit>> = _isHomeClicked

    private var _isRecentClicked = MutableSharedFlow<Result<Unit>>()
    val isRecentClicked: SharedFlow<Result<Unit>> = _isRecentClicked

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context is Context && intent is Intent && intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {

            val async = goAsync()
            "************* ${HardwareButtonsReceiver::class.simpleName} has been triggered ***************".printIfDebug()
            val reason = intent.getStringExtra("reason")
            if (reason != null) {
                when (reason) {
                    "recentapps" -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            "********** Recent app button has been clicked *************".printIfDebug()
                            _isRecentClicked.emit(Result.success(Unit))
                            async.finish()
                        }
                    }
                    "homekey" -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            "********** Home button has been clicked *************".printIfDebug()
                            _isHomeClicked.emit(Result.success(Unit))
                            async.finish()
                        }
                    }
                }
            }
        }
    }
}