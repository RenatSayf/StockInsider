package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.NetworkRepository
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.*
import javax.inject.Inject
import com.renatsayf.stockinsider.models.Target
import com.renatsayf.stockinsider.network.INetworkRepository
import java.util.*
import kotlin.collections.ArrayList

class DataRepositoryImpl @Inject constructor(private val network: INetworkRepository, private val db: AppDao) : IDataRepository
{
    override fun getTradingScreenFromNetAsync(set: SearchSet): Observable<ArrayList<Deal>>
    {
        return network.getTradingScreen(set)
    }

    override fun getInsiderTradingFromNetAsync(insider: String): Single<ArrayList<Deal>>
    {
        return network.getInsiderTrading(insider)
    }

    override suspend fun getAllSearchSetsFromDbAsync(): List<RoomSearchSet> = CoroutineScope(Dispatchers.IO).run {
        withContext(Dispatchers.Main) {
            db.getSearchSets()
        }
    }

    override fun getTradingByTickerAsync(ticker: String): Single<ArrayList<Deal>>
    {
        return network.getTradingByTicker(ticker)
    }

    override suspend fun getUserSearchSetsFromDbAsync(): List<RoomSearchSet> = CoroutineScope(Dispatchers.IO).run {
        val res = async {
            db.getUserSearchSets()
        }
        res.await()
    }

    override suspend fun getSearchSetFromDbAsync(setName: String): RoomSearchSet = CoroutineScope(Dispatchers.IO).run {
        val set = async {
            db.getSetByName(setName)
        }
        set.await()
    }

    override suspend fun saveSearchSetAsync(set: RoomSearchSet): Long = CoroutineScope(Dispatchers.IO).run {
        return withContext(Dispatchers.Main) {
            db.insertOrUpdateSearchSet(set)
        }
    }

    override suspend fun deleteSearchSetAsync(set: RoomSearchSet): Int = CoroutineScope(Dispatchers.IO).run {
        return withContext(Dispatchers.Main){
            db.deleteSet(set)
        }
    }

    override suspend fun getCompaniesFromDbAsync(): Array<Companies>? = CoroutineScope(Dispatchers.IO).run {
        val companies = async {
            db.getAllCompanies()?.toTypedArray()
        }
        companies.await()
    }

    override suspend fun getSearchSetsByTarget(target: Target): List<RoomSearchSet> = CoroutineScope(Dispatchers.IO).run {
        return withContext(Dispatchers.Main) {
            db.getSearchSetsByTarget(target.name.lowercase(Locale.getDefault()))
        }
    }

    override suspend fun getCompanyByTicker(list: List<String>): List<Companies> = CoroutineScope(Dispatchers.IO).run {
        withContext(Dispatchers.Main) {
            db.getCompanyByTicker(list)
        }
    }

    override suspend fun getAllSimilar(pattern: String): List<Companies> = CoroutineScope(Dispatchers.IO).run {
        withContext(Dispatchers.Main) {
            db.getAllSimilar(pattern)
        }
    }

    override fun destructor()
    {
        if (network is NetworkRepository) {
            network.composite.clear()
        }
    }
}