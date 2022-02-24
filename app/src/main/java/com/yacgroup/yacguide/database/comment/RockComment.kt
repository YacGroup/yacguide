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

package com.yacgroup.yacguide.database.comment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class RockComment {

    @PrimaryKey
    var id: Int = 0

    var qualityId: Int = 0
    var text: String? = null
    var rockId: Int = 0

    companion object {

        const val RELEVANCE_NONE = 0

        // This needs to be in sync with sandsteinklettern.de!
        val RELEVANCE_MAP: LinkedHashMap<Int, String> = object : LinkedHashMap<Int, String>() {
            init {
                put(RELEVANCE_NONE, "keine Angabe")
                put(1, "Hauptgipfel")
                put(2, "lohnender Gipfel")
                put(3, "Durchschnittsgipfel")
                put(4, "Quacke")
                put(5, "Dreckhaufen")
            }
        }
    }
}
