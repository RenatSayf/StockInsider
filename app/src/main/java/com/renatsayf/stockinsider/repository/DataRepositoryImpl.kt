package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.*
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(private val request: SearchRequest, private val db: AppDao) : IDataRepository
{
    override fun getTradingScreenFromNetAsync(set: SearchSet): Observable<ArrayList<Deal>>
    {
        return request.getTradingScreen(set)
    }

    override fun getInsiderTradingFromNetAsync(insider: String): Single<ArrayList<Deal>>
    {
        return request.getInsiderTrading(insider)
    }

    override fun getAllSearchSetsFromDbAsync(): List<RoomSearchSet> = runBlocking {
        val res = async {
            db.getSearchSets()
        }
        res.await()
    }

    override fun getTradingByTickerAsync(ticker: String): Single<ArrayList<Deal>>
    {
        return request.getTradingByTicker(ticker)
    }

    override fun getUserSearchSetsFromDbAsync(): List<RoomSearchSet> = runBlocking {
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

    override fun saveSearchSetAsync(set: RoomSearchSet): Long = runBlocking {
        val res = async {
            db.insertOrUpdateSearchSet(set)
        }
        res.await()
    }

    override fun deleteSearchSetAsync(set: RoomSearchSet): Int = runBlocking {
        val res = async {
            db.deleteSet(set)
        }
        res.await()
    }



    override fun getCompaniesFromDbAsync(): Array<Companies> = runBlocking{
        val companies = async {
            db.getAllCompanies().toTypedArray()
        }
        companies.await()
    }
}