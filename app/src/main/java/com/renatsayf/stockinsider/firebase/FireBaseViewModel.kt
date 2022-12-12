package com.renatsayf.stockinsider.firebase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.di.App
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FireBaseViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }

    init {
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
            val userAgent = get("user_agent").asString()
            App.userAgent = userAgent
        }
    }
}