package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.firebase.FireBaseViewModel
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Observable
import io.reactivex.Single

interface INetRepository {
    fun getTradingScreen(set: SearchSet, userAgent: String = FireBaseViewModel.userAgent) : Observable<ArrayList<Deal>>

    fun getInsiderTrading(insider: String, userAgent: String = FireBaseViewModel.userAgent): Single<ArrayList<Deal>>

    fun getTradingByTicker(ticker: String, userAgent: String = FireBaseViewModel.userAgent): Single<ArrayList<Deal>>

    fun getAllCompaniesName(): Single<List<Company>>
}