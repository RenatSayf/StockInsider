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

    @Query("DELETE FROM search_set")
    suspend fun deleteAll() : Int

    @Delete(entity = RoomSearchSet::class)
    suspend fun deleteSet(set : RoomSearchSet) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = RoomSearchSet::class)
    suspend fun insertOrUpdateSearchSet(set : RoomSearchSet) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = Companies::class)
    suspend fun insertCompanies(list : List<Companies>)

    @Query("SELECT ticker FROM companies")
    suspend fun getAllTickers() : List<String>

    @Query("SELECT * FROM companies")
    suspend fun getAllCompanies() : List<Companies>


}