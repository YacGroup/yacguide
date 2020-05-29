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
import com.yacgroup.yacguide.database.Sector

@Dao
interface SectorCommentDao {
    @Query("${SectorComment.SELECT_ALL} ${SectorComment.FOR_SECTOR} :sectorId")
    fun getAll(sectorId: Int): List<SectorComment>

    @Query("${SectorComment.SELECT_ALL} ${Sector.JOIN_ON} SectorComment.sectorId ${Sector.FOR_REGION} :regionId")
    fun getAllInRegion(regionId: Int): List<SectorComment>

    @Query("${SectorComment.SELECT_ALL} ${Sector.JOIN_ON} SectorComment.sectorId ${Region.JOIN_ON} Sector.parentId ${Region.FOR_COUNTRY} :countryName")
    fun getAllInCountry(countryName: String): List<SectorComment>

    @Query("SELECT COUNT(*) FROM SectorComment ${SectorComment.FOR_SECTOR} :sectorId")
    fun getCommentCount(sectorId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<SectorComment>)

    @Delete
    fun delete(comments: List<SectorComment>)

    @Query(SectorComment.DELETE_ALL)
    fun deleteAll()

    @Query("${SectorComment.DELETE_ALL} ${SectorComment.FOR_SECTOR} :sectorId")
    fun deleteAll(sectorId: Int)
}
