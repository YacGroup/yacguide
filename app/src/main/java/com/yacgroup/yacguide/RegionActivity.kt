/*
 * Copyright (C) 2019, 2022, 2023 Axel Paetzold
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

import android.content.Intent
import android.os.Bundle
import com.yacgroup.yacguide.activity_properties.*
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.databinding.ActivityTableBinding
import com.yacgroup.yacguide.list_adapters.*
import com.yacgroup.yacguide.utils.IntentConstants

class RegionActivity : TableActivityWithOptionsMenu<ActivityTableBinding>() {

    private lateinit var _viewAdapter: ListViewAdapter<Region>
    private lateinit var _rockCounter: RockCounter

    override fun getViewBinding() = ActivityTableBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        properties = arrayListOf(
            RouteSearchable(this),
            RockSearchable(this),
            AscentFilterable(this)
        )
        _rockCounter = RockCounter(RockCounterConfig.generate(this, customSettings))

        _viewAdapter = ListViewAdapter(ItemDiffCallback(
            _areItemsTheSame = { region1, region2 -> region1.id == region2.id }
        )) { region -> ListItem(
            backgroundColor = _getRegionBackground(region),
            mainText = _getRegionMainText(region),
            subText = _getRegionSubText(region),
            onClick = { _onRegionSelected(region) })
        }
        activityViewBinding.layoutListViewContent.tableRecyclerView.adapter = _viewAdapter
    }

    override fun displayContent() {
        this.title = activityLevel.parentUId.name
        val regions = db.getRegions(activityLevel.parentUId.name).sortedBy { region ->
            db.getSectors(region.id).isEmpty()
        }
        _viewAdapter.submitList(regions)
    }

    private fun _getRegionBackground(region: Region): Int {
        return if (db.getSectors(region.id).isNotEmpty())
                visualUtils.accentBgColor
            else
                visualUtils.defaultBgColor
    }

    private fun _getRegionMainText(region: Region): Pair<String, String> {
        val icon = if (db.getSectors(region.id).isNotEmpty())
                visualUtils.tickedBoxIcon
            else
                visualUtils.emptyBoxIcon
        return Pair(region.name.orEmpty(), icon)
    }

    private fun _getRegionSubText(region: Region): String {
        return if (_rockCounter.isApplicable()) {
                val rockCount = _rockCounter.calculateRockCount(db.getRocksForRegion(region.id))
                "(${rockCount.ascended} / ${rockCount.total})"
            } else
                ""
    }

    private fun _onRegionSelected(region: Region) {
        startActivity(Intent(this@RegionActivity, SectorActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eSector.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, region.id)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, region.name)
        })
    }
}
