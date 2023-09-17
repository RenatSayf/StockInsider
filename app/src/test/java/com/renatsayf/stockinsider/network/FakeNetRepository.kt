package com.renatsayf.stockinsider.network

import com.renatsayf.stockinsider.db.Company
import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FakeNetRepository : INetRepository {

    private var result: Result<Any?> = Result.failure(Exception(""))

    fun<T> setExpectedResult(result: Result<T>) {
        this.result = if (result.isSuccess) {
            Result.success(result.getOrNull())
        }
        else {
            Result.failure(result.getOrThrow() as Throwable)
        }
    }

    override suspend fun getTradingListAsync(set: SearchSet): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                if (result.isSuccess) {
                    result.getOrNull() as Result<List<Deal>>
                } else {
                    Result.failure(result.getOrThrow() as Throwable)
                }
            }
        }
    }

    override suspend fun getInsiderTradingAsync(insider: String): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                if (result.isSuccess) {
                    result.getOrNull() as Result<List<Deal>>
                } else {
                    Result.failure(result.getOrThrow() as Throwable)
                }
            }
        }
    }

    override suspend fun getDealsByTicker(ticker: String): Deferred<Result<List<Deal>>> {
        return coroutineScope {
            async {
                if (result.isSuccess) {
                    result.getOrNull() as Result<List<Deal>>
                } else {
                    Result.failure(result.getOrThrow() as Throwable)
                }
            }
        }
    }

    override suspend fun getAllCompaniesNameAsync(): Deferred<Result<List<Company>>> {
        return coroutineScope {
            async {
                Result.success(result as List<Company>)
            }
        }
    }

    override suspend fun getDealsListAsync(set: SearchSet): Deferred<List<Deal>> {
        return coroutineScope {
            async {
                result as List<Deal>
            }
        }
    }
}