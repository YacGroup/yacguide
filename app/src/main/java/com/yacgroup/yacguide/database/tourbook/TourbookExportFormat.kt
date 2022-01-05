/*
 * Copyright (C) 2022 Christian Sommer
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

package com.yacgroup.yacguide.database.tourbook

enum class TourbookExportFormat (val id: Int) {
    eJSON(0),
    eJSONVERBOSE(1),
    eCSV(2);

    companion object {
        private val _by_id = HashMap<Int, TourbookExportFormat>()

        init {
            values().forEach { _by_id[it.id] = it }
        }

        fun fromId(id: Int): TourbookExportFormat? = _by_id[id]
    }
}