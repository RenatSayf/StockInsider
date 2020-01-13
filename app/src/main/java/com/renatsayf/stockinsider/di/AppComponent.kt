package com.renatsayf.stockinsider.di

import com.renatsayf.stockinsider.models.SearchRequest
import com.renatsayf.stockinsider.ui.home.HomeViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [SearchRequest::class])
interface AppComponent
{
    fun inject(homeViewModel : HomeViewModel)
}