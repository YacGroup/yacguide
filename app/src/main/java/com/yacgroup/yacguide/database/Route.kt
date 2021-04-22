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

import java.util.HashMap

@Entity
class Route {

    @PrimaryKey
    var id: Int = 0

    var nr: Float = 0.toFloat()
    var statusId: Int = 0
    var name: String? = null
    var grade: String? = null
    var firstAscendLeader: String? = null
    var firstAscendFollower: String? = null
    var firstAscendDate: String? = null
    var typeOfClimbing: String? = null
    var description: String? = null
    var ascendsBitMask: Int = 0 // a 32bit number for 14 ascend styles.
                                // Bit index == (styleId - 1).
                                // Bit value == 1 if route has been ascended with this style.
                                // Note: styleId == 0 ("Unknown") corresponds to mask 0b10000...0 = -IntMax.
    var parentId: Int = 0

    companion object {

        const val statusAcknowledged = 1
        const val statusTempClosed = 2
        const val statusClosed = 3
        const val statusFirstAscend = 4
        const val statusUnacknowledged = 5
        const val statusMention = 6
        const val statusUnfinished = 7

        // This needs to be in sync with sandsteinklettern.de!
        val STATUS: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(statusAcknowledged, "anerkannt.")
                put(statusTempClosed, "zeitlich gesperrt.")
                put(statusClosed, "voll gesperrt.")
                put(statusFirstAscend, "eine Erstbegehung.")
                put(statusUnacknowledged, "nicht anerkannt.")
                put(statusMention, "nur eine Erwähnung.")
                put(statusUnfinished, "ein Projekt.")
            }
        }
    }
}
