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

package com.yacgroup.yacguide.migration

import android.content.Context
import android.content.SharedPreferences
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.preferences.PreferencesFormatV2

/**
 * Class for migrating preferences.
 */
class PreferencesMigration(
    var context: Context,
    var customSettings: SharedPreferences) {

    /**
     * Migrate settings, if necessary, to fix issue https://github.com/YacGroup/yacguide/issues/453.
     */
    fun migrateFromVersion1To2() {
        customSettings.let {
            it.edit().apply {
                PreferencesFormatV2.entries.forEach {formatEntry ->
                    val oldKey = context.resources.getString(formatEntry.resIdKeyV1)
                    val newKey = context.resources.getString(formatEntry.resIdKey)
                    if (it.contains(oldKey)) {
                        if (formatEntry.resIdKeyV1 == R.string.lead || formatEntry.resIdKeyV1 == R.string.follow) {
                            this.putInt(
                                newKey,
                                it.getInt(oldKey, context.getColor(formatEntry.resIdDefaultValue))
                            )
                        } else {
                            this.putBoolean(
                                newKey,
                                it.getBoolean(
                                    oldKey,
                                    context.resources.getBoolean(formatEntry.resIdDefaultValue)
                                )
                            )
                        }
                        this.remove(oldKey)
                    }
                }
                this.putInt(
                    context.resources.getString(R.string.pref_key_pref_version),
                    context.resources.getInteger(R.integer.pref_version)
                )
            }.apply()
        }
    }
}
