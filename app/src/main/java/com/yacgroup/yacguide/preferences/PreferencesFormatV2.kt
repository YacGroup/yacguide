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

package com.yacgroup.yacguide.preferences

import com.yacgroup.yacguide.R

/**
 * Enum class that defines the preferences format version 2 and the mapping to the keys of version 1.
 */
enum class PreferencesFormatV2(
    val resIdKey: Int,
    val resIdKeyV1: Int,
    val keyType: PreferencesType,
    val resIdDefaultValue: Int) {

    eOrderTourbookChronologically(
        R.string.pref_key_order_tourbook_chronologically,
        R.string.order_tourbook_chronologically,
        PreferencesType.eBoolean,
        R.bool.pref_default_order_tourbook_chronologically
    ),
    eOnlyOfficialSummits(
        R.string.pref_key_only_official_summits,
        R.string.only_official_summits,
        PreferencesType.eBoolean,
        R.bool.pref_default_only_official_summits
    ),
    eOnlyOfficialRoutes(
        R.string.pref_key_only_official_routes,
        R.string.only_official_routes,
        PreferencesType.eBoolean,
        R.bool.pref_default_only_official_routes
    ),
    eCountSummits(
        R.string.pref_key_count_summits,
        R.string.count_summits,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_summits
    ),
    eCountMassifs(
        R.string.pref_key_count_massifs,
        R.string.count_massifs,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_massifs
    ),
    eCountBoulders(
        R.string.pref_key_count_boulders,
        R.string.count_boulders,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_boulders,
    ),
    eCountCaves(
        R.string.pref_key_count_caves,
        R.string.count_caves,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_caves
    ),
    eCountUnofficialRocks(
        R.string.pref_key_count_unofficial_rocks,
        R.string.count_unofficial_rocks,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_unofficial_rocks
    ),
    eCountProhibitedRocks(
        R.string.pref_key_count_prohibited_rocks,
        R.string.count_prohibited_rocks,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_prohibited_rocks
    ),
    eCountCollapsedRocks(
        R.string.pref_key_count_collapsed_rocks,
        R.string.count_collapsed_rocks,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_collapsed_rocks
    ),
    eCountOnlyLeads(
        R.string.pref_key_count_only_leads,
        R.string.count_only_leads,
        PreferencesType.eBoolean,
        R.bool.pref_default_count_only_leads

    ),
    eColorizeTourbookEntries(
        R.string.pref_key_colorize_tourbook_entries,
        R.string.colorize_tourbook_entries,
        PreferencesType.eBoolean,
        R.bool.pref_default_colorize_tourbook_entries
    ),
    eColorLead(
        R.string.pref_key_color_lead,
        R.string.lead,
        PreferencesType.eInt,
        R.color.pref_default_color_lead
    ),
    eColorFollow(
        R.string.pref_key_color_follow,
        R.string.follow,
        PreferencesType.eInt,
        R.color.pref_default_color_follow
    )
}
