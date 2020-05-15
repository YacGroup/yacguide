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
interface SectorCommentDao {
    @Query("SELECT * FROM SectorComment WHERE sectorId = :sectorId")
    fun getAll(sectorId: Int): List<SectorComment>

    @Query("SELECT * FROM SectorComment JOIN Sector ON SectorComment.sectorId = Sector.id WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<SectorComment>

    @Query("SELECT * FROM SectorComment JOIN Sector ON SectorComment.sectorId = Sector.id JOIN Region ON Sector.parentId = Region.id WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<SectorComment>

    @Query("SELECT COUNT(*) FROM SectorComment WHERE sectorId = :sectorId")
    fun getCommentCount(sectorId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<SectorComment>)

    @Delete
    fun delete(comments: List<SectorComment>)

    @Query("DELETE FROM SectorComment")
    fun deleteAll()

    @Query("DELETE FROM SectorComment WHERE sectorId = :sectorId")
    fun deleteAll(sectorId: Int)
}
