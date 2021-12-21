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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_REGIONS
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_REGIONS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_REGIONS_SECTORS

@Dao
interface RegionDao {
    @Query("$SELECT_REGIONS WHERE Region.country = :countryName")
    fun getAll(countryName: String): List<Region>

    @Query("$SELECT_REGIONS $VIA_REGIONS_SECTORS")
    fun getAllNonEmpty(): List<Region>

    @Query("$SELECT_REGIONS WHERE Region.id = :id")
    fun getRegion(id: Int): Region?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(regions: List<Region>)

    @Query(DELETE_REGIONS)
    fun deleteAll()

    @Query("$DELETE_REGIONS WHERE Region.country = :countryName")
    fun deleteAll(countryName: String)
}
