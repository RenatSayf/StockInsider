package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.ui.dialogs.ConfirmationDialog
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConfirmationDialogModule
{
    @Provides
    @Singleton
    fun provideConfirmationDialog() : ConfirmationDialog
    {
        return ConfirmationDialog("", "")
    }
}