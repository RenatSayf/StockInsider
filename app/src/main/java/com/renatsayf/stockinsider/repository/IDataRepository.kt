package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import kotlinx.coroutines.Deferred

interface IDataRepository
{
    suspend fun getTradingListFromNetAsync(set: SearchSet): Deferred<Result<List<Deal>>>

    suspend fun getInsiderTradingFromNetAsync2(insider: String): Deferred<Result<List<Deal>>>

    suspend fun getAllCompanies(): Deferred<Result<List<Company>>>

    suspend fun getAllSearchSetsFromDbAsync() : List<RoomSearchSet>

    suspend fun getDealsByTicker(ticker: String): Deferred<Result<List<Deal>>>

    suspend fun getUserSearchSetsFromDbAsync() : List<RoomSearchSet>

    suspend fun getSearchSetFromDbAsync(setName: String) : RoomSearchSet

    suspend fun getSearchSetByIdAsync(id: Long) : Deferred<RoomSearchSet?>

    suspend fun saveSearchSetAsync(set: RoomSearchSet) : Deferred<Long>

    suspend fun addNewSearchSetAsync(set: RoomSearchSet) : Deferred<Long>

    suspend fun deleteSearchSetAsync(set: RoomSearchSet) : Deferred<Int>

    suspend fun deleteSetByIdAsync(id: Long) : Deferred<Int>

    suspend fun getCompaniesFromDbAsync() : Array<Company>?

    suspend fun getSearchSetsByTarget(target: String) : List<RoomSearchSet>

    suspend fun getTrackedCountAsync() : Deferred<Int>

    fun getTrackedCountSync(): Int

    suspend fun getTargetCountAsync() : Deferred<Int>

    suspend fun getCompanyByTicker(list: List<String>) : List<Company>

    suspend fun getAllSimilar(pattern: String) : List<Company>

    @Throws(Exception::class)
    suspend fun insertCompanies(list : List<Company>)

    suspend fun updateSearchSetTickerAsync(setName: String, value: String) : Deferred<Int>
}