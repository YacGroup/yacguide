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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R

data class Statistic(
    val name: String
)

class StatisticsViewAdapter : ListAdapter<Statistic, RecyclerView.ViewHolder>(StatisticDiffCallback()) {

    inner class StatisticViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _statisticNameLayout = view.findViewById<LinearLayout>(R.id.titleLayout)
        private val _statisticName = view.findViewById<TextView>(R.id.titleTextView)
        private val _statisticChartLayout = view.findViewById<RelativeLayout>(R.id.expandedContentLayout)

        fun bind(stat: Statistic) {
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
}

class StatisticDiffCallback : DiffUtil.ItemCallback<Statistic>() {
    override fun areItemsTheSame(oldStat: Statistic, newStat: Statistic): Boolean {
        return oldStat.name == newStat.name
    }
    override fun areContentsTheSame(oldStat: Statistic, newStat: Statistic): Boolean {
        return oldStat == newStat
    }
}