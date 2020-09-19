package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.donate.DonateFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent


@InstallIn(ApplicationComponent::class)
@Module
object DonateDialogModule
{
    @Provides
    fun provideDonateDialog() : DonateFragment
    {
        return DonateFragment.getInstance()
    }
}