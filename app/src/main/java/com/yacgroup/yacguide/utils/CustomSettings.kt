/*
 * Copyright (C) 2020 Axel Paetzold
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

package com.yacgroup.yacguide.utils

import android.graphics.Color

class CustomSettings {

    private val _colorLead = Color.GREEN
    private val _colorFollow = Color.GREEN

    fun getColorLead(): Int {
        return _colorLead
    }

    fun getColorFollow(): Int {
        return _colorFollow
    }

    companion object {

        private var _instance: CustomSettings? = null

        fun getCustomSettings(): CustomSettings {
            if (_instance == null) {
                _instance = CustomSettings()
            }
            return _instance as CustomSettings
        }
    }
}
