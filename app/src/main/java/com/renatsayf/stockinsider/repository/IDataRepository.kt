package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Single
import kotlinx.coroutines.Deferred

interface IDataRepository
{
    fun getTradingScreenFromNetAsync(set: SearchSet) : io.reactivex.Observable<ArrayList<Deal>>

    fun getInsiderTradingFromNetAsync(insider: String) : Single<ArrayList<Deal>>

    fun getAllSearchSetsFromDbAsync() : List<RoomSearchSet>

    fun getTradingByTickerAsync(ticker: String) : Single<ArrayList<Deal>>

    fun getUserSearchSetsFromDbAsync() : List<RoomSearchSet>

    suspend fun getSearchSetFromDbAsync(setName: String) : RoomSearchSet

    suspend fun saveSearchSetAsync(set: RoomSearchSet) : Long

    fun deleteSearchSetAsync(set: RoomSearchSet) : Int

    fun getCompaniesFromDbAsync() : Array<Companies>
}