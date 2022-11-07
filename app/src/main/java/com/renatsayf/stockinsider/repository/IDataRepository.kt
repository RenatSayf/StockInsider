package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import io.reactivex.Observable
import io.reactivex.Single
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

    suspend fun getSearchSetByIdAsync(id: Long) : Deferred<RoomSearchSet?>

    suspend fun saveSearchSetAsync(set: RoomSearchSet) : Deferred<Long>

    suspend fun deleteSearchSetAsync(set: RoomSearchSet) : Deferred<Int>

    suspend fun getCompaniesFromDbAsync() : Array<Company>?

    suspend fun getSearchSetsByTarget(target: String) : List<RoomSearchSet>

    suspend fun getCompanyByTicker(list: List<String>) : List<Company>

    suspend fun getAllSimilar(pattern: String) : List<Company>

    @Throws(Exception::class)
    suspend fun insertCompanies(list : List<Company>)

    suspend fun updateSearchSetTickerAsync(setName: String, value: String) : Deferred<Int>

    fun destructor()
}