/*
 * Copyright (C) 2026 Christian Sommer
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

import com.yacgroup.yacguide.database.Ascend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.TimeZone

internal class AscendDatePickerUtilsTest {

    private val _utcTimeZone = TimeZone.getTimeZone("UTC")

    @Test
    fun getSelection_withValidDate_returnsCorrectDate() {
        val ascend = Ascend(
            id = 0, routeId = 1, styleId = 1, partnerIds = null, notes = null,
            year = 2023, month = 10, day = 27
        )
        val utils = AscendDatePickerUtils(ascend)

        val selection = utils.getSelection(1)

        assertNotNull(selection)
        val calendar = Calendar.getInstance(_utcTimeZone).apply {
            timeInMillis = selection!!
        }

        assertEquals(2023, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH))
        assertEquals(27, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun getSelection_withInvalidDateAndNonZeroMode_returnsNull() {
        val ascend = Ascend(
            id = 0, routeId = 1, styleId = 1, partnerIds = null, notes = null,
            year = 0, month = 0, day = 0
        )
        val utils = AscendDatePickerUtils(ascend)

        val selection = utils.getSelection(1)

        assertNull(selection)
    }

    @Test
    fun getSelection_withInvalidDateAndZeroMode_returnsProvidedTodayDate() {
        val expectedToday = 123456789L
        val ascend = Ascend(
            id = 0, routeId = 1, styleId = 1, partnerIds = null, notes = null,
            year = 0, month = 0, day = 0
        )
        val utils = AscendDatePickerUtils(ascend, _todayProvider = { expectedToday })

        val selection = utils.getSelection(0)

        assertEquals(expectedToday, selection)
    }

    @Test
    fun updateAscend_updatesAscendObjectCorrectly() {
        val ascend = Ascend(
            id = 0, routeId = 1, styleId = 1, partnerIds = null, notes = null,
            year = 0, month = 0, day = 0
        )
        val utils = AscendDatePickerUtils(ascend)
        val calendar = Calendar.getInstance(_utcTimeZone).apply {
            set(2024, Calendar.JUNE, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        utils.updateAscend(calendar.timeInMillis)

        assertEquals(2024, ascend.year)
        assertEquals(6, ascend.month) // June is 5 in Calendar, so 6 in Ascend (1-based)
        assertEquals(15, ascend.day)
    }

    @Test
    fun selectionConsistency_afterUpdateAscend_returnsUpdatedValues() {
        val ascend = Ascend(
            id = 0, routeId = 1, styleId = 1, partnerIds = null, notes = null,
            year = 2022, month = 5, day = 20
        )
        val utils = AscendDatePickerUtils(ascend)

        val newDate = Calendar.getInstance(_utcTimeZone).apply {
            set(2025, Calendar.DECEMBER, 24, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        utils.updateAscend(newDate.timeInMillis)

        val selection = utils.getSelection(1)

        assertNotNull(selection)
        val resultCalendar = Calendar.getInstance(_utcTimeZone).apply {
            timeInMillis = selection!!
        }

        assertEquals(2025, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, resultCalendar.get(Calendar.MONTH))
        assertEquals(24, resultCalendar.get(Calendar.DAY_OF_MONTH))

        assertEquals(2025, ascend.year)
        assertEquals(12, ascend.month)
        assertEquals(24, ascend.day)
    }
}
