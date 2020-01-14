package com.renatsayf.stockinsider.network

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenInsiderService
{
    @GET("screener")
    fun getInsiderTrading(
                @Query("s") ticker: String,
                @Query("fd") filingDate: String,
                @Query("td") tradeDate: String,
                @Query("xp") isPurchase: String,
                @Query("xs") isSale: String,
                @Query("vl") tradedMin: String,
                @Query("vh") tradedMax: String,
                @Query("isofficer") isOfficer: String,
                @Query("iscob") isCob : String = isOfficer,
                @Query("isceo") isSeo: String = isOfficer,
                @Query("ispres") isPres: String = isOfficer,
                @Query("iscoo") isCoo: String = isOfficer,
                @Query("iscfo") isCfo: String = isOfficer,
                @Query("isgc") isGc: String = isOfficer,
                @Query("isvp") isVp: String = isOfficer,
                @Query("isdirector") isDirector: String,
                @Query("istenpercent") isTenPercent: String,
                @Query("grp") groupBy: String,
                @Query("sortcol") sortBy: String
                         ): Deferred<String>

//    companion object
//    {
//        operator fun invoke(
//                interceptor: Interceptor
//                           ) : OpenInsiderService
//        {
//            val requestInterceptor = Interceptor { chain ->
//                val url = chain.request().url().newBuilder().build()
//                val request = chain.request().newBuilder().url(url).build()
//                return@Interceptor chain.proceed(request)
//            }
//
//            val okHttpClient = OkHttpClient.Builder().addInterceptor(requestInterceptor)
//                .addInterceptor(interceptor).build()
//            return Retrofit.Builder().client(okHttpClient)
//                .baseUrl("http://openinsider.com/")
//                .addCallAdapterFactory(CoroutineCallAdapterFactory())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//                .create(OpenInsiderService::class.java)
//        }
//    }
}