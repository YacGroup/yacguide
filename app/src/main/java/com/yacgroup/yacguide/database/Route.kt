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

import java.util.HashMap

@Entity
data class Route(
    @PrimaryKey val id: Int,
    val nr: Float,
    val statusId: Int,
    val name: String?,
    val grade: String?,
    val firstAscendLeader: String?,
    val firstAscendFollower: String?,
    val firstAscendDate: String?,
    val typeOfClimbing: String?,
    val description: String?,
    var ascendsBitMask: Int,    // a 32bit number for 14 ascend styles.
                                // Bit index == (styleId - 1).
                                // Bit value == 1 if route has been ascended with this style.
                                // Note: styleId == 0 ("Unknown") corresponds to mask 0b10000...0 = -IntMax.
    val parentId: Int
) {
    companion object {

        const val STATUS_ACKNOWLEDGED = 1
        const val STATUS_TEMP_CLOSED = 2
        const val STATUS_CLOSED = 3
        const val STATUS_FIRST_ASCEND = 4
        const val STATUS_UNACKNOWLEDGED = 5
        const val STATUS_MENTION = 6
        const val STATUS_UNFINISHED = 7

        // This needs to be in sync with sandsteinklettern.de!
        val STATUS: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(STATUS_ACKNOWLEDGED, "anerkannt.")
                put(STATUS_TEMP_CLOSED, "zeitlich gesperrt.")
                put(STATUS_CLOSED, "voll gesperrt.")
                put(STATUS_FIRST_ASCEND, "eine Erstbegehung.")
                put(STATUS_UNACKNOWLEDGED, "nicht anerkannt.")
                put(STATUS_MENTION, "nur eine Erwähnung.")
                put(STATUS_UNFINISHED, "ein Projekt.")
            }
        }
    }
}
