package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Deferred

interface INetRepository {
    suspend fun getTradingListAsync(set: SearchSet): Deferred<ArrayList<Deal>>

    fun getInsiderTrading(insider: String): Single<ArrayList<Deal>>
    suspend fun getInsiderTradingAsync(insider: String): Deferred<Result<List<Deal>>>

    fun getTradingByTicker(ticker: String): Single<ArrayList<Deal>>

    fun getAllCompaniesName(): Single<List<Company>>

    suspend fun getDealsListAsync(set: SearchSet) : Deferred<List<Deal>>
}