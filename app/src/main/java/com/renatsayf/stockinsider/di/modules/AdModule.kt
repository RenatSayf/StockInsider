package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object AdModule
{
    @Provides
    fun provideAd(@ApplicationContext context: Context) : InterstitialAd
    {
        MobileAds.initialize(context)
        return InterstitialAd(context)
    }
}