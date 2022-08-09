package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Observable
import io.reactivex.Single
import com.renatsayf.stockinsider.models.Target
import kotlinx.coroutines.Deferred
import java.lang.Exception
import kotlin.jvm.Throws

interface IDataRepository
{
    fun getTradingScreenFromNetAsync(set: SearchSet) : Observable<ArrayList<Deal>>

    fun getInsiderTradingFromNetAsync(insider: String) : Single<ArrayList<Deal>>

    suspend fun getAllSearchSetsFromDbAsync() : List<RoomSearchSet>

    fun getTradingByTickerAsync(ticker: String) : Single<ArrayList<Deal>>

    suspend fun getUserSearchSetsFromDbAsync() : List<RoomSearchSet>

    suspend fun getSearchSetFromDbAsync(setName: String) : RoomSearchSet

    suspend fun saveSearchSetAsync(set: RoomSearchSet) : Deferred<Long>

    suspend fun deleteSearchSetAsync(set: RoomSearchSet) : Int

    suspend fun getCompaniesFromDbAsync() : Array<Companies>?

    suspend fun getSearchSetsByTarget(target: String) : List<RoomSearchSet>

    suspend fun getCompanyByTicker(list: List<String>) : List<Companies>

    suspend fun getAllSimilar(pattern: String) : List<Companies>

    @Throws(Exception::class)
    suspend fun insertCompanies(list : List<Companies>)

    fun updateSearchSetTicker(id: Int, value: String) : Int

    fun destructor()
}