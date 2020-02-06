package com.renatsayf.stockinsider.db

import androidx.room.*

@Dao
interface SearchSetDao
{
    @Query("SELECT * FROM search_set")
    suspend fun getSearchSets() : List<RoomSearchSet>

    @Query("SELECT * FROM search_set WHERE set_name = :setName")
    suspend fun getSetByName(setName : String) : RoomSearchSet

    @Query("DELETE FROM search_set")
    suspend fun deleteAll() : Int

    @Delete
    suspend fun deleteSet(set : RoomSearchSet) : Int

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = RoomSearchSet::class)
    suspend fun insertOrUpdateSearchSet(set : RoomSearchSet) : Long

//    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = Companies::class)
//    suspend fun insertCompanies(list : List<Companies>)
}