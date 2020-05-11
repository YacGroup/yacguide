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

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.HashMap
import kotlin.experimental.or

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
    var ascendsBitMask: Int = 0 // a 13bit number for 13 ascend styles.
                                // Bit index == (style id - 1).
                                // Bit value == 1 if route has been ascended with this style
    var parentId: Int = 0

    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        val STATUS: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(1, "anerkannt.")
                put(2, "zeitlich gesperrt.")
                put(3, "voll gesperrt.")
                put(4, "eine Erstbegehung.")
                put(5, "nicht anerkannt.")
                put(6, "nur eine Erwähnung.")
                put(7, "ein Projekt.")
            }
        }
    }
}
