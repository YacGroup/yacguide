/*
 * Copyright (C) 2020, 2022, 2023 Axel Paetzold
 *               2024 Christian Sommer
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
import android.widget.Toast
import androidx.preference.Preference
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.utils.DialogWidgetBuilder

class PreferencesActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _customSettings: SharedPreferences

    override fun getLayoutId() = R.layout.activity_preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.action_settings)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)

        _displayContent()
    }

    private fun _resetDatabase() {
        DialogWidgetBuilder(this, R.string.reset_database).apply {
            setMessage(R.string.reset_database_confirm)
            setNegativeButton()
            setPositiveButton(R.string.ok) {_, _ ->
                _db.deleteCountriesRecursively()
                _db.deleteAscends()
                _resetCustomSettings()
                Toast.makeText(this.context, R.string.reset_database_done, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun _resetCustomSettings() {
        _customSettings.edit().apply {
            clear().apply()
            putInt(
                getString(R.string.default_region_key),
                resources.getInteger(R.integer.default_region_id))
            putStringSet(
                getString(R.string.pinned_countries),
                emptySet<String>())
        }.apply()

        _displayContent()
    }

    private fun _displayContent() {
        PreferenceFragment().let { it ->
            supportFragmentManager.apply {
                beginTransaction()
                    .replace(R.id.preference_container, it)
                    .commit()
                // Necessary otherwise the method findPreference below will not find anything.
                executePendingTransactions()
            }
            it.findPreference<Preference>("reset_database")?.setOnPreferenceClickListener { _ ->
                _resetDatabase()
                true
            }
        }
    }
}
