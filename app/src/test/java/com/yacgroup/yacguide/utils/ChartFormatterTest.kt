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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ChartFormatterTest {

    @Test
    fun axisFormatter_getAxisLabel_invalidIndex_returnsEmptyString() {
        val formatter = AxisFormatter(listOf("Label1", "Label2"))
        assertTrue(formatter.getAxisLabel(100f, null) == "")
    }

    @Test
    fun axisFormatter_getAxisLabel_validIndex_returnsAccordingElementFromPassedCustomLabelList() {
        val labelList = listOf("Label1", "Label2", "Label3")
        val formatter = AxisFormatter(labelList)
        labelList.forEachIndexed { idx, label ->
            assertEquals(label, formatter.getAxisLabel(idx.toFloat(), null))
        }
    }

    @Test
    fun intValueFormatter_getFormattedValue_valueIsZeroOrLower_ReturnsEmptyString() {
        assertTrue(IntValueFormatter().getFormattedValue(0f) == "")
        assertTrue(IntValueFormatter().getFormattedValue(-3f) == "")
    }

    @Test
    fun intValueFormatter_getFormattedValue_valueIsGreaterThanZero_ReturnsAccordingIntValueAsString() {
        val value = 3.5f
        assertTrue(value.toInt().toString() == IntValueFormatter().getFormattedValue(value))
    }
}