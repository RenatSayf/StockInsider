package com.renatsayf.stockinsider.di.modules

import android.content.Context
import com.renatsayf.stockinsider.BuildConfig
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.network.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

@InstallIn(SingletonComponent::class)
@Module
object NetRepositoryModule {
    @Provides
    fun provideSearchRequest(api: IApi): INetRepository {
        return NetRepository(api)
    }

    @Provides
    fun api(@ApplicationContext context: Context): IApi {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC

        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(DocumAdapter.FACTORY)
            .baseUrl(context.getString(R.string.base_url))
            .client(okHttpClient).build()

        return when (BuildConfig.DATA_SOURCE) {
            "NETWORK" -> retrofit.create(IApi::class.java)
            else -> MockApi(context)
        }
    }
}