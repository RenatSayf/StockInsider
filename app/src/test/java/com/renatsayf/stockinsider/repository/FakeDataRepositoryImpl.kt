package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.db.FakeAppDao
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.network.FakeNetRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FakeDataRepositoryImpl(
    private val net: FakeNetRepository,
    private val dao: FakeAppDao
) : DataRepositoryImpl(net, dao) {

    override suspend fun getSearchSetByIdAsync(id: Long): Deferred<RoomSearchSet?> {
        return coroutineScope {

            async {
                dao.getSetById(id)
            }
        }
    }
}