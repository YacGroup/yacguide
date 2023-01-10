/*
 * Copyright (C) 2019, 2023 Axel Paetzold
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
data class SectorComment(
    @PrimaryKey val id: Int,
    val qualityId: Int,
    val text: String?,
    val sectorId: Int
) {
    companion object {

        // This needs to be in sync with sandsteinklettern.de!
        val QUALITY_MAP: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(1, "Hauptteilgebiet")
                put(2, "lohnendes Gebiet")
                put(3, "kann man mal hingehen")
                put(4, "weniger bedeutend")
                put(5, "unbedeutend")
            }
        }
    }
}
