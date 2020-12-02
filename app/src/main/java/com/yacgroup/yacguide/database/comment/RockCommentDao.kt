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

import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_ROCK_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_ROCK_COMMENTS
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_COMMENTS_ROCK
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_REGION
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_ROCKS_SECTOR

@Dao
interface RockCommentDao {
    @Query("$SELECT_ROCK_COMMENTS WHERE RockComment.rockId = :rockId")
    fun getAll(rockId: Int): List<RockComment>

    @Query("$SELECT_ROCK_COMMENTS $VIA_COMMENTS_ROCK $VIA_ROCKS_SECTOR WHERE Sector.parentId = :regionId")
    fun getAllInRegion(regionId: Int): List<RockComment>

    @Query("$SELECT_ROCK_COMMENTS $VIA_COMMENTS_ROCK $VIA_ROCKS_REGION WHERE Region.country = :countryName")
    fun getAllInCountry(countryName: String): List<RockComment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comments: List<RockComment>)

    @Delete
    fun delete(comments: List<RockComment>)

    @Query(DELETE_ROCK_COMMENTS)
    fun deleteAll()
}
