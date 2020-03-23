package com.renatsayf.stockinsider.di

import com.renatsayf.stockinsider.MainActivity
import com.renatsayf.stockinsider.di.modules.*
import com.renatsayf.stockinsider.network.Scheduler
import com.renatsayf.stockinsider.service.GetFilingDataTask
import com.renatsayf.stockinsider.service.StockInsiderService
import com.renatsayf.stockinsider.ui.main.MainFragment
import com.renatsayf.stockinsider.ui.main.MainViewModel
import com.renatsayf.stockinsider.ui.result.ResultFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [AppContextModule::class, SearchRequestModule::class, RoomDataBaseModule::class, ScheduleReceiverModule::class, ConfirmationDialogModule::class, UtilsModule::class, NotificationModule::class]
          )
interface AppComponent
{
    fun inject(mainFragment : MainFragment)
    fun inject(mainViewModel : MainViewModel)
    fun inject(resultFragment : ResultFragment)
    fun inject(scheduler : Scheduler)
    fun inject(stockInsiderService : StockInsiderService)
    fun inject(getFilingDataTask : GetFilingDataTask)
    fun inject(mainActivity: MainActivity)
}