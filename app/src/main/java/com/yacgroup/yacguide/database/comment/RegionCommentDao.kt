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

package com.yacgroup.yacguide.database.comment

import androidx.room.*

import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_REGION_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_REGION_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_COMMENTS_REGION

@Dao
interface RegionCommentDao {
    @Query("$SELECT_REGION_COMMENTS WHERE RegionComment.regionId = :regionId")
    fun getAll(regionId: Int): List<RegionComment>

    @Query("$SELECT_REGION_COMMENTS $VIA_COMMENTS_REGION WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<RegionComment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RegionComment>)

    @Delete
    fun delete(regionComments: List<RegionComment>)

    @Query(DELETE_REGION_COMMENTS)
    fun deleteAll()

    @Query("$DELETE_REGION_COMMENTS WHERE RegionComment.regionId = :regionId")
    fun deleteAll(regionId: Int)
}
