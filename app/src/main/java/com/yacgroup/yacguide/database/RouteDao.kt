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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_ROUTES
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_REGION
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_ROCK
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_ROUTE
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_SECTOR
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_ROUTES
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_SECTOR
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROUTES_ASCENDS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROUTES_QUALITY
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROUTES_ROCK
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_SECTORS_REGION

@Dao
interface RouteDao {
    @get:Query(SELECT_ROUTES)
    val all: List<Route>

    @Query("$SELECT_ROUTES WHERE LOWER(Route.name) LIKE LOWER(:name)")
    fun getAllByName(name: String): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_QUALITY WHERE QualityAvg.quality <= :maxQualityId")
    fun getAllByQuality(maxQualityId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ASCENDS WHERE Ascend.styleId = :styleId $ORDERED_BY_ROUTE")
    fun getAllForStyle(styleId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName $ORDERED_BY_REGION")
    fun getAllInCountry(countryName: String): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName AND LOWER(Route.name) LIKE LOWER(:name) $ORDERED_BY_REGION")
    fun getAllByNameInCountry(countryName: String, name: String): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_QUALITY $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName AND QualityAvg.quality <= :maxQualityId")
    fun getAllByQualityInCountry(countryName: String, maxQualityId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION $VIA_ROUTES_ASCENDS WHERE Region.country = :countryName AND Ascend.styleId = :styleId $ORDERED_BY_REGION")
    fun getAllInCountryForStyle(countryName: String, styleId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId $ORDERED_BY_SECTOR")
    fun getAllInRegion(regionId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId AND LOWER(Route.name) LIKE LOWER(:name) $ORDERED_BY_SECTOR")
    fun getAllByNameInRegion(regionId: Int, name: String): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_QUALITY $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId AND QualityAvg.quality <= :maxQualityId")
    fun getAllByQualityInRegion(regionId: Int, maxQualityId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR $VIA_ROUTES_ASCENDS WHERE Sector.parentId = :regionId AND Ascend.styleId = :styleId $ORDERED_BY_SECTOR")
    fun getAllInRegionForStyle(regionId: Int, styleId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK WHERE Rock.parentId = :sectorId $ORDERED_BY_ROCK")
    fun getAllInSector(sectorId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK WHERE Rock.parentId = :sectorId AND LOWER(Route.name) LIKE LOWER(:name) $ORDERED_BY_ROCK")
    fun getAllByNameInSector(sectorId: Int, name: String): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_QUALITY $VIA_ROUTES_ROCK WHERE Rock.parentId = :sectorId AND QualityAvg.quality <= :maxQualityId")
    fun getAllByQualityInSector(sectorId: Int, maxQualityId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROUTES_ASCENDS WHERE Rock.parentId = :sectorId AND Ascend.styleId = :styleId $ORDERED_BY_ROCK")
    fun getAllInSectorForStyle(sectorId: Int, styleId: Int): List<Route>

    @Query("$SELECT_ROUTES WHERE Route.parentId = :rockId $ORDERED_BY_ROUTE")
    fun getAllAtRock(rockId: Int): List<Route>

    @Query("$SELECT_ROUTES WHERE Route.parentId = :rockId AND LOWER(Route.name) LIKE LOWER(:name) $ORDERED_BY_ROUTE")
    fun getAllByNameAtRock(rockId: Int, name: String): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_QUALITY WHERE Route.parentId = :rockId AND QualityAvg.quality <= :maxQualityId")
    fun getAllByQualityAtRock(rockId: Int, maxQualityId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ASCENDS WHERE Route.parentId = :rockId AND Ascend.styleId = :styleId $ORDERED_BY_ROUTE")
    fun getAllAtRockForStyle(rockId: Int, styleId: Int): List<Route>

    @Query("$SELECT_ROUTES WHERE Route.id = :id")
    fun getRoute(id: Int): Route?

    @Update
    fun update(route: Route)

    @Update
    fun update(routes: List<Route>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routes: List<Route>)

    @Delete
    fun delete(routes: List<Route>)

    @Query(DELETE_ROUTES)
    fun deleteAll()
}
