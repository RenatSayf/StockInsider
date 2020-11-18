package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
object AdModule
{
    @Provides
    fun provideAd(@ApplicationContext context: Context) : InterstitialAd
    {
        MobileAds.initialize(context)
        return InterstitialAd(context).apply {
            adUnitId = if (BuildConfig.DEBUG)
            {
                context.getString(R.string.test_interstitial_ads_id)
            }
            else
            {
                context.getString(R.string.full_screen_ad_2)
            }
            loadAd(AdRequest.Builder().build())
        }
    }
}