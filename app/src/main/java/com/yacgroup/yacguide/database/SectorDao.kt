/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_SECTORS
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_SECTOR
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_SECTORS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_SECTORS_REGION

@Dao
interface SectorDao {
    @get:Query("$SELECT_SECTORS $ORDERED_BY_SECTOR")
    val all: List<Sector>

    @Query("$SELECT_SECTORS WHERE Sector.parentId = :regionId $ORDERED_BY_SECTOR")
    fun getAll(regionId: Int): List<Sector>

    @Query("$SELECT_SECTORS $VIA_SECTORS_REGION WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<Sector>

    @Query("$SELECT_SECTORS WHERE Sector.id = :id")
    fun getSector(id: Int): Sector?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sectors: List<Sector>)

    @Delete
    fun delete(sectors: List<Sector>)

    @Query(DELETE_SECTORS)
    fun deleteAll()

    @Query("$DELETE_SECTORS WHERE Sector.parentId = :parentId")
    fun deleteAll(parentId: Int)
}
