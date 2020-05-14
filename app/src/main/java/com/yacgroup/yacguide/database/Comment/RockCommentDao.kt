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

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RockCommentDao {
    @Query("SELECT * FROM RockComment WHERE rockId = :rockId")
    fun getAll(rockId: Int): List<RockComment>

    @Query("SELECT COUNT(*) FROM RockComment WHERE rockId = :rockId")
    fun getCommentCount(rockId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comment: RockComment)

    @Query("DELETE FROM RockComment WHERE rockId = :rockId")
    fun deleteAll(rockId: Int)
}
