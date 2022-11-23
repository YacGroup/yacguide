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
data class RockComment(
    @PrimaryKey val id: Int,
    val qualityId: Int,
    val text: String?,
    val rockId: Int
) {
    companion object {

        const val NO_INFO_ID = 0
        const val RELEVANCE_MAJOR = 1
        const val RELEVANCE_WORTHWHILE = 2
        const val RELEVANCE_AVERAGE = 3
        const val RELEVANCE_MINOR = 4
        const val RELEVANCE_MUCK = 5

        // This needs to be in sync with sandsteinklettern.de!
        val RELEVANCE_MAP: LinkedHashMap<Int, String> = object : LinkedHashMap<Int, String>() {
            init {
                put(NO_INFO_ID, "keine Angabe")
                put(RELEVANCE_MAJOR, "Hauptgipfel")
                put(RELEVANCE_WORTHWHILE, "lohnender Gipfel")
                put(RELEVANCE_AVERAGE, "Durchschnittsgipfel")
                put(RELEVANCE_MINOR, "Quacke")
                put(RELEVANCE_MUCK, "Dreckhaufen")
            }
        }
    }
}
