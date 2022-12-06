/*
 * Copyright (C) 2022 Axel Paetzold
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

package com.yacgroup.yacguide.list_adapters

import android.content.Context
import android.content.SharedPreferences
import com.yacgroup.yacguide.R

class RockCounterConfig(
    val countSummits: Boolean = false,
    val countMassifs: Boolean = false,
    val countBoulders: Boolean = false,
    val countCaves: Boolean = false,
    val countUnofficialRocks: Boolean = false,
    val countProhibitedRocks: Boolean = false,
    val countCollapsedRocks: Boolean = false,
    val countOnlyLeads: Boolean = false) {

    companion object {
        fun generate(context: Context, customSettings: SharedPreferences): RockCounterConfig {
            return RockCounterConfig(
                countSummits = customSettings.getBoolean(context.getString(R.string.count_summits),
                    context.resources.getBoolean(R.bool.count_summits)),
                countMassifs = customSettings.getBoolean(context.getString(R.string.count_massifs),
                    context.resources.getBoolean(R.bool.count_massifs)),
                countBoulders = customSettings.getBoolean(context.getString(R.string.count_boulders),
                    context.resources.getBoolean(R.bool.count_boulders)),
                countCaves = customSettings.getBoolean(context.getString(R.string.count_caves),
                    context.resources.getBoolean(R.bool.count_caves)),
                countUnofficialRocks = customSettings.getBoolean(context.getString(R.string.count_unofficial_rocks),
                    context.resources.getBoolean(R.bool.count_unofficial_rocks)),
                countProhibitedRocks = customSettings.getBoolean(context.getString(R.string.count_prohibited_rocks),
                    context.resources.getBoolean(R.bool.count_prohibited_rocks)),
                countCollapsedRocks = customSettings.getBoolean(context.getString(R.string.count_collapsed_rocks),
                    context.resources.getBoolean(R.bool.count_collapsed_rocks)),
                countOnlyLeads = customSettings.getBoolean(context.getString(R.string.count_only_leads),
                    context.resources.getBoolean(R.bool.count_only_leads))
            )
        }
    }
}