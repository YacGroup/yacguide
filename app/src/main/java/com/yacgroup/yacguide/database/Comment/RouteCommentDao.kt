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

import android.arch.persistence.room.*

@Dao
interface RouteCommentDao {
    @Query("SELECT * FROM RouteComment WHERE routeId = :routeId")
    fun getAll(routeId: Int): List<RouteComment>

    @Query("SELECT * FROM RouteComment JOIN Route ON RouteComment.routeId = Route.id JOIN Rock ON Route.parentId = Rock.id WHERE Rock.parentId = :sectorId")
    fun getAllInSector(sectorId: Int): List<RouteComment>

    @Query("SELECT * FROM RouteComment JOIN Route ON RouteComment.routeId = Route.id JOIN Rock ON Route.parentId = Rock.id JOIN Sector ON Rock.parentId = Sector.id WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<RouteComment>

    @Query("SELECT * FROM RouteComment JOIN Route ON RouteComment.routeId = Route.id JOIN Rock ON Route.parentId = Rock.id JOIN Sector ON Rock.parentId = Sector.id JOIN Region ON Sector.parentId = Region.id WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<RouteComment>

    @Query("SELECT COUNT(*) FROM RouteComment WHERE routeId = :routeId")
    fun getCommentCount(routeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RouteComment>)

    @Delete
    fun delete(comments: List<RouteComment>)

    @Query("DELETE FROM RouteComment")
    fun deleteAll()
}
