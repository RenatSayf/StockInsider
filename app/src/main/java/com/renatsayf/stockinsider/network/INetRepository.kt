package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import kotlinx.coroutines.Deferred

interface INetRepository {
    suspend fun getTradingListAsync(set: SearchSet): Deferred<Result<List<Deal>>>

    suspend fun getInsiderTradingAsync(insider: String): Deferred<Result<List<Deal>>>

    suspend fun getDealsByTicker(ticker: String): Deferred<Result<List<Deal>>>

    suspend fun getAllCompaniesNameAsync(): Deferred<Result<List<Company>>>

    suspend fun getDealsListAsync(set: SearchSet) : Deferred<List<Deal>>
}