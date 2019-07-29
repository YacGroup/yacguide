package com.example.paetz.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface SectorDao {
    @Query("SELECT * FROM Sector WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): Array<Sector>

    @Query("SELECT * FROM Sector WHERE id = :id")
    fun getSector(id: Int): Sector?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sector: Sector)

    @Query("DELETE FROM Sector WHERE parentId = :parentId")
    fun deleteAll(parentId: Int)
}
