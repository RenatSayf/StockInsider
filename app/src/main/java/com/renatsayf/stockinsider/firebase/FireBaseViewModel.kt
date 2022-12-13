package com.renatsayf.stockinsider.firebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FireBaseViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    companion object {
        var userAgent = ""
        var workerPeriod = 480L
        var requestsCount = 5
    }

    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    init {
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
            userAgent = getString("user_agent")
            workerPeriod = if(BuildConfig.DEBUG) 15L else getLong("worker_period")
            requestsCount = getLong("requests_count").toInt()
        }
    }
}

