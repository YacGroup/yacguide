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
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Sector

@Dao
interface RockCommentDao {
    @Query("${RockComment.SELECT_ALL} ${RockComment.FOR_ROCK} :rockId")
    fun getAll(rockId: Int): List<RockComment>

    @Query("${RockComment.SELECT_ALL} ${Rock.JOIN_ON} RockComment.rockId ${Rock.FOR_SECTOR} :sectorId")
    fun getAllInSector(sectorId: Int): List<RockComment>

    @Query("${RockComment.SELECT_ALL} ${Rock.JOIN_ON} RockComment.rockId ${Sector.JOIN_ON} Rock.parentId ${Sector.FOR_REGION} :regionId")
    fun getAllInRegion(regionId: Int): List<RockComment>

    @Query("${RockComment.SELECT_ALL} ${Rock.JOIN_ON} RockComment.rockId ${Sector.JOIN_ON} Rock.parentId ${Region.JOIN_ON} Sector.parentId ${Region.FOR_COUNTRY} :countryName")
    fun getAllInCountry(countryName: String): List<RockComment>

    @Query("SELECT COUNT(*) FROM RockComment ${RockComment.FOR_ROCK} :rockId")
    fun getCommentCount(rockId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RockComment>)

    @Delete
    fun delete(comments: List<RockComment>)

    @Query(RockComment.DELETE_ALL)
    fun deleteAll()
}
