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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_ROCKS
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_ROCK
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_ROCKS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_ROUTES
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_SECTOR
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROUTES_ASCENDS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_SECTORS_REGION

@Dao
interface RockDao {
    @get:Query(SELECT_ROCKS)
    val all: List<Rock>

    @Query("$SELECT_ROCKS WHERE LOWER(Rock.name) LIKE LOWER(:name)")
    fun getAllByName(name: String): List<Rock>

    @Query("$SELECT_ROCKS $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<Rock>

    @Query("$SELECT_ROCKS $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName AND LOWER(Rock.name) LIKE LOWER(:name)")
    fun getAllByNameInCountry(countryName: String, name: String): List<Rock>

    @Query("$SELECT_ROCKS $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<Rock>

    @Query("$SELECT_ROCKS $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId AND LOWER(Rock.name) LIKE LOWER(:name)")
    fun getAllByNameInRegion(regionId: Int, name: String): List<Rock>

    @Query("$SELECT_ROCKS WHERE Rock.parentId = :sectorId $ORDERED_BY_ROCK")
    fun getAllInSector(sectorId: Int): List<Rock>

    @Query("$SELECT_ROCKS WHERE Rock.parentId = :sectorId AND LOWER(Rock.name) LIKE LOWER(:name) $ORDERED_BY_ROCK")
    fun getAllByNameInSector(sectorId: Int, name: String): List<Rock>

    @Query("$SELECT_ROCKS WHERE Rock.id = :id")
    fun getRock(id: Int): Rock?

    @Query("$SELECT_ROCKS $VIA_ROCKS_ROUTES $VIA_ROUTES_ASCENDS WHERE Ascend.id = :ascendId")
    fun getRockForAscend(ascendId: Int): Rock?

    @Update
    fun update(rock: Rock)

    @Update
    fun update(rocks: List<Rock>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rocks: List<Rock>)

    @Delete
    fun delete(rocks: List<Rock>)

    @Query(DELETE_ROCKS)
    fun deleteAll()
}
