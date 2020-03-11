package com.renatsayf.stockinsider.di

import com.renatsayf.stockinsider.di.modules.*
import com.renatsayf.stockinsider.network.ScheduleReceiver
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
    fun inject(scheduleReceiver : ScheduleReceiver)
    fun inject(stockInsiderService : StockInsiderService)
}