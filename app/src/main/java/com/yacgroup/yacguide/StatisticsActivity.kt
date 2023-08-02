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
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.BarEntry
import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.list_adapters.Statistic
import com.yacgroup.yacguide.list_adapters.StatisticsViewAdapter

class StatisticsActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _viewAdapter: StatisticsViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.menu_statistics)

        _db = DatabaseWrapper(this)
        _viewAdapter = StatisticsViewAdapter()
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_statistics

    override fun onResume() {
        super.onResume()
        _displayContent()
    }

    private fun _displayContent() {
        // Placeholders:
        val statistics = listOf(
            Statistic("Statistik 1", listOf("Stat 1.1", "Stat 1.2"), mapOf(
                "2002" to BarEntry(0f, floatArrayOf(10f, 3f)),
                "2003" to BarEntry(1f, floatArrayOf(17f, 4f)),
                "2020" to BarEntry(2f, floatArrayOf(22f, 5f))
            )),
            Statistic("Statistik 2", listOf(""), mapOf(
                "2002" to BarEntry(0f, 10f),
                "2003" to BarEntry(1f, 17f),
                "2020" to BarEntry(2f, 22f),
                "2021" to BarEntry(3f, 45f),
                "2022" to BarEntry(4f, 17f),
                "2023" to BarEntry(5f, 62f)
            ))
        )
        _viewAdapter.submitList(statistics)
    }
}
