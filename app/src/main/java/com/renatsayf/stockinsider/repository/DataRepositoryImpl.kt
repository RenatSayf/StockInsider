package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.AppDao
import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.INetRepository
import com.renatsayf.stockinsider.utils.sortByAnotherList
import kotlinx.coroutines.*
import javax.inject.Inject

open class DataRepositoryImpl @Inject constructor(private val network: INetRepository, private val db: AppDao) : IDataRepository
{
    override suspend fun getTradingListFromNetAsync(set: SearchSet): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                network.getTradingListAsync(set).await()
            }
        }
    }
    override suspend fun getInsiderTradingFromNetAsync2(insider: String): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                network.getInsiderTradingAsync(insider).await()
            }
        }
    }
    override suspend fun getAllCompanies(): Deferred<Result<List<Company>>> {
        return coroutineScope {
            async {
                network.getAllCompaniesNameAsync().await()
            }
        }
    }

    override suspend fun getAllSearchSetsFromDbAsync(): List<RoomSearchSet> = coroutineScope {
        withContext(Dispatchers.IO) {
            db.getSearchSets()
        }
    }

    override suspend fun getDealsByTicker(ticker: String): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                network.getDealsByTicker(ticker).await()
            }
        }
    }

    override suspend fun getUserSearchSetsFromDbAsync(): List<RoomSearchSet> = coroutineScope {
        withContext(Dispatchers.IO) {
            db.getUserSearchSets()
        }
    }

    override suspend fun getSearchSetFromDbAsync(setName: String): RoomSearchSet = coroutineScope {
        withContext(Dispatchers.IO) {
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

    override suspend fun addNewSearchSetAsync(set: RoomSearchSet): Deferred<Long> {
        return coroutineScope {
            async {
                db.insertOrIgnore(set)
            }
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
        withContext(Dispatchers.IO) {
            db.getAllCompanies()?.toTypedArray()
        }
    }

    override suspend fun getSearchSetsByTarget(target: String): List<RoomSearchSet> = coroutineScope {
        withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
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

}

