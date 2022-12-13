package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.network.NetRepository
import com.renatsayf.stockinsider.utils.sortByAnotherList
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.*
import javax.inject.Inject

open class DataRepositoryImpl @Inject constructor(private val network: INetRepository, private val db: AppDao) : IDataRepository
{
    override fun getTradingScreenFromNetAsync(set: SearchSet): Observable<ArrayList<Deal>>
    {
        return network.getTradingScreen(set)
    }

    override fun getInsiderTradingFromNetAsync(insider: String): Single<ArrayList<Deal>>
    {
        return network.getInsiderTrading(insider)
    }

    override fun getAllCompaniesName(): Single<List<Company>> {
        return network.getAllCompaniesName()
    }

    override suspend fun getAllSearchSetsFromDbAsync(): List<RoomSearchSet> = coroutineScope {
        withContext(Dispatchers.Main) {
            db.getSearchSets()
        }
    }

    override fun getTradingByTickerAsync(ticker: String): Single<ArrayList<Deal>>
    {
        return network.getTradingByTicker(ticker)
    }

    override suspend fun getUserSearchSetsFromDbAsync(): List<RoomSearchSet> = coroutineScope {
        withContext(Dispatchers.Default) {
            db.getUserSearchSets()
        }
    }

    override suspend fun getSearchSetFromDbAsync(setName: String): RoomSearchSet = coroutineScope {
        withContext(Dispatchers.Default) {
            db.getSetByName(setName)
        }
    }

    override suspend fun getSearchSetByIdAsync(id: Long): Deferred<RoomSearchSet?> {
        return coroutineScope {
            async {
                db.getSetById(id)
            }
        }
    }

    override suspend fun saveSearchSetAsync(set: RoomSearchSet): Deferred<Long> = coroutineScope {
        async {
            db.insertOrUpdateSearchSet(set)
        }
    }

    override suspend fun deleteSearchSetAsync(set: RoomSearchSet): Deferred<Int> = coroutineScope {
        async {
            db.deleteSet(set)
        }
    }

    override suspend fun deleteSetByIdAsync(id: Long): Deferred<Int> {
        return coroutineScope {
            async {
                db.deleteSetById(id)
            }
        }
    }

    override suspend fun getCompaniesFromDbAsync(): Array<Company>? = coroutineScope {
        withContext(Dispatchers.Default) {
            db.getAllCompanies()?.toTypedArray()
        }
    }

    override suspend fun getSearchSetsByTarget(target: String): List<RoomSearchSet> = coroutineScope {
        withContext(Dispatchers.Default) {
            db.getSearchSetsByTarget(target)
        }
    }

    override suspend fun getTrackedCountAsync(): Deferred<Int> {
        return coroutineScope {
            async {
                db.getTrackedCount()
            }
        }
    }

    override suspend fun getTargetCountAsync(): Deferred<Int> {
        return coroutineScope {
            async {
                db.getTargetCount()
            }
        }
    }

    override suspend fun getCompanyByTicker(list: List<String>): List<Company> = coroutineScope {
        withContext(Dispatchers.Default) {
            val companyByTicker = db.getCompanyByTicker(list)
            val sortByAnotherList = sortByAnotherList(companyByTicker, list)
            sortByAnotherList
        }
    }

    override suspend fun getAllSimilar(pattern: String): List<Company> = coroutineScope {
        async {
            db.getAllSimilar("%$pattern%")
        }
    }.await()

    @Throws(Exception::class)
    override suspend fun insertCompanies(list: List<Company>) {
        CoroutineScope(Dispatchers.IO).launch {
            db.insertCompanies(list)
        }
    }

    override suspend fun updateSearchSetTickerAsync(setName: String, value: String): Deferred<Int> {
        return coroutineScope {
            async {
                db.updateSearchSetTicker(setName, value)
            }
        }
    }

    override fun destructor()
    {
        if (network is NetRepository) {
            network.composite.apply {
                dispose()
                clear()
            }
        }
    }

}

