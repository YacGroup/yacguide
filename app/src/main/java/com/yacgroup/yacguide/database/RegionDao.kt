package com.yacgroup.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RegionDao {
    @Query("SELECT * FROM Region WHERE country = :countryName")
    fun getAll(countryName: String): Array<Region>

    @Query("SELECT * FROM Region WHERE id = :id")
    fun getRegion(id: Int): Region?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(region: Region)

    @Query("DELETE FROM Region WHERE Country = :countryName")
    fun deleteAll(countryName: String)
}
