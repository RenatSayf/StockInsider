package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Observable
import io.reactivex.Single

interface INetworkRepository {
    fun getTradingScreen(set: SearchSet) : Observable<ArrayList<Deal>>

    fun getInsiderTrading(insider: String): Single<ArrayList<Deal>>

    fun getTradingByTicker(ticker: String): Single<ArrayList<Deal>>
}