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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_ROUTES
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_NR
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_ROUTES
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_SECTOR
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROUTES_ASCENDS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROUTES_ROCK
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_SECTORS_REGION

@Dao
interface RouteDao {
    @get:Query(SELECT_ROUTES)
    val all: List<Route>

    @Query("$SELECT_ROUTES WHERE Route.parentId = :parentId $ORDERED_BY_NR")
    fun getAll(parentId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ASCENDS WHERE Route.parentId = :parentId AND Ascend.styleId = :styleId $ORDERED_BY_NR")
    fun getAllForStyle(parentId: Int, styleId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<Route>

    @Query("$SELECT_ROUTES $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<Route>

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
