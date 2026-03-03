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

import com.google.android.material.datepicker.MaterialDatePicker
import com.yacgroup.yacguide.database.Ascend
import java.util.Calendar
import java.util.TimeZone

/**
 * Utility class for ascend date picker
 *
 * @param _ascend The ascend object to manage.
 * @param _todayProvider A function providing "today's" UTC milliseconds. Defaults to MaterialDatePicker.
 */
class AscendDatePickerUtils (
    private val _ascend: Ascend,
    private val _todayProvider: () -> Long = { MaterialDatePicker.todayInUtcMilliseconds() }
) {

    // Create UTC calendar instances to avoid time zone issues
    private val _calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

    /**
     * Return the initial selection time in milliseconds.
     *
     * @param calendarInputMode The input mode of the calendar picker. If 0 (text mode),
     * defaults to today's date if no date is set in the ascend object.
     * @return The selection in UTC milliseconds, or null if no date is set and mode is not 0.
     */
    fun getSelection(calendarInputMode: Int): Long? {
        return if (_ascend.year != 0 && _ascend.month != 0 && _ascend.day != 0) {
            _calendar.also {
                it.set(_ascend.year, _ascend.month - 1, _ascend.day)
            }.timeInMillis
        } else {
            // Do not fill the calendar with the current date in text mode
            // because this mode is intended for fast input of older dates.
            if (calendarInputMode == 0) _todayProvider() else null
        }
    }

    /**
     * Update the ascend object with the selected date.
     *
     * @param selection The selected date in UTC milliseconds since the Unix epoch.
     */
    fun updateAscend(selection: Long) {
        _calendar.timeInMillis = selection
        _ascend.year = _calendar.get(Calendar.YEAR)
        _ascend.month = _calendar.get(Calendar.MONTH) + 1
        _ascend.day = _calendar.get(Calendar.DAY_OF_MONTH)
    }
}
