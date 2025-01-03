/*
 * Copyright (C) 2022, 2025 Axel Paetzold
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
        val consideredRockTypes = getConsideredRockTypes()
        val excludedRockStates = getExcludedRockStates()
        val consideredRocks = rocks.filter { consideredRockTypes.contains(it.type) && !excludedRockStates.contains(it.status) }

        val ascendedRocks = if (_config.countOnlyLeads) {
            consideredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) }
        } else {
            consideredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) || AscendStyle.isFollow(it.ascendsBitMask) }
        }

        return RockCount(ascended = ascendedRocks.size,
                         total = consideredRocks.size)
    }

    fun getConsideredRockTypes(): List<Char> {
        val consideredRockTypes = emptyList<Char>().toMutableList()
        if (_config.countSummits) {
            consideredRockTypes.add(Rock.typeSummit)
            consideredRockTypes.add(Rock.typeAlpine)
        }
        if (_config.countMassifs) {
            consideredRockTypes.add(Rock.typeMassif)
            consideredRockTypes.add(Rock.typeStonePit)
        }
        if (_config.countBoulders) {
            consideredRockTypes.add(Rock.typeBoulder)
        }
        if (_config.countCaves) {
            consideredRockTypes.add(Rock.typeCave)
        }
        if (_config.countUnofficialRocks) {
            consideredRockTypes.add(Rock.typeUnofficial)
        }

        return consideredRockTypes
    }

    fun getExcludedRockStates(): List<Char> {
        val excludedRockStates = emptyList<Char>().toMutableList()
        if (!_config.countProhibitedRocks) {
            excludedRockStates.add(Rock.statusProhibited)
        }
        if (!_config.countCollapsedRocks) {
            excludedRockStates.add(Rock.statusCollapsed)
        }

        return excludedRockStates
    }

}