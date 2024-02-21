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
        customSettings.let {
            it.edit().apply {
                for ((oldResource, newResourcePair) in settingsResourceMap) {
                    val oldKey = context.resources.getString(oldResource)
                    val newKey = context.resources.getString(newResourcePair.first)
                    if (it.contains(oldKey)) {
                        if (oldResource == R.string.lead || oldResource == R.string.follow) {
                            this.putInt(
                                newKey,
                                it.getInt(oldKey, context.getColor(newResourcePair.second))
                            )
                        } else {
                            this.putBoolean(
                                newKey,
                                it.getBoolean(oldKey, context.resources.getBoolean(newResourcePair.second))
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
