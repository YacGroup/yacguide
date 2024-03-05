/*
 * Copyright (C) 2024 Christian Sommer
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

package com.yacgroup.yacguide.extensions

import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager

/*
 * See https://stackoverflow.com/questions/45131683/kotlin-extension-function-access-java-private-field
 *
 * TODO: Remove, if the following feature request is implemented: https://github.com/skydoves/ColorPickerView/issues/145
 */
fun ColorPickerView.getSharedPreferenceManager(): ColorPickerPreferenceManager {
    return ColorPickerView::class.java.getDeclaredField("preferenceManager").let { field ->
        field.isAccessible = true
        return@let field.get(this) as ColorPickerPreferenceManager
    }
}
