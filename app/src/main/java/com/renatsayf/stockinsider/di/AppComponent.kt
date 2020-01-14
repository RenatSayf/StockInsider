package com.renatsayf.stockinsider.di

import com.renatsayf.stockinsider.di.modules.SearchRequestModule
import com.renatsayf.stockinsider.ui.home.HomeViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [SearchRequestModule::class])
interface AppComponent
{
    fun inject(homeViewModel : HomeViewModel)
}