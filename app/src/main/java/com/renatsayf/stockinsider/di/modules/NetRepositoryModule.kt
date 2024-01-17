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
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
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
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

        val okHttpClient = OkHttpClient().newBuilder()
            .cache(Cache(File(context.cacheDir, "insider-cache"), 5L * 1024L * 1024L))
            .addNetworkInterceptor(CacheInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(DocumAdapter.FACTORY)
            .addConverterFactory(StringAdapter.FACTORY)
            .baseUrl(context.getString(R.string.base_url))
            .client(okHttpClient).build()

        return when (BuildConfig.DATA_SOURCE) {
            "NETWORK" -> retrofit.create(IApi::class.java)
            else -> MockApi(context)
        }
    }

    class CacheInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response: Response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(30, TimeUnit.MINUTES)
                .build()
            return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
    }

}