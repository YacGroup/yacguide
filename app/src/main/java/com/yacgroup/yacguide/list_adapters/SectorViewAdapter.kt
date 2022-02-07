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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

class SectorViewAdapter(
    context: Context,
    customSettings: SharedPreferences,
    private val _db: DatabaseWrapper,
    private val _onClick: (Int, String) -> Unit)
    : ListAdapter<Sector, RecyclerView.ViewHolder>(SectorDiffCallback) {

    private val _countSummits = customSettings.getBoolean(
        context.getString(R.string.count_summits),
        context.resources.getBoolean(R.bool.count_summits))
    private val _countMassifs = customSettings.getBoolean(
        context.getString(R.string.count_massifs),
        context.resources.getBoolean(R.bool.count_massifs))
    private val _countBoulders = customSettings.getBoolean(
        context.getString(R.string.count_boulders),
        context.resources.getBoolean(R.bool.count_boulders))
    private val _countCaves = customSettings.getBoolean(
        context.getString(R.string.count_caves),
        context.resources.getBoolean(R.bool.count_caves))
    private val _countUnofficialRocks = customSettings.getBoolean(
        context.getString(R.string.count_unofficial_rocks),
        context.resources.getBoolean(R.bool.count_unofficial_rocks))
    private val _countProhibitedRocks = customSettings.getBoolean(
        context.getString(R.string.count_prohibited_rocks),
        context.resources.getBoolean(R.bool.count_prohibited_rocks))
    private val _countCollapsedRocks = customSettings.getBoolean(
        context.getString(R.string.count_collapsed_rocks),
        context.resources.getBoolean(R.bool.count_collapsed_rocks))
    private val _countOnlyLeads = customSettings.getBoolean(
        context.getString(R.string.count_only_leads),
        context.resources.getBoolean(R.bool.count_only_leads))

    inner class SectorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _mainRightTextView = view.findViewById<TextView>(R.id.mainRightTextView)
        private val _subTextView = view.findViewById<TextView>(R.id.subTextView)

        fun bind(sector: Sector) {
            val sectorName = ParserUtils.decodeObjectNames(sector.name)
            val rocks = _db.getRocksForSector(sector.id)
            _mainLeftTextView.text = sectorName.first
            _mainRightTextView.text = _generateRockCountString(rocks)
            _subTextView.text = sectorName.second
            _listItemLayout.apply {
                setOnClickListener {
                    _onClick(sector.id, sector.name.orEmpty())
                }
            }
        }

        private fun _generateRockCountString(rocks: List<Rock>): String {
            if (!(_countSummits || _countMassifs || _countBoulders || _countCaves)) {
                return ""
            }

            var filteredRocks = rocks
            if (!_countSummits) {
                filteredRocks = filteredRocks.filter{ it.type != Rock.typeSummit && it.type != Rock.typeAlpine }
            }
            if (!_countMassifs) {
                filteredRocks = filteredRocks.filter { it.type != Rock.typeMassif && it.type != Rock.typeStonePit }
            }
            if (!_countBoulders) {
                filteredRocks = filteredRocks.filter { it.type != Rock.typeBoulder }
            }
            if (!_countCaves) {
                filteredRocks = filteredRocks.filter { it.type != Rock.typeCave }
            }

            if (!_countUnofficialRocks) {
                filteredRocks = filteredRocks.filter { it.type != Rock.typeUnofficial }
            }
            if (!_countProhibitedRocks) {
                filteredRocks = filteredRocks.filter { it.status != Rock.statusProhibited }
            }
            if (!_countCollapsedRocks) {
                filteredRocks = filteredRocks.filter { it.status != Rock.statusCollapsed }
            }
            val allRockCount = filteredRocks.size

            filteredRocks = if (_countOnlyLeads) {
                filteredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) }
            } else {
                filteredRocks.filter { AscendStyle.isLead(it.ascendsBitMask) || AscendStyle.isFollow(it.ascendsBitMask) }
            }

            return "(${filteredRocks.size} / $allRockCount)"
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return SectorViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as SectorViewHolder).bind(getItem(position) as Sector)
    }
}

object SectorDiffCallback : DiffUtil.ItemCallback<Sector>() {
    override fun areItemsTheSame(oldSector: Sector, newSector: Sector): Boolean {
        return oldSector.id == newSector.id
    }

    override fun areContentsTheSame(oldSector: Sector, newSector: Sector): Boolean {
        // We need to return false here to always re-draw the list if returning from RockActivity.
        // This is needed to update the sector's rock count
        // since the Sector item itself will not have changed by that.
        return false
    }
}