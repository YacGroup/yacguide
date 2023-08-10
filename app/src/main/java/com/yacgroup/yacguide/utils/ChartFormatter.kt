/*
 * Copyright (C) 2023 Axel Paetzold
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

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class AxisFormatter(private val _labels: List<String>) : ValueFormatter() {
    // This class provides custom axis labels different from indices
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return try {
            _labels[value.toInt()]
        } catch (e: IndexOutOfBoundsException) {
            ""
        }
    }
}

class IntValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return if (value > 0) value.toInt().toString() else ""
    }
}