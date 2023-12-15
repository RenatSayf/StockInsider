package com.renatsayf.stockinsider.service.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.renatsayf.stockinsider.utils.printIfDebug
import com.renatsayf.stockinsider.utils.printStackTraceIfDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.system.exitProcess

class LocationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        val TAG = "${LocationWorker::class.simpleName}.5624885549"
        private val NAME = "${LocationWorker::class.simpleName}.SSDFDFJHDHD"

        private val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
        }.build()

        fun doLocationScan(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<LocationWorker>().apply {
                setConstraints(constraints)
                addTag(TAG)
            }.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }

        private var _countryCode = MutableSharedFlow<kotlin.Result<String>>()
        val countryCode: SharedFlow<kotlin.Result<String>> = _countryCode
    }

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)


    override suspend fun doWork(): Result {

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            CoroutineScope(Dispatchers.Default).launch {
                _countryCode.emit(value = kotlin.Result.success(Locale.getDefault().country))
            }
            return Result.failure()
        }
        locationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val addresses =
                        Geocoder(applicationContext, Locale.getDefault()).getFromLocation(
                            latitude,
                            longitude,
                            1
                        )
                    val countryCode = addresses?.firstOrNull()?.countryCode?: Locale.getDefault().country
                    "********** ${LocationWorker::class.simpleName} countryCode == $countryCode ***************".printIfDebug()
                    CoroutineScope(Dispatchers.Default).launch {
                        _countryCode.emit(value = kotlin.Result.success(countryCode))
                    }
                }?: run {
                    "********** ${LocationWorker::class.simpleName} location == NULL,  Country by default = ${Locale.getDefault().country}***************".printIfDebug()
                    CoroutineScope(Dispatchers.Default).launch {
                        _countryCode.emit(value = kotlin.Result.success(Locale.getDefault().country))
                    }
                }
            }
            .addOnFailureListener { ex ->
                ex.printStackTraceIfDebug()
                Result.failure()
            }

        return Result.success()
    }
}