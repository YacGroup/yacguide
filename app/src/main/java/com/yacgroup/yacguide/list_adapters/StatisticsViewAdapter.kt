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

package com.yacgroup.yacguide.list_adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.statistics.Statistic
import com.yacgroup.yacguide.utils.AxisFormatter
import com.yacgroup.yacguide.utils.IntValueFormatter

class StatisticsViewAdapter(private val _baseColor: Color) : ListAdapter<Statistic, RecyclerView.ViewHolder>(StatisticDiffCallback()) {

    inner class StatisticViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _statisticNameLayout = view.findViewById<LinearLayout>(R.id.titleLayout)
        private val _statisticName = view.findViewById<TextView>(R.id.titleTextView)
        private val _statisticChartLayout = view.findViewById<RelativeLayout>(R.id.expandedContentLayout)
        private val _chart = view.findViewById<HorizontalBarChart>(R.id.barChart)

        fun bind(stat: Statistic) {
            val dataSet = _generateDataSet(stat)
            with(_chart) {
                data = BarData(dataSet).apply {
                    setValueFormatter(IntValueFormatter())
                }
                layoutParams.height = 100 * stat.data.size
                description.isEnabled = false
                setDrawValueAboveBar(false) // Display values inside the bars
                listOf(axisLeft, axisRight).forEach { axis ->
                    with(axis) {
                        setDrawGridLines(false)
                        axisMinimum = 0f // Let chart start at value 0
                        granularity = 1f // Let chart only display integer labels
                        isGranularityEnabled = true
                    }
                }
                with(xAxis) {
                    valueFormatter = AxisFormatter(stat.data.keys.toList())
                    xAxis.setDrawGridLines(false)
                    xAxis.labelCount = stat.data.size // Do not display more than the number of x values as labels
                    xAxis.granularity = 1f // Do not duplicate labels on zooming to match labelCount
                }
                legend.isEnabled = stat.stackTitles.size > 1
            }
            _statisticNameLayout.setOnClickListener {
                _statisticChartLayout.visibility =
                    if (_statisticChartLayout.visibility == View.GONE)
                        View.VISIBLE
                    else
                        View.GONE
                notifyDataSetChanged()
            }
            _statisticName.text = stat.name
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.expandable_list_item, viewGroup, false)
        return StatisticViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as StatisticViewHolder).bind(getItem(position))
    }

    private fun _generateDataSet(stat: Statistic): BarDataSet {
        val colorOffset = 255f / stat.stackTitles.size
        return BarDataSet(
            stat.data.values.mapIndexed { idx, value ->
                BarEntry(idx.toFloat(), value.map { it.toFloat() }.toFloatArray())
            }, stat.name).apply {
                label = ""
                stackLabels = stat.stackTitles.toTypedArray()
                colors = List(stat.stackTitles.size) { idx ->
                    Color.rgb(_baseColor.red(), _baseColor.green(), 255f - idx * colorOffset)
                }
        }
    }
}

class StatisticDiffCallback : DiffUtil.ItemCallback<Statistic>() {
    override fun areItemsTheSame(oldStat: Statistic, newStat: Statistic): Boolean {
        return oldStat.name == newStat.name
    }
    override fun areContentsTheSame(oldStat: Statistic, newStat: Statistic): Boolean {
        return oldStat == newStat
    }
}