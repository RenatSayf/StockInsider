package com.renatsayf.stockinsider.utils

import com.renatsayf.stockinsider.BuildConfig

val START_TIME = if (BuildConfig.DEBUG) 10000L else 120000L
val TIME_INTERVAL = if (BuildConfig.DEBUG) 30000L else 60000L * 8