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

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Rock {

    @PrimaryKey
    var id: Int = 0

    var nr: Float = 0.toFloat()
    var type: Char = ' '   // 'G' = Rock, 'M' = Massif, 'H' = Cage, 'B' = Boulder, 'N' = unofficial
    var status: Char = ' ' // 'X' = prohibited, 'Z' = temporarily prohibited, 'T' = partly prohibited
    var name: String? = null
    var longitude: Float = 0f
    var latitude: Float = 0f
    var ascendsBitMask: Int = 0 // a 13bit number for 13 ascend styles.
                                // Bit index == (style id - 1).
                                // Bit value == 1 if rock has been ascended with this style
    var parentId: Int = 0

    companion object {

        const val SELECT_ALL = "SELECT Rock.* FROM Rock"
        const val DELETE_ALL = "DELETE FROM Rock"
        const val JOIN_ON = "JOIN Rock ON Rock.id ="

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
