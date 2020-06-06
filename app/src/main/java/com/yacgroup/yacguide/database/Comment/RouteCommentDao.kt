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

package com.yacgroup.yacguide.database.Comment

import androidx.room.*
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.database.Sector

@Dao
interface RouteCommentDao {
    @Query("${RouteComment.SELECT_ALL} ${RouteComment.FOR_ROUTE} :routeId")
    fun getAll(routeId: Int): List<RouteComment>

    @Query("${RouteComment.SELECT_ALL} ${Route.JOIN_ON} RouteComment.routeId ${Rock.JOIN_ON} Route.parentId ${Sector.JOIN_ON} Rock.parentId ${Sector.FOR_REGION} :regionId")
    fun getAllInRegion(regionId: Int): List<RouteComment>

    @Query("${RouteComment.SELECT_ALL} ${Route.JOIN_ON} RouteComment.routeId ${Rock.JOIN_ON} Route.parentId ${Sector.JOIN_ON} Rock.parentId ${Region.JOIN_ON} Sector.parentId ${Region.FOR_COUNTRY} :countryName")
    fun getAllInCountry(countryName: String): List<RouteComment>

    @Query("SELECT COUNT(*) FROM RouteComment ${RouteComment.FOR_ROUTE} :routeId")
    fun getCommentCount(routeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RouteComment>)

    @Delete
    fun delete(comments: List<RouteComment>)

    @Query(RouteComment.DELETE_ALL)
    fun deleteAll()
}
