package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object ConfirmationDialogModule
{
    @Provides
    fun provideConfirmationDialog() : ConfirmationDialog
    {
        return ConfirmationDialog("", "", "")
    }
}