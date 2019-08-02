package com.example.paetz.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RockDao {
    @Query("SELECT * FROM Rock WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): Array<Rock>


    @Query("SELECT * FROM Rock ORDER BY nr")
    fun getAllRocks(): Array<Rock>


    @Query("SELECT * FROM Rock WHERE id = :id")
    fun getRock(id: Int): Rock?

    @Query("UPDATE Rock SET ascended = :ascended WHERE id = :id")
    fun updateAscended(ascended: Boolean, id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rock: Rock)

    @Query("DELETE FROM Rock WHERE parentId = :parentId")
    fun deleteAll(parentId: Int)
}
