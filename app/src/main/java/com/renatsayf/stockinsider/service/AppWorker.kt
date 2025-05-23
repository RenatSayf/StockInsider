@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.renatsayf.stockinsider.service

import android.content.Context
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.di.modules.NetRepositoryModule
import com.renatsayf.stockinsider.di.modules.RoomDataBaseModule
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.service.notifications.RequestNotification
import com.renatsayf.stockinsider.service.notifications.ServiceNotification
import com.renatsayf.stockinsider.ui.settings.Constants
import com.renatsayf.stockinsider.utils.AppCalendar
import com.renatsayf.stockinsider.utils.LOGS_FILE_NAME
import com.renatsayf.stockinsider.utils.appendTextToFile
import com.renatsayf.stockinsider.utils.createTextFile
import com.renatsayf.stockinsider.utils.getNextStartTime
import com.renatsayf.stockinsider.utils.isLogsFileExists
import com.renatsayf.stockinsider.utils.printIfDebug
import com.renatsayf.stockinsider.utils.reduceFileSize
import com.renatsayf.stockinsider.utils.timeToFormattedString
import com.renatsayf.stockinsider.utils.timeToFormattedStringWithoutSeconds
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay


class AppWorker (
    private val context: Context,
    parameters: WorkerParameters
): CoroutineWorker(context, parameters) {

    companion object {
        val TAG = this::class.java.simpleName.plus(".Tag")
        val SEARCH_SET_KEY = this::class.java.simpleName.plus(".SearchSetKey")

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var state: State = State.Initial
        private set

        private lateinit var _db: AppDao
        private lateinit var _net: INetRepository
        private var _searchSets: List<RoomSearchSet>? = null
        private var _function: ((Context, String, RoomSearchSet) -> Unit)? = ServiceNotification.notify
        private var _trackingPeriodInMinutes: Long = FireBaseConfig.trackingPeriod
        private var _isTestMode: Boolean = Constants.TEST_MODE

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun injectDependenciesToTest(
            searchSets: List<RoomSearchSet>? = null,
            trackingPeriodInMinutes: Long,
            isTestMode: Boolean
        ) {
            this._searchSets = searchSets
            this._trackingPeriodInMinutes = trackingPeriodInMinutes
            this._isTestMode = isTestMode
        }
    }

    init {
        _db = RoomDataBaseModule.provideRoomDataBase(context)
        _net = NetRepositoryModule.provideNetRepository(context, NetRepositoryModule.provideApi(context))
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {

        val notification = RequestNotification(context).create()
        return ForegroundInfo(RequestNotification.NOTIFY_ID, notification!!)
    }

    override suspend fun doWork(): Result
    {
        state = State.Started
        return try
        {
            val startMessage = "${System.currentTimeMillis().timeToFormattedString()} ->> The search has been started **********"
            startMessage.printIfDebug()
            writeLogToFile(startMessage)

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                setForeground(getForegroundInfo())
            }

            if (_searchSets == null) _searchSets = getTrackingSetsAsync().await()
            val array = _searchSets?.map { set ->
                val duration = (10..20).random()
                delay(duration * 1000L)
                val params = set.toSearchSet()
                val deals = _net.getDealsListAsync(params).await()

                val nextFillingTime = AppCalendar().getNextStartTime(_trackingPeriodInMinutes, _isTestMode).timeToFormattedStringWithoutSeconds()
                val message = context.getString(com.renatsayf.stockinsider.R.string.text_by_search) +
                            " ${set.queryName}, ${deals.size} ${context.getString(com.renatsayf.stockinsider.R.string.text_results_were_found)}\n" +
                            "${context.getString(com.renatsayf.stockinsider.R.string.text_next_check_will_be_at)} $nextFillingTime"
                when {
                    BuildConfig.DEBUG -> {
                        _function?.invoke(context, message, set)
                    }
                    else -> {
                        if (deals.isNotEmpty()) {
                            _function?.invoke(context, message, set)
                        }
                    }
                }
                context.appendTextToFile(LOGS_FILE_NAME, "${System.currentTimeMillis().timeToFormattedString()} ->> $message")
                deals.isNotEmpty()
            }?.toBooleanArray()?: booleanArrayOf()

            val data = Data.Builder().apply {
                putBooleanArray("result", array)
            }.build()

            val successMessage = "${System.currentTimeMillis().timeToFormattedString()} ->> The search was completed successfully."
            successMessage.printIfDebug()
            context.appendTextToFile(LOGS_FILE_NAME, successMessage)

            state = State.Completed
            Result.success(data)
        }
        catch (e: Exception)
        {
            if (BuildConfig.DEBUG) e.printStackTrace()
            val errorMessage = e.message ?: "********** Unknown error **********"
            val errorData = Data.Builder().apply {
                putString("error", "*********** $errorMessage *************")
            }.build()

            context.appendTextToFile(
                LOGS_FILE_NAME,
                content = "${System.currentTimeMillis().timeToFormattedString()} ->> The search was completed with an error: $errorMessage"
            )
            "********************** ${this.javaClass.simpleName}: catch block - Background work failed *****************************".printIfDebug()

            state = State.Failed
            Result.failure(errorData)
        }
    }

    private suspend fun getTrackingSetsAsync() : Deferred<List<RoomSearchSet>?> = coroutineScope {
        async {
            _db.getTrackedSets(target = Target.Tracking, isTracked = 1)
        }
    }

    sealed class State {
        object Initial: State()
        object Started: State()
        object Completed: State()
        object Failed: State()
    }

    private fun writeLogToFile(message: String) {
        val fileName = LOGS_FILE_NAME
        val time = System.currentTimeMillis().timeToFormattedString()
        val newString = "$time ->> $message\n"
        context.isLogsFileExists(
            fileName,
            onExists = { file ->
                if (file.length() >= 1024 * 100L) {
                    context.reduceFileSize(fileName, 1024 * 20L)
                }
                context.appendTextToFile(fileName, newString)
            },
            onNotExists = {
                context.createTextFile(fileName, "******************************")
                context.appendTextToFile(fileName, newString)
            }
        )
    }

}

