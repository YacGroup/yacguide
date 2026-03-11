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

/**
 * Enum class for input mode of the ascend date picker.
 */
enum class AscendDatePickerInputMode(val value: Int) {
    eText(MaterialDatePicker.INPUT_MODE_TEXT),
    eCalendar(MaterialDatePicker.INPUT_MODE_CALENDAR);

    companion object {
        /**
         * Returns the enum entry matching the given [value].
         * Defaults to [eCalendar] if no match is found.
         */
        fun from(value: Int): AscendDatePickerInputMode {
            return entries.find { it.value == value } ?: eCalendar
        }
    }
}
