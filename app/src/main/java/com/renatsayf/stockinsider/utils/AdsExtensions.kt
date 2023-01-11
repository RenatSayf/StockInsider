package com.renatsayf.stockinsider.utils

import android.app.Application
import java.util.*


fun Application.getCurrentLocale(): Locale {

    return this.resources.configuration.locales[0]
}

val Application.currentCountryCode: String
    get() {
        return getCurrentLocale().country
    }


























