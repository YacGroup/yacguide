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

import androidx.room.*

@Dao
interface AscendDao {
    @get:Query(Ascend.SELECT_ALL)
    val all: List<Ascend>

    @Query("${Ascend.SELECT_ALL} WHERE year = :year AND styleId = :styleId ${Ascend.ORDER_CHRONOLOGICALLY}")
    fun getAll(year: Int, styleId: Int): List<Ascend>

    @Query("${Ascend.SELECT_ALL} WHERE year = :year AND styleId < :styleIdLimit ${Ascend.ORDER_CHRONOLOGICALLY}")
    fun getAllBelowStyleId(year: Int, styleIdLimit: Int): List<Ascend>

    @Query("${Ascend.SELECT_ALL} WHERE routeId = :routeId ${Ascend.ORDER_CHRONOLOGICALLY}")
    fun getAscendsForRoute(routeId: Int): List<Ascend>

    @Query("${Ascend.SELECT_ALL} ${Route.JOIN_ON} Ascend.routeId ${Route.FOR_ROCK} :rockId")
    fun getAscendsForRock(rockId: Int): List<Ascend>

    @Query("${Ascend.SELECT_ALL} WHERE id = :id")
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

    @Query(Ascend.DELETE_ALL)
    fun deleteAll()
}
