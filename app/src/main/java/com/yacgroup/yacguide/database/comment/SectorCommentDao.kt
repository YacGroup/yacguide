/*
 * Copyright (C) 2019, 2023 Axel Paetzold
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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_SECTOR_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_SECTOR_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_COMMENTS_SECTOR
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_SECTORS_REGION

@Dao
interface SectorCommentDao {
    @get:Query("$SELECT_SECTOR_COMMENTS")
    val all: List<SectorComment>

    @Query("$SELECT_SECTOR_COMMENTS WHERE SectorComment.sectorId = :sectorId")
    fun getAll(sectorId: Int): List<SectorComment>

    @Query("$SELECT_SECTOR_COMMENTS $VIA_COMMENTS_SECTOR WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<SectorComment>

    @Query("$SELECT_SECTOR_COMMENTS $VIA_COMMENTS_SECTOR $VIA_SECTORS_REGION WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<SectorComment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<SectorComment>)

    @Delete
    fun delete(comments: List<SectorComment>)

    @Query(DELETE_SECTOR_COMMENTS)
    fun deleteAll()
}
