package com.renatsayf.stockinsider.db

class FakeAppDao : AppDao {

    private var result: Any? = null

    fun<T> setExpectedResult(param: Any, result: T) {
        this.result = result
    }

    override suspend fun getSearchSets(): List<RoomSearchSet> {
        return listOf()
    }

    override suspend fun getUserSearchSets(): List<RoomSearchSet> {
        return listOf()
    }

    override suspend fun getSetByName(setName: String): RoomSearchSet {
        return result as RoomSearchSet
    }

    override suspend fun getSetById(id: Long): RoomSearchSet {
        return result as RoomSearchSet
    }

    override suspend fun deleteAll(): Int {
        return -1
    }

    override suspend fun deleteSet(set: RoomSearchSet): Int {
        return result as Int
    }

    override suspend fun deleteSetById(id: Long): Int {
        return result as Int
    }

    override suspend fun insertOrUpdateSearchSet(set: RoomSearchSet): Long {
        return -1L
    }

    override suspend fun insertOrIgnore(set: RoomSearchSet): Long {
        return 0L
    }

    override suspend fun insertCompanies(list: List<Company>) {

    }

    override suspend fun getAllTickers(): List<String> {
        return emptyList()
    }

    override suspend fun getAllCompanies(): List<Company>? {
        return emptyList()
    }

    override suspend fun getSearchSetsByTarget(target: String): List<RoomSearchSet> {
        return emptyList()
    }

    override suspend fun getTrackedSets(target: String, isTracked: Int): List<RoomSearchSet> {
        return emptyList()
    }

    override suspend fun getTrackedCount(): Int {
        return 0
    }

    override suspend fun getTargetCount(): Int {
        return 0
    }

    override suspend fun getCompanyByTicker(list: List<String>): List<Company> {
        return emptyList()
    }

    override suspend fun getAllSimilar(pattern: String): List<Company> {
        return emptyList()
    }

    override suspend fun updateSearchSetTicker(setName: String, value: String): Int {
        return -1
    }
}