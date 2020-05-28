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

package com.yacgroup.yacguide.database.Comment

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import java.util.HashMap

@Entity
class RegionComment {

    @PrimaryKey
    var id: Int = 0

    var qualityId: Int = 0
    var text: String? = null
    var regionId: Int = 0

    companion object {

        const val SELECT_ALL = "SELECT RegionComment.* FROM RegionComment"
        const val DELETE_ALL = "DELETE FROM RegionComment"
        const val FOR_REGION = "WHERE RegionComment.regionId ="

        // This needs to be in sync with sandsteinklettern.de!
        val QUALITY_MAP: Map<Int, String> = object : HashMap<Int, String>() {
            init {
                put(1, "international bedeutend")
                put(2, "national bedeutend")
                put(3, "lohnt einen Abstecher")
                put(4, "lokal bedeutend")
                put(5, "unbedeutend")
            }
        }
    }
}
