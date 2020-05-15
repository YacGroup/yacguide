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
interface RegionCommentDao {
    @Query("SELECT * FROM RegionComment WHERE regionId = :regionId")
    fun getAll(regionId: Int): List<RegionComment>

    @Query("SELECT * FROM RegionComment JOIN Region ON RegionComment.regionId = Region.id WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<RegionComment>

    @Query("SELECT COUNT(*) FROM RegionComment WHERE regionId = :regionId")
    fun getCommentCount(regionId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RegionComment>)

    @Delete
    fun delete(regionComments: List<RegionComment>)

    @Query("DELETE FROM RegionComment")
    fun deleteAll()

    @Query("DELETE FROM RegionComment WHERE regionId = :regionId")
    fun deleteAll(regionId: Int)
}
