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
class Region {

    @PrimaryKey
    var id: Int = 0

    var name: String? = null
    var country: String? = null

    companion object {
        const val SELECT_ALL = "SELECT Region.* FROM Region"
        const val DELETE_ALL = "DELETE FROM Region"
        const val JOIN_ON = "JOIN Region ON Region.id ="
        const val FOR_COUNTRY = "WHERE Region.country ="
    }
}
