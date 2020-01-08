package com.example.paetz.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface AscendDao {
    @get:Query("SELECT * FROM Ascend")
    val all: Array<Ascend>

    @Query("SELECT * FROM Ascend WHERE year = :year AND styleId = :styleId ORDER BY year, month, day")
    fun getAll(year: Int, styleId: Int): Array<Ascend>

    @Query("SELECT * FROM Ascend WHERE year = :year AND styleId < :styleIdLimit ORDER BY year, month, day")
    fun getAllBelowStyleId(year: Int, styleIdLimit: Int): Array<Ascend>

    @Query("SELECT * FROM Ascend WHERE routeId = :routeId ORDER BY year, month, day")
    fun getAscendsForRoute(routeId: Int): Array<Ascend>

    @Query("SELECT * FROM Ascend WHERE id = :id")
    fun getAscend(id: Int): Ascend?

    @Query("SELECT DISTINCT year FROM Ascend WHERE styleId = :styleId")
    fun getYears(styleId: Int): IntArray

    @Query("SELECT DISTINCT year FROM Ascend WHERE styleId < :styleIdLimit")
    fun getYearsBelowStyleId(styleIdLimit: Int): IntArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ascend: Ascend)

    @Delete
    fun delete(ascend: Ascend)
}
