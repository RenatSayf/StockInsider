package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Companies
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import io.reactivex.Observable
import kotlinx.coroutines.*
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(private val request: SearchRequest, private val db: AppDao) : IDataRepository
{
    override fun getDealListFromNet(set: SearchSet): Observable<ArrayList<Deal>>
    {
        return request.getTradingScreen(set)
    }

    override fun getSearchSetFromDbAsync(setName: String): RoomSearchSet = runBlocking {
        val set = async {
            db.getSetByName(setName)
        }
        set.await()
    }

    override fun saveDefaultSearchAsync(set: RoomSearchSet)
    {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try
                {
                    db.insertOrUpdateSearchSet(set)
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getCompaniesFromDbAsync(): Array<Companies> = runBlocking{
        val companies = async {
            db.getAllCompanies().toTypedArray()
        }
        companies.await()
    }
}