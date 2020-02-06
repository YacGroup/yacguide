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

package com.yacgroup.yacguide.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RouteDao {
    @get:Query("SELECT * FROM Route")
    val all: Array<Route>

    @Query("SELECT * FROM Route WHERE parentId = :parentId ORDER BY nr")
    fun getAll(parentId: Int): Array<Route>

    @Query("SELECT * FROM Route WHERE id = :id")
    fun getRoute(id: Int): Route?

    @Query("UPDATE Route SET description = :description WHERE id = :id")
    fun updateDescription(description: String, id: Int)

    @Query("UPDATE Route SET ascendsBitMask = :bitMask WHERE id = :id")
    fun setAscendsBitMask(bitMask: Int, id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(route: Route)

    @Query("DELETE FROM Route WHERE parentId = :parentId")
    fun deleteAll(parentId: Int)
}
