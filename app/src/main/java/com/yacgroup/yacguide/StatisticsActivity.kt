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

package com.yacgroup.yacguide

import android.os.Bundle
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.BarEntry
import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.list_adapters.Statistic
import com.yacgroup.yacguide.list_adapters.StatisticsViewAdapter
import com.yacgroup.yacguide.utils.AscendStyle

class StatisticsActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _viewAdapter: StatisticsViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.menu_statistics)

        _db = DatabaseWrapper(this)
        _viewAdapter = StatisticsViewAdapter(getColor(R.color.colorPrimary).toColor())
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_statistics

    override fun onResume() {
        super.onResume()
        _displayContent()
    }

    private fun _displayContent() {
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
        val statistics = listOf(
            Statistic(
                getString(R.string.ascends_per_year),
                listOf(
                    getString(R.string.others),
                    getString(R.string.lead),
                    getString(R.string.solo)
                ),
                soloCounts.mapIndexed { idx, ascendCount ->
                    val yearLabel = if (ascendCount.year == 0) "" else ascendCount.year.toString()
                    yearLabel to BarEntry(idx.toFloat(), floatArrayOf(
                        otherCounts[idx].count.toFloat(),
                        leadCounts[idx].count.toFloat(),
                        soloCounts[idx].count.toFloat()
                    ))
                }.toMap()
            )
        )
        _viewAdapter.submitList(statistics)
    }
}
