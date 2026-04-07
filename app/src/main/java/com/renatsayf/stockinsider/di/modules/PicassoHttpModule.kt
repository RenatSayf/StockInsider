package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.firebase.FireBaseConfig
import okhttp3.OkHttpClient

object PicassoHttpModule {

    private val userAgent = FireBaseConfig.userAgent

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("User-Agent", userAgent)
                .build()
            chain.proceed(newRequest)
        }
        .build()
}