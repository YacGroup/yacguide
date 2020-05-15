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
interface SectorDao {
    @Query("${Sector.SELECT_ALL} WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): List<Sector>

    @Query("${Sector.SELECT_ALL} ${Region.JOIN_ON} Sector.parentId ${Region.FOR_COUNTRY} :countryName")
    fun getAllInCountry(countryName: String): List<Sector>

    @Query("${Sector.SELECT_ALL} WHERE id = :id")
    fun getSector(id: Int): Sector?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sectors: List<Sector>)

    @Delete
    fun delete(sectors: List<Sector>)

    @Query(Sector.DELETE_ALL)
    fun deleteAll()

    @Query("${Sector.DELETE_ALL} WHERE parentId = :parentId")
    fun deleteAll(parentId: Int)
}
