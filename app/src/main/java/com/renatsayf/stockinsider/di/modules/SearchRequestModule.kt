package com.renatsayf.stockinsider.di.modules

import com.renatsayf.stockinsider.network.DocumAdapter
import com.renatsayf.stockinsider.network.IApi
import com.renatsayf.stockinsider.network.ISearchRequest
import com.renatsayf.stockinsider.network.SearchRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

@InstallIn(SingletonComponent::class)
@Module
object SearchRequestModule
{
    @Provides
    fun provideSearchRequest(api: IApi): ISearchRequest
    {
        return SearchRequest(api)
    }

    @Provides
    fun api(): IApi {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC

        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build()

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(DocumAdapter.FACTORY)
            .baseUrl("http://openinsider.com/")
            .client(okHttpClient).build()

        return retrofit.create(IApi::class.java)
    }
}