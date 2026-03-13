/*
 * Copyright (C) 2024, 2026 Christian Sommer
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

package com.yacgroup.yacguide

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.skydoves.colorpickerpreference.ColorPickerPreference
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager

class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.preferences_filename)
        setPreferencesFromResource(R.xml.preferences, rootKey)

        /*
         * Configure color picker preferences
         */
        ColorPickerPreferenceManager.getInstance(requireContext()).apply {
            setSharedPreferenceName(
                requireContext(),
                getString(R.string.preferences_filename)
            )
        }
        for (stringResource in listOf(R.string.pref_key_color_lead, R.string.pref_key_color_follow)) {
            val colorPrefKey = getString(stringResource)
            findPreference<ColorPickerPreference>(colorPrefKey)?.let { colorPref ->
                colorPref.getColorPickerView().preferenceName = colorPrefKey
            }
        }
    }
}
