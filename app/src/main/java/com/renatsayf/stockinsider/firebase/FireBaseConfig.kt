@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.firebase

import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Source


object FireBaseConfig {

    var userAgent = Firebase.remoteConfig.getString("user_agent")
    var workerPeriod = if(BuildConfig.DEBUG && BuildConfig.DATA_SOURCE == Source.LOCALHOST.name) 60L
    else Firebase.remoteConfig.getLong("worker_period")
    var requestsCount = if(BuildConfig.DEBUG) 3 else Firebase.remoteConfig.getLong("requests_count").toInt()

    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    init {
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate().addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    if (BuildConfig.DEBUG) e.printStackTrace()
                }
            })
        }
    }
}

