package com.renatsayf.stockinsider.di.modules

import android.content.Context
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.utils.AppCalendar
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class AppCalendarModule @Inject constructor(private val context: Context)
{
    @Provides
    @Singleton
    fun provideAppCalendar() : AppCalendar
    {
        val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
        return AppCalendar(timeZone)
    }
}