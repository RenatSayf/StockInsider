package com.renatsayf.stockinsider.di

import android.app.Application
import com.renatsayf.stockinsider.di.modules.AppContextModule
import javax.inject.Inject

open class App @Inject constructor() : Application()
{
    val component: AppComponent by lazy {
        DaggerAppComponent.builder().appContextModule(AppContextModule(this)).build()
    }

}