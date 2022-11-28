/*
 * Copyright (C) 2022 Axel Paetzold
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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DateUtilsTest {

    @Test
    fun formatDate_invalidDateFormat_returnsEmptyString() {
        val formattedDate = DateUtils.formatDate("abc")
        assertTrue(formattedDate.isEmpty())
    }

    @Test
    fun formatDate_validDateFormatWithInvalidDate_returnsEmptyString() {
        val formattedDate = DateUtils.formatDate("0000-00-00")
        assertTrue(formattedDate.isEmpty())
    }

    @Test
    fun formatDate_validDateFormatWithValidDate_returnsDateInProperFormat() {
        val formattedDate = DateUtils.formatDate("2012-05-21")
        assertEquals("21.5.2012", formattedDate)
    }
}