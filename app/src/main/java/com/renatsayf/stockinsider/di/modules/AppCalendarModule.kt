package com.renatsayf.stockinsider.di.modules

import android.content.Context
import android.icu.util.TimeZone
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.utils.AppCalendar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object AppCalendarModule
{
    @Provides
    @Singleton
    fun provideAppCalendar(@ApplicationContext context: Context) : AppCalendar
    {
        val timeZone = TimeZone.getTimeZone(context.getString(R.string.app_time_zone))
        return AppCalendar(timeZone)
    }
}