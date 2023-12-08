@file:Suppress("ObjectLiteralToLambda", "UnnecessaryVariable")

package com.renatsayf.stockinsider.firebase

import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.ui.settings.Constants


object FireBaseConfig {

    val userAgent: String
        get() {
            val value = Firebase.remoteConfig.getString("user_agent")
            return value.ifEmpty { "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.43 Mobile Safari/537.36" }
        }
    val trackingPeriod: Long
        get() {
            val value = if (!Constants.TEST_MODE) Firebase.remoteConfig.getLong("tracking_period") else Constants.TEST_TRACKING_PERIOD_IN_MINUTES
            return value
        }
    val requestsCount: Int
        get() {
            val value = Firebase.remoteConfig.getLong("requests_count").toInt()
            return if (value == 0 || BuildConfig.DEBUG) 3
            else value
        }
    val problemDevices: Array<String>
        get() {
            val value = Firebase.remoteConfig.getString("problem_devices")
            return if (value.isNotEmpty()) {
                val split = value.split(",")
                split.toTypedArray()
            }
            else arrayOf("XIAOMI","HUAWEI")
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

