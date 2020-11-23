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

import java.util.ArrayList

@Entity
class Ascend {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var routeId: Int = 0
    var styleId: Int = 0 // id for os, RP, af, ...
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var partnerIds: ArrayList<Int>? = null
    var notes: String? = null

    companion object {
        const val SELECT_ALL = "SELECT Ascend.* FROM Ascend"
        const val DELETE_ALL = "DELETE FROM Ascend"
        const val JOIN_UP_ON = "JOIN Ascend ON Ascend.routeId ="
        const val ORDER_CHRONOLOGICALLY = "ORDER BY year, month, day"
    }

}
