package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Observable
import io.reactivex.Single

class FakeNetRepository : INetRepository {

    private var result: Any? = null

    fun<T> setExpectedResult(result: T) {
        this.result = result
    }

    override fun getTradingScreen(set: SearchSet): Observable<ArrayList<Deal>> {
        return result as Observable<ArrayList<Deal>>
    }

    override fun getInsiderTrading(insider: String): Single<ArrayList<Deal>> {
        return Single.just(ArrayList())
    }

    override fun getTradingByTicker(ticker: String): Single<ArrayList<Deal>> {
        return Single.just(ArrayList())
    }

    override fun getAllCompaniesName(): Single<List<Company>> {
        return Single.just(listOf())
    }
}