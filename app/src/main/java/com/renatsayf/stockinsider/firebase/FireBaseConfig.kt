@file:Suppress("ObjectLiteralToLambda")

package com.renatsayf.stockinsider.firebase

import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.models.Source


object FireBaseConfig {

    val userAgent: String
        get() {
            val value = Firebase.remoteConfig.getString("user_agent")
            return value.ifEmpty { "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36" }
        }
    val workerPeriod: Long
        get() {
            val value = Firebase.remoteConfig.getLong("worker_period")
            return if (value == 0L || (BuildConfig.DEBUG && BuildConfig.DATA_SOURCE == Source.LOCALHOST.name)) 60L
            else value
        }
    val requestsCount: Int
        get() {
            val value = Firebase.remoteConfig.getLong("requests_count").toInt()
            return if (value == 0 || BuildConfig.DEBUG) 3
            else value
        }
    val sanctionsArray: Array<String>
        get() {
            val value = Firebase.remoteConfig.getString("sanctions_list")
            return if (value.isNotEmpty()) {
                Gson().fromJson(value, Array<String>::class.java)
            }
            else arrayOf("RU","BY","IR","CU","KP","SY")
        }

    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    init {
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        if (BuildConfig.DEBUG) e.printStackTrace()
                    }
                })
        }
    }
}

