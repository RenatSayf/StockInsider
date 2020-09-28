package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Single
import kotlinx.coroutines.Job

interface IDataRepository
{
    fun getTradingScreenFromNetAsync(set: SearchSet) : io.reactivex.Observable<ArrayList<Deal>>

    fun getInsiderTradingFromNetAsync(insider: String) : Single<ArrayList<Deal>>

    fun getTradingByTickerAsync(ticker: String) : Single<ArrayList<Deal>>

    fun getSearchSetFromDbAsync(setName: String) : RoomSearchSet

    fun saveDefaultSearchAsync(set: RoomSearchSet)

    fun getCompaniesFromDbAsync() : Array<Companies>
}