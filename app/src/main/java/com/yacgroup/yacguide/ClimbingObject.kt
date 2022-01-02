/*
 * Copyright (C) 2021 Axel Paetzold
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

package com.yacgroup.yacguide

import com.yacgroup.yacguide.database.DatabaseWrapper

enum class ClimbingObjectLevel(val value: Int) {
    eUnknown(DatabaseWrapper.INVALID_ID),
    eCountry(0),
    eRegion(1),
    eSector(2),
    eRock(3),
    eRoute(4);

    companion object {
        fun fromInt(value: Int) = ClimbingObjectLevel.values().first { it.value == value }
    }
}

class ClimbingObject(level: ClimbingObjectLevel, parentId: Int, parentName: String) {
    val level: ClimbingObjectLevel = level
    val parentId: Int = parentId
    val parentName: String = parentName
}