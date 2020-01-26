package com.renatsayf.stockinsider.db

import androidx.room.*

@Dao
interface SearchSetDao
{
    @Query("SELECT * FROM search_set")
    suspend fun getSearchSets() : List<RoomSearchSet>

    @Query("SELECT * FROM search_set WHERE creation_date = :creationDate")
    suspend fun getSetById(creationDate : String) : RoomSearchSet

    @Delete
    suspend fun deleteSet(set : RoomSearchSet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSearchSet(set : RoomSearchSet) : Long
}