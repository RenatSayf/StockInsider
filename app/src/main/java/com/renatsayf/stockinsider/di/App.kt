package com.renatsayf.stockinsider.di

import android.app.Application

open class App : Application()
{
    val component: AppComponent by lazy {
        DaggerAppComponent.create()
    }

}