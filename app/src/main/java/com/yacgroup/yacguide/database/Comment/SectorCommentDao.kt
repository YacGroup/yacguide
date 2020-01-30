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
interface SectorCommentDao {
    @Query("SELECT * FROM SectorComment WHERE sectorId = :sectorId")
    fun getAll(sectorId: Int): Array<SectorComment>

    @Query("SELECT COUNT(*) FROM SectorComment WHERE sectorId = :sectorId")
    fun getCommentCount(sectorId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comment: SectorComment)

    @Query("DELETE FROM SectorComment WHERE sectorId = :sectorId")
    fun deleteAll(sectorId: Int)
}
