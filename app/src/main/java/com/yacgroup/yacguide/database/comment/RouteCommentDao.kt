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

package com.yacgroup.yacguide.database.comment

import androidx.room.*
import com.yacgroup.yacguide.database.SqlMacros.Companion.COUNT_ROUTE_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_ROUTE_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_ROUTE_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_COMMENTS_ROUTE
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_SECTOR
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROUTES_ROCK
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_SECTORS_REGION

@Dao
interface RouteCommentDao {
    @get:Query(SELECT_ROUTE_COMMENTS)
    val all: List<RouteComment>

    @Query("$SELECT_ROUTE_COMMENTS WHERE RouteComment.routeId = :routeId")
    fun getAll(routeId: Int): List<RouteComment>

    @Query("$SELECT_ROUTE_COMMENTS $VIA_COMMENTS_ROUTE $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<RouteComment>

    @Query("$SELECT_ROUTE_COMMENTS $VIA_COMMENTS_ROUTE $VIA_ROUTES_ROCK $VIA_ROCKS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<RouteComment>

    @Query("$COUNT_ROUTE_COMMENTS WHERE RouteComment.routeId = :routeId")
    fun getCommentCount(routeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RouteComment>)

    @Delete
    fun delete(comments: List<RouteComment>)

    @Query(DELETE_ROUTE_COMMENTS)
    fun deleteAll()
}
