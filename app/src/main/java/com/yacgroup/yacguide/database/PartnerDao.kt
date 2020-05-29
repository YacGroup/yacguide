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

import androidx.room.*

@Dao
interface PartnerDao {
    @get:Query(Partner.SELECT_ALL)
    val all: List<Partner>

    @Query("SELECT Id FROM Partner WHERE Name = :name")
    fun getId(name: String): Int

    @Query("${Partner.SELECT_ALL} WHERE Id = :id")
    fun getPartner(id: Int): Partner?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(partner: Partner)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(partners: List<Partner>)

    @Delete
    fun delete(partner: Partner)

    @Query(Partner.DELETE_ALL)
    fun deleteAll()
}
