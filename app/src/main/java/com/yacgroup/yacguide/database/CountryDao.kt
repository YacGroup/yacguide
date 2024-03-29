/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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
import com.yacgroup.yacguide.database.SqlMacros.Companion.DELETE_COUNTRIES
import com.yacgroup.yacguide.database.SqlMacros.Companion.ORDERED_BY_COUNTRY
import com.yacgroup.yacguide.database.SqlMacros.Companion.SELECT_COUNTRIES
import com.yacgroup.yacguide.database.SqlMacros.Companion.VIA_COUNTRIES_REGIONS

@Dao
interface CountryDao {
    @get:Query("$SELECT_COUNTRIES $ORDERED_BY_COUNTRY")
    val all: List<Country>

    @Query("$SELECT_COUNTRIES $VIA_COUNTRIES_REGIONS")
    fun getAllNonEmpty(): List<Country>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(countries: List<Country>)

    @Query(DELETE_COUNTRIES)
    fun deleteAll()
}
