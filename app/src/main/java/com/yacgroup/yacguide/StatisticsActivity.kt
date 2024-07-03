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
import com.yacgroup.yacguide.list_adapters.StatisticsViewAdapter
import com.yacgroup.yacguide.statistics.StatisticGenerator

class StatisticsActivity : BaseNavigationActivity() {

    private lateinit var _statsGenerator: StatisticGenerator
    private lateinit var _viewAdapter: StatisticsViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.menu_statistics)

        _statsGenerator = StatisticGenerator(this)
        _viewAdapter = StatisticsViewAdapter(getColor(R.color.colorPrimary).toColor())
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_statistics

    override fun onResume() {
        super.onResume()
        _displayContent()
    }

    private fun _displayContent() {
        val statistics = listOf(
            _statsGenerator.generateAscendCountsStatistic(),
            _statsGenerator.generateMostFrequentPartnersStatistic()
        )
        _viewAdapter.submitList(statistics)
    }
}
