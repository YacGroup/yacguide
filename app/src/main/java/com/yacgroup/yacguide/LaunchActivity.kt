/*
 * Copyright (C) 2022, 2023 Axel Paetzold
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
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.extensions.getPackageInfoCompat
import com.yacgroup.yacguide.extensions.versionCodeCompat
import com.yacgroup.yacguide.network.CountryAndRegionParser
import com.yacgroup.yacguide.utils.IntentConstants

const val TIME_OUT_MS = 1500L

class LaunchActivity : AppCompatActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _customSettings: SharedPreferences
    private lateinit var _updateHandler: UpdateHandler

    inner class Timer : CountDownTimer(TIME_OUT_MS, TIME_OUT_MS) {
        override fun onTick(msUntilFinish: Long) {}
        override fun onFinish() {
            _updateHandler.abort()
            _switchToDatabase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        _db = DatabaseWrapper(this)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
        _checkForSettingsMigration()

        _updateHandler = UpdateHandler(this, CountryAndRegionParser(_db))
        _updateHandler.update(
            onUpdateFinished = { findViewById<ImageView>(R.id.iconImageView).setImageResource(R.drawable.ic_start_done) },
            isSilent = true)
        Timer().start()
    }

    private fun _switchToDatabase() {
        val invalidId = resources.getInteger(R.integer.pref_default_region_id)
        val defaultRegionId = _customSettings.getInt(getString(R.string.pref_key_default_region), invalidId)
        val intent = if (defaultRegionId == invalidId || _db.getRegion(defaultRegionId) == null)
            Intent(this, CountryActivity::class.java).apply {
                putExtra(IntentConstants.SHOW_WHATS_NEW, _checkForVersionUpdate())
            }
        else
            Intent(this, SectorActivity::class.java).apply {
                putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eSector.value)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, defaultRegionId)
                putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, _db.getRegion(defaultRegionId)?.name.orEmpty())
                putExtra(IntentConstants.SHOW_WHATS_NEW, _checkForVersionUpdate())
            }
        startActivity(intent)
        finish()
    }

    /**
     * Check whether the "What's New" dialog needs to be shown or not. This is done by comparing
     * the version stored in the settings with the current app version.
     */
    private fun _checkForVersionUpdate(): Boolean {
        val savedVerCode = _customSettings.getInt(
            getString(R.string.pref_key_version_code), 0)
        val curVerCode = try {
            packageManager.getPackageInfoCompat(packageName, 0).versionCodeCompat
        } catch (e: Exception) {
            0
        }
        return if (curVerCode > savedVerCode) {
            _customSettings.edit().apply {
                putInt(getString(R.string.pref_key_version_code), curVerCode)
            }.apply()
            true
        } else {
            false
        }
    }

    /**
     * Check whether the settings need to be migrated or not.
     */
    private fun _checkForSettingsMigration() {
        if (_customSettings.getInt(getString(R.string.pref_key_pref_version), 1) == 1) {
            _migrateSettings()
        }
    }

    /**
     * Migrate settings, if necessary, to fix issue https://github.com/YacGroup/yacguide/issues/453.
     */
    private fun _migrateSettings() {
        val settingsResourceMap = mapOf(
            R.string.order_tourbook_chronologically to
                    Pair(R.string.pref_key_order_tourbook_chronologically, R.bool.pref_default_order_tourbook_chronologically),
            R.string.only_official_summits to
                    Pair(R.string.pref_key_only_official_summits, R.bool.pref_default_only_official_summits),
            R.string.only_official_routes to
                    Pair(R.string.pref_key_only_official_routes, R.bool.pref_default_only_official_routes),
            R.string.count_summits to
                Pair(R.string.pref_key_count_summits, R.bool.pref_default_count_summits),
            R.string.count_massifs to
                Pair(R.string.pref_key_count_massifs, R.bool.pref_default_count_massifs),
            R.string.count_boulders to
                    Pair(R.string.pref_key_count_boulders, R.bool.pref_default_count_boulders),
            R.string.count_caves to
                    Pair(R.string.pref_key_count_caves, R.bool.pref_default_count_caves),
            R.string.count_unofficial_rocks to
                    Pair(R.string.pref_key_count_unofficial_rocks, R.bool.pref_default_count_unofficial_rocks),
            R.string.count_prohibited_rocks to
                    Pair(R.string.pref_key_count_prohibited_rocks, R.bool.pref_default_count_prohibited_rocks),
            R.string.count_collapsed_rocks to
                    Pair(R.string.pref_key_count_collapsed_rocks, R.bool.pref_default_count_collapsed_rocks),
            R.string.count_only_leads to
                    Pair(R.string.pref_key_count_only_leads, R.bool.pref_default_count_only_leads),
            R.string.colorize_tourbook_entries to
                    Pair(R.string.pref_key_colorize_tourbook_entries, R.bool.pref_default_colorize_tourbook_entries),
            R.string.lead to
                    Pair(R.string.pref_key_color_lead, R.color.pref_default_color_lead),
            R.string.follow to
                    Pair(R.string.pref_key_color_follow, R.color.pref_default_color_follow)
        )
        _customSettings.let {
            it.edit().apply {
                for ((oldResource, newResourcePair) in settingsResourceMap) {
                    val oldKey = resources.getString(oldResource)
                    val newKey = resources.getString(newResourcePair.first)
                    Log.d("[PREF]", "Checking for deprecated key '$oldKey'.")
                    if (it.contains(oldKey)) {
                        Log.d("[PREF]", "Migrating key '$oldKey' to '$newKey'.")
                        if (oldResource in listOf(R.string.lead, R.string.follow)) {
                            this.putInt(
                                newKey,
                                it.getInt(oldKey, this@LaunchActivity.getColor(newResourcePair.second))
                            )
                        } else {
                            this.putBoolean(
                                newKey,
                                it.getBoolean(oldKey, resources.getBoolean(newResourcePair.second))
                            )
                        }
                        this.remove(oldKey)
                    }
                }
                this.putInt(
                    resources.getString(R.string.pref_key_pref_version),
                    resources.getInteger(R.integer.pref_version)
                )
            }.apply()
        }
    }
}
