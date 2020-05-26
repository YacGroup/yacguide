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

@Dao
interface RegionCommentDao {
    @Query("${RegionComment.SELECT_ALL} ${RegionComment.FOR_REGION} :regionId")
    fun getAll(regionId: Int): List<RegionComment>

    @Query("${RegionComment.SELECT_ALL} ${Region.JOIN_ON} RegionComment.regionId ${Region.FOR_COUNTRY} :countryName")
    fun getAllInCountry(countryName: String): List<RegionComment>

    @Query("SELECT COUNT(*) FROM RegionComment ${RegionComment.FOR_REGION} :regionId")
    fun getCommentCount(regionId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RegionComment>)

    @Delete
    fun delete(regionComments: List<RegionComment>)

    @Query(RegionComment.DELETE_ALL)
    fun deleteAll()

    @Query("${RegionComment.DELETE_ALL} ${RegionComment.FOR_REGION} :regionId")
    fun deleteAll(regionId: Int)
}
