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

package com.yacgroup.yacguide

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast

class PreferencesActivity : BaseNavigationActivity() {

    private var _customSettings: SharedPreferences? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_preferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.action_settings)

        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
        _displayContent()
    }

    fun saveSettings(v: View) {
        val editor = _customSettings!!.edit()
        editor.putBoolean(getString(R.string.tourbook_ordering_key), findViewById<CheckBox>(R.id.tourbookOrderingCheckbox).isChecked)
        editor.commit()
        Toast.makeText(this, R.string.settings_stored, Toast.LENGTH_SHORT).show()
    }

    private fun _displayContent() {
        findViewById<CheckBox>(R.id.tourbookOrderingCheckbox).isChecked =
                _customSettings!!.getBoolean(getString(R.string.tourbook_ordering_key),
                        resources.getBoolean(R.bool.order_tourbook_chronologically))
    }
}
