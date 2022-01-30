package com.renatsayf.stockinsider.db

class FakeAppDao : AppDao {

    private var result: Any? = null

    fun<T> setExpectedResult(param: Any, result: T) {
        this.result = result
    }

    override suspend fun getSearchSets(): List<RoomSearchSet> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserSearchSets(): List<RoomSearchSet> {
        TODO("Not yet implemented")
    }

    override suspend fun getSetByName(setName: String): RoomSearchSet {
        return result as RoomSearchSet
    }

    override suspend fun deleteAll(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSet(set: RoomSearchSet): Int {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrUpdateSearchSet(set: RoomSearchSet): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertCompanies(list: List<Companies>) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllTickers(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllCompanies(): List<Companies>? {
        TODO("Not yet implemented")
    }

    override suspend fun getSearchSetsByTarget(target: String): List<RoomSearchSet> {
        TODO("Not yet implemented")
    }
}