package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Single

interface IDataRepository
{
    fun getTradingScreenFromNetAsync(set: SearchSet) : io.reactivex.Observable<ArrayList<Deal>>

    fun getInsiderTradingFromNetAsync(insider: String) : Single<ArrayList<Deal>>

    suspend fun getAllSearchSetsFromDbAsync() : List<RoomSearchSet>

    fun getTradingByTickerAsync(ticker: String) : Single<ArrayList<Deal>>

    suspend fun getUserSearchSetsFromDbAsync() : List<RoomSearchSet>

    suspend fun getSearchSetFromDbAsync(setName: String) : RoomSearchSet

    suspend fun saveSearchSetAsync(set: RoomSearchSet) : Long

    suspend fun deleteSearchSetAsync(set: RoomSearchSet) : Int

    suspend fun getCompaniesFromDbAsync() : Array<Companies>
}