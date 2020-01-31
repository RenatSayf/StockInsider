package com.renatsayf.stockinsider.di

import com.renatsayf.stockinsider.di.modules.SearchRequestModule
import com.renatsayf.stockinsider.ui.home.HomeFragment
import com.renatsayf.stockinsider.ui.result.ResultFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [SearchRequestModule::class])
interface AppComponent
{
    fun inject(homeFragment : HomeFragment)
    fun inject(resultFragment : ResultFragment)
}