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

import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.utils.AscendStyle


class RockCounter(private val _config: RockCounterConfig) {

    inner class RockCount(
        val ascended: Int,
        val total: Int
    )

    fun isApplicable() = (_config.countSummits
                       || _config.countMassifs
                       || _config.countBoulders
                       || _config.countCaves)

    fun calculateRockCount(rocks: List<Rock>): RockCount {
        val consideredRocks = _filterConsideredRocks(rocks)

        val ascendedRocks = if (_config.countOnlyLeads) {
            consideredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) }
        } else {
            consideredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) || AscendStyle.isFollow(it.ascendsBitMask) }
        }

        return RockCount(ascended = ascendedRocks.size,
                         total = consideredRocks.size)
    }

    fun _filterConsideredRocks(rocks: List<Rock>): List<Rock> {
        var filteredRocks = rocks
        if (!_config.countSummits) {
            filteredRocks = filteredRocks.filter{ it.type != Rock.typeSummit && it.type != Rock.typeAlpine }
        }
        if (!_config.countMassifs) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeMassif && it.type != Rock.typeStonePit }
        }
        if (!_config.countBoulders) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeBoulder }
        }
        if (!_config.countCaves) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeCave }
        }

        if (!_config.countUnofficialRocks) {
            filteredRocks = filteredRocks.filter { it.type != Rock.typeUnofficial }
        }
        if (!_config.countProhibitedRocks) {
            filteredRocks = filteredRocks.filter { it.status != Rock.statusProhibited }
        }
        if (!_config.countCollapsedRocks) {
            filteredRocks = filteredRocks.filter { it.status != Rock.statusCollapsed }
        }

        return filteredRocks
    }

}