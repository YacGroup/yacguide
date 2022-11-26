/*
 * Copyright (C) 2020, 2022 Axel Paetzold
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
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.utils.DialogWidgetBuilder

class PreferencesActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _customSettings: SharedPreferences

    private val _settingKeysMap = mapOf(R.id.tourbookOrderingCheckbox to Pair(R.string.order_tourbook_chronologically,
                                                                              R.bool.order_tourbook_chronologically),
                                        R.id.countSummitsCheckbox to Pair(R.string.count_summits,
                                                                          R.bool.count_summits),
                                        R.id.countMassifsCheckbox to Pair(R.string.count_massifs,
                                                                          R.bool.count_massifs),
                                        R.id.countBouldersCheckbox to Pair(R.string.count_boulders,
                                                                           R.bool.count_boulders),
                                        R.id.countCavesCheckbox to Pair(R.string.count_caves,
                                                                        R.bool.count_caves),
                                        R.id.countUnofficialRocks to Pair(R.string.count_unofficial_rocks,
                                                                          R.bool.count_unofficial_rocks),
                                        R.id.countProhibitedRocks to Pair(R.string.count_prohibited_rocks,
                                                                          R.bool.count_prohibited_rocks),
                                        R.id.countCollapsedRocks to Pair(R.string.count_collapsed_rocks,
                                                                         R.bool.count_collapsed_rocks),
                                        R.id.countOnlyLeadsCheckbox to Pair(R.string.count_only_leads,
                                                                            R.bool.count_only_leads),
                                        R.id.colorizeTourbookEntriesCheckbox to Pair(R.string.colorize_tourbook_entries,
                                                                                     R.bool.colorize_tourbook_entries))

    private lateinit var _ascendColorsList: List<Int>

    override fun getLayoutId() = R.layout.activity_preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.action_settings)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
        _ascendColorsList = listOf(
                ContextCompat.getColor(this, R.color.greenblue),
                ContextCompat.getColor(this, R.color.green),
                ContextCompat.getColor(this, R.color.yellow),
                ContextCompat.getColor(this, R.color.red),
                ContextCompat.getColor(this, R.color.purple),
                ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.white))

        _displayContent()
    }

    override fun onStop() {
        _storeSettings()
        super.onStop()
    }

    fun changeColor(view: View) {
        val currColor = (view.background as ColorDrawable).color
        val nextColor = _ascendColorsList[(_ascendColorsList.indexOf(currColor) + 1) % _ascendColorsList.size]
        view.setBackgroundColor(nextColor)
    }

    @Suppress("UNUSED_PARAMETER")
    fun resetDatabase(view: View) {
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
        val editor = _customSettings.edit()
        for ((_, keyPair) in _settingKeysMap) {
            editor.putBoolean(getString(keyPair.first), resources.getBoolean(keyPair.second))
        }
        editor.putInt(getString(R.string.lead), ContextCompat.getColor(this, R.color.color_lead))
        editor.putInt(getString(R.string.follow), ContextCompat.getColor(this, R.color.color_follow))

        editor.apply()

        _displayContent()
    }

    private fun _storeSettings() {
        val editor = _customSettings.edit()
        for ((checkboxId, keyPair) in _settingKeysMap) {
            editor.putBoolean(getString(keyPair.first), findViewById<CheckBox>(checkboxId).isChecked)
        }
        editor.putInt(getString(R.string.lead), (findViewById<Button>(R.id.leadColorButton).background as ColorDrawable).color)
        editor.putInt(getString(R.string.follow), (findViewById<Button>(R.id.followColorButton).background as ColorDrawable).color)

        editor.commit()
    }

    private fun _displayContent() {
        for ((checkboxId, keyPair) in _settingKeysMap) {
            findViewById<CheckBox>(checkboxId).isChecked = _customSettings.getBoolean(
                    getString(keyPair.first),
                    resources.getBoolean(keyPair.second))
        }
        findViewById<Button>(R.id.leadColorButton).setBackgroundColor(_customSettings.getInt(getString(R.string.lead),
                        ContextCompat.getColor(this, R.color.color_lead)))
        findViewById<Button>(R.id.followColorButton).setBackgroundColor(_customSettings.getInt(getString(R.string.follow),
                        ContextCompat.getColor(this, R.color.color_follow)))
    }
}
