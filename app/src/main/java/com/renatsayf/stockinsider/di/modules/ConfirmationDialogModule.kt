package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object ConfirmationDialogModule
{
    @Provides
    fun provideConfirmationDialog() : ConfirmationDialog
    {
        return ConfirmationDialog("", "", "")
    }
}