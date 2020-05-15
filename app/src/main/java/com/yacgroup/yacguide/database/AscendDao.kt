/*
 * Copyright (C) 2019 Fabian Kantereit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yacgroup.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface AscendDao {
    @get:Query("SELECT * FROM Ascend")
    val all: List<Ascend>

    @Query("SELECT * FROM Ascend WHERE year = :year AND styleId = :styleId ORDER BY year, month, day")
    fun getAll(year: Int, styleId: Int): List<Ascend>

    @Query("SELECT * FROM Ascend WHERE year = :year AND styleId < :styleIdLimit ORDER BY year, month, day")
    fun getAllBelowStyleId(year: Int, styleIdLimit: Int): List<Ascend>

    @Query("SELECT * FROM Ascend WHERE routeId = :routeId ORDER BY year, month, day")
    fun getAscendsForRoute(routeId: Int): List<Ascend>

    @Query("SELECT Ascend.* FROM Ascend JOIN Route ON routeId = Route.id WHERE Route.parentId = :rockId")
    fun getAscendsForRock(rockId: Int): List<Ascend>

    @Query("SELECT * FROM Ascend WHERE id = :id")
    fun getAscend(id: Int): Ascend?

    @Query("SELECT DISTINCT year FROM Ascend WHERE styleId = :styleId")
    fun getYears(styleId: Int): IntArray

    @Query("SELECT DISTINCT year FROM Ascend WHERE styleId < :styleIdLimit")
    fun getYearsBelowStyleId(styleIdLimit: Int): IntArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ascend: Ascend)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ascends: List<Ascend>)

    @Delete
    fun delete(ascend: Ascend)

    @Query("DELETE FROM Ascend")
    fun deleteAll()
}
