package com.renatsayf.stockinsider.ui.settings

import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.Source

object Constants {
    val TEST_MODE: Boolean = BuildConfig.DATA_SOURCE == Source.TEST_TIME_PERIOD.name
    val TEST_TRACKING_PERIOD_IN_MINUTES: Long = FireBaseConfig.trackingPeriod
}

const val KEY_APP_STORE_LINK = "link"
const val KEY_IS_AD_DISABLED = "KEY_IS_AD_DISABLED"
const val PURCHASES_NOT_FOUND = "PURCHASES_NOT_FOUND"
const val BILLING_CLIENT_IS_NOT_READY = "BILLING_CLIENT_IS_NOT_READY"