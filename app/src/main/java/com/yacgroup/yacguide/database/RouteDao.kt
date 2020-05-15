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

import android.arch.persistence.room.*

@Dao
interface RouteDao {
    @get:Query("SELECT * FROM Route")
    val all: List<Route>

    @Query("SELECT * FROM Route WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): List<Route>

    @Query("SELECT * FROM Route JOIN Rock ON Route.parentId = Rock.id WHERE Rock.parentId = :sectorId")
    fun getAllInSector(sectorId: Int): List<Route>

    @Query("SELECT * FROM Route JOIN Rock ON Route.parentId = Rock.id JOIN Sector ON Rock.parentId = Sector.id WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<Route>

    @Query("SELECT * FROM Route JOIN Rock ON Route.parentId = Rock.id JOIN Sector ON Rock.parentId = Sector.id JOIN Region ON Sector.parentId = Region.id WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<Route>

    @Query("SELECT * FROM Route WHERE id = :id")
    fun getRoute(id: Int): Route?

    @Update
    fun update(route: Route)

    @Update
    fun update(routes: List<Route>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routes: List<Route>)

    @Delete
    fun delete(routes: List<Route>)

    @Query("DELETE FROM Route")
    fun deleteAll()
}
