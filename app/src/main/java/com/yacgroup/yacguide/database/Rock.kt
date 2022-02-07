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

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Rock(
    @PrimaryKey val id: Int,
    val nr: Float,
    val type: Char,
    val status: Char,
    val name: String?,
    val longitude: Float,
    val latitude: Float,
    var ascendsBitMask: Int,    // a 32bit number for 14 ascend styles.
                                // Bit index == (styleId - 1).
                                // Bit value == 1 if rock has been ascended with this style.
                                // Note: styleId == 0 ("Unknown") corresponds to mask 0b10000...0 = -IntMax.
    val parentId: Int
) {
    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        const val typeSummit = 'G'
        const val typeMassif = 'M'
        const val typeBoulder = 'B'
        const val typeStonePit = 'S'
        const val typeAlpine = 'A'
        const val typeCave = 'H'
        const val typeUnofficial = 'N'

        const val statusTemporarilyProhibited = 'Z'
        const val statusProhibited = 'X'
        const val statusPartlyProhibited = 'T'
        const val statusCollapsed = 'E'
    }
}
