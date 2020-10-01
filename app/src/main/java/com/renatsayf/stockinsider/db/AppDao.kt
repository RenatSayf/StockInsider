package com.renatsayf.stockinsider.db

import androidx.room.*
import com.renatsayf.stockinsider.models.SearchSet

@Dao
interface AppDao
{
    @Query("SELECT * FROM search_set")
    suspend fun getSearchSets() : List<RoomSearchSet>

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