/*
 * Copyright (C) 2023, 2025 Axel Paetzold
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

package com.yacgroup.yacguide.statistics

import android.content.Context
import android.content.SharedPreferences
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.ObjectCount
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.list_adapters.RockCounter
import com.yacgroup.yacguide.list_adapters.RockCounterConfig
import com.yacgroup.yacguide.utils.AscendStyle

class StatisticGenerator(private val _ctx: Context, private val _customSettings: SharedPreferences) {

    private val _db = DatabaseWrapper(_ctx)

    fun generateAscendCountsStatistic(): Statistic {
        // The following counts contain all years, even if there was no ascent of the according style.
        // This means that the list sizes are all equal.
        val soloCounts = _db.getAscendCountPerYear(AscendStyle.eSOLO.id)
        val leadCounts = _db.getAscendCountPerYear(AscendStyle.eONSIGHT.id, AscendStyle.eHOCHGESCHLEUDERT.id)
        val followCounts = _db.getAscendCountPerYear(AscendStyle.eALTERNATINGLEADS.id, AscendStyle.eHINTERHERGEHAMPELT.id)
        val unknownCounts = _db.getAscendCountPerYear(AscendStyle.eUNKNOWN.id)
        val otherCounts = followCounts.map { followCount ->
            ObjectCount(followCount.year, followCount.count + unknownCounts.first {
                it.year == followCount.year
            }.count)
        }
        return Statistic(
            _ctx.getString(R.string.ascends_per_year),
            listOf(
                _ctx.getString(R.string.follow) + "/" + _ctx.getString(R.string.others),
                _ctx.getString(R.string.lead),
                _ctx.getString(R.string.solo)
            ),
            otherCounts.mapIndexed { idx, ascendCount ->
                val yearLabel = if (ascendCount.year == 0) "" else ascendCount.year.toString()
                try {
                    yearLabel to listOf(
                        otherCounts[idx].count,
                        leadCounts[idx].count,
                        soloCounts[idx].count
                    )
                } catch (e: IndexOutOfBoundsException) {
                    // According to the SQL query in getAscendCountPerYear() Dao-API,
                    // this should never happen.
                    yearLabel to listOf(0, 0, 0)
                }
            }.toMap()
        )
    }

    fun generateMostFrequentPartnersStatistic(): Statistic {
        val statsTitle = _ctx.getString(R.string.ascends_per_partner)
        val ascendCountsPerPartner = StatisticUtils.getAscendCountsPerPartner(
            _db.getAscendsBelowStyleId(AscendStyle.eBOTCHED.id)
        )
        val allData = _db.getPartners().sortedBy {
            ascendCountsPerPartner.get(it.id, 0)
        }.associate { partner ->
            partner.name.orEmpty() to listOf(ascendCountsPerPartner.get(partner.id))
        }
        // FIXME: This limitation of entries is a temporary fix for issue #489.
        val limitedData = allData.entries.toList().takeLast(99).associate { it.key to it.value }
        return Statistic(
            statsTitle,
            listOf(statsTitle), // not used because only single-stack
            limitedData
        )
    }

    fun generateNewlyCollectedRockCountsStatistic(): Statistic {
        val considerOnlyLeads = _customSettings.getBoolean(_ctx.getString(R.string.pref_key_count_only_leads),
            _ctx.resources.getBoolean(R.bool.pref_default_count_only_leads))
        val startStyleId = if (considerOnlyLeads)
                AscendStyle.eSOLO.id
            else
                AscendStyle.eUNKNOWN.id
        val endStyleId = if (considerOnlyLeads)
                AscendStyle.eHOCHGESCHLEUDERT.id
            else
                AscendStyle.eHINTERHERGEHAMPELT.id
        val rockCounter = RockCounter(RockCounterConfig.generate(_ctx, _customSettings))
        val statsTitle = _ctx.getString(R.string.newly_collected_rocks_per_year)
        return Statistic(
            statsTitle,
            listOf(statsTitle),
            _db.getNewlyAscendedRockCountsPerYear(startStyleId, endStyleId, rockCounter.getConsideredRockTypes(), rockCounter.getExcludedRockStates()).associate { rockCount ->
                val yearLabel = if (rockCount.year == 0) "" else rockCount.year.toString()
                yearLabel to listOf(rockCount.count)
            }
        )
    }

}
