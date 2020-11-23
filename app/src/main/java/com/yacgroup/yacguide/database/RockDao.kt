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
interface RockDao {
    @get:Query(Rock.SELECT_ALL)
    val all: List<Rock>

    @Query("${Rock.SELECT_ALL} WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): List<Rock>

    @Query("SELECT DISTINCT Rock.* FROM Rock ${Route.JOIN_UP_ON} Rock.id ${Ascend.JOIN_UP_ON} Route.id ${Rock.FOR_SECTOR} :parentId AND Ascend.styleId = :styleId")
    fun getAllForStyle(parentId: Int, styleId: Int): List<Rock>

    @Query("${Rock.SELECT_ALL} ${Sector.JOIN_ON} Rock.parentId ${Sector.FOR_REGION} :regionId")
    fun getAllInRegion(regionId: Int): List<Rock>

    @Query("${Rock.SELECT_ALL} ${Sector.JOIN_ON} Rock.parentId ${Region.JOIN_ON} Sector.parentId ${Region.FOR_COUNTRY} :countryName")
    fun getAllInCountry(countryName: String): List<Rock>

    @Query("${Rock.SELECT_ALL} WHERE id = :id")
    fun getRock(id: Int): Rock?

    @Query("${Rock.SELECT_ALL} JOIN Route ON Rock.id = Route.parentId JOIN Ascend ON Route.id = Ascend.routeId WHERE Ascend.id = :ascendId")
    fun getRockForAscend(ascendId: Int): Rock?

    @Update
    fun update(rock: Rock)

    @Update
    fun update(rocks: List<Rock>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rocks: List<Rock>)

    @Delete
    fun delete(rocks: List<Rock>)

    @Query(Rock.DELETE_ALL)
    fun deleteAll()
}
