@file:Suppress("ObjectLiteralToLambda", "UnnecessaryVariable")

package com.renatsayf.stockinsider.firebase

import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.ui.settings.Constants
import com.renatsayf.stockinsider.utils.printStackTraceIfDebug
import kotlinx.serialization.json.Json


object FireBaseConfig {

    val userAgent: String
        get() {
            val value = Firebase.remoteConfig.getString("user_agent")
            return value.ifEmpty { "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.43 Mobile Safari/537.36" }
        }
    val trackingPeriod: Long
        get() {
            return try {
                if (!Constants.TEST_MODE) Firebase.remoteConfig.getLong("tracking_period") else Constants.TEST_TRACKING_PERIOD_IN_MINUTES
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                240
            }
        }
    val requestsCount: Int
        get() {
            return try {
                if (!BuildConfig.DEBUG) Firebase.remoteConfig.getLong("requests_count").toInt() else 3
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                5
            }
        }
    val problemDevices: Array<String>
        get() {
            val value = try {
                Firebase.remoteConfig.getString("problem_devices")
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                "XIAOMI,HUAWEI"
            }
            val split = value.split(",").map { it.trim() }
            return split.toTypedArray()
        }

    val sanctionsList: Array<String>
        get() {
            val value = try {
                val string = Firebase.remoteConfig.getString("sanctions_list")
                val array = Json.decodeFromString<Array<String>>(string)
                array
            }
            catch (e: Exception) {
                arrayOf("RU","BY","IR","CU","KP","SY","CN")
            }
            return value
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

