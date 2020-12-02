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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_PARTNERS
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_PARTNERS
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_PARTNER_ID

@Dao
interface PartnerDao {
    @get:Query(SELECT_PARTNERS)
    val all: List<Partner>

    @Query("$SELECT_PARTNER_ID WHERE Partner.name = :name")
    fun getId(name: String): Int

    @Query("$SELECT_PARTNERS WHERE Partner.id = :id")
    fun getPartner(id: Int): Partner?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(partner: Partner)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(partners: List<Partner>)

    @Delete
    fun delete(partner: Partner)

    @Query(DELETE_PARTNERS)
    fun deleteAll()
}
