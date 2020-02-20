package com.renatsayf.stockinsider.di

import android.app.Application
import javax.inject.Inject

open class App @Inject constructor() : Application()
{
    val component: AppComponent by lazy {
        DaggerAppComponent.create()
    }

}