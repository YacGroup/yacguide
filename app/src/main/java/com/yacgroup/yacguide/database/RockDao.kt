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
interface RockDao {
    @get:Query("SELECT * FROM Rock")
    val all: List<Rock>

    @Query("SELECT * FROM Rock WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): List<Rock>

    @Query("SELECT * FROM Rock JOIN Sector ON Rock.parentId = Sector.id WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<Rock>

    @Query("SELECT * FROM Rock JOIN Sector ON Rock.parentId = Sector.id JOIN Region ON Sector.parentId = Region.id WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<Rock>

    @Query("SELECT * FROM Rock WHERE id = :id")
    fun getRock(id: Int): Rock?

    @Query("SELECT Rock.* FROM Rock JOIN Route ON Rock.id = Route.parentId JOIN Ascend ON Route.id = Ascend.routeId WHERE Ascend.id = :ascendId")
    fun getRockForAscend(ascendId: Int): Rock?

    @Update
    fun update(rock: Rock)

    @Update
    fun update(rocks: List<Rock>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rocks: List<Rock>)

    @Delete
    fun delete(rocks: List<Rock>)

    @Query("DELETE FROM Rock")
    fun deleteAll()

    @Query("DELETE FROM Rock WHERE parentId = :parentId")
    fun deleteAll(parentId: Int)
}
