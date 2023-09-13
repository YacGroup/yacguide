/*
 * Copyright (C) 2023 Axel Paetzold
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
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.AscendCount
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.utils.AscendStyle

class StatisticGenerator(private val _ctx: Context) {

    private val _db = DatabaseWrapper(_ctx)

    fun generateAscendCountsStatistic(): Statistic {
        // The following counts contain all years, even if there was no ascent of the according style.
        // This means that the list sizes are all equal.
        val soloCounts = _db.getAscendCountPerYear(AscendStyle.eSOLO.id)
        val leadCounts = _db.getAscendCountPerYear(AscendStyle.eONSIGHT.id, AscendStyle.eHOCHGESCHLEUDERT.id)
        val followCounts = _db.getAscendCountPerYear(AscendStyle.eALTERNATINGLEADS.id, AscendStyle.eHINTERHERGEHAMPELT.id)
        val unknownCounts = _db.getAscendCountPerYear(AscendStyle.eUNKNOWN.id)
        val otherCounts = followCounts.map { followCount ->
            AscendCount(followCount.year, followCount.count + unknownCounts.first {
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

}