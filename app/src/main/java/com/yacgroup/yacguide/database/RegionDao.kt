/*
 * Copyright (C) 2019, 2022, 2023 Axel Paetzold
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
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_COUNTRY
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_REGIONS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_REGIONS_COUNTRY
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_REGIONS_SECTORS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_ROUTES
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_SECTORS_ROCKS

@Dao
interface RegionDao {
    @get:Query(SELECT_REGIONS)
    val all: List<Region>

    @Query("$SELECT_REGIONS WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<Region>

    @Query("$SELECT_REGIONS $VIA_REGIONS_SECTORS $VIA_REGIONS_COUNTRY $ORDERED_BY_COUNTRY")
    fun getAllNonEmpty(): List<Region>

    @Query("$SELECT_REGIONS WHERE Region.id = :id")
    fun getRegion(id: Int): Region?

    @Query("$SELECT_REGIONS $VIA_REGIONS_SECTORS $VIA_SECTORS_ROCKS $VIA_ROCKS_ROUTES WHERE Route.id = :routeId")
    fun getRegionForRoute(routeId: Int): Region?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(regions: List<Region>)

    @Query(DELETE_REGIONS)
    fun deleteAll()
}
