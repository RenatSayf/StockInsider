package com.renatsayf.stockinsider.db

import androidx.room.*

@Dao
interface AppDao
{
    @Query("SELECT * FROM search_set")
    suspend fun getSearchSets() : List<RoomSearchSet>

    @Query("SELECT * FROM search_set WHERE search_set.set_name NOT IN ('default_set', 'purchases_more_1', 'purchases_more_5', 'sales_more_1', 'sales_more_5', 'current_set', 'pur_more1_for_3', 'pur_more5_for_3', 'sale_more1_for_3', 'sale_more5_for_3', 'pur_more1_for_14', 'pur_more5_for_14', 'sale_more1_for_14', 'sale_more5_for_14')")
    suspend fun getUserSearchSets() : List<RoomSearchSet>

    @Query("SELECT * FROM search_set WHERE set_name = :setName")
    suspend fun getSetByName(setName : String) : RoomSearchSet

    @Query ("SELECT * FROM search_set WHERE id = :id")
    suspend fun getSetById(id: Long) : RoomSearchSet?

    @Query("DELETE FROM search_set")
    suspend fun deleteAll() : Int

    @Delete(entity = RoomSearchSet::class)
    suspend fun deleteSet(set : RoomSearchSet) : Int

    @Query("DELETE FROM search_set WHERE id = :id")
    suspend fun deleteSetById(id: Long) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = RoomSearchSet::class)
    suspend fun insertOrUpdateSearchSet(set : RoomSearchSet) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = Company::class)
    suspend fun insertCompanies(list : List<Company>)

    @Query("SELECT ticker FROM companies")
    suspend fun getAllTickers() : List<String>

    @Query("SELECT * FROM companies")
    suspend fun getAllCompanies() : List<Company>?

    @Query("SELECT DISTINCT * FROM search_set WHERE target = :target")
    suspend fun getSearchSetsByTarget(target: String) : List<RoomSearchSet>

    @Query("SELECT DISTINCT * FROM search_set WHERE target = :target AND is_tracked = :isTracked")
    suspend fun getTrackedSets(target: String, isTracked: Int) : List<RoomSearchSet>

    @Query("SELECT * FROM companies WHERE ticker IN(:list)")
    suspend fun getCompanyByTicker(list: List<String>) : List<Company>

    @Query("SELECT DISTINCT * FROM companies WHERE company_name like '%' || :pattern || '%' OR ticker like '%' || :pattern || '%'")
    suspend fun getAllSimilar(pattern: String) : List<Company>

    @Query("UPDATE search_set SET ticker = :value WHERE set_name = :setName")
    suspend fun updateSearchSetTicker(setName: String, value: String) : Int


}