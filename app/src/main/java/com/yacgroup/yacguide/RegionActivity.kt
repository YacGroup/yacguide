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
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.activity_properties.*
import com.yacgroup.yacguide.list_adapters.RegionViewAdapter
import com.yacgroup.yacguide.utils.IntentConstants

class RegionActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: RegionViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        properties = arrayListOf(
            RouteSearchable(this),
            RockSearchable(this),
            AscentFilterable(this)
        )

        _viewAdapter = RegionViewAdapter(this, customSettings, db) { regionId, regionName -> _onRegionSelected(regionId, regionName) }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_table

    override fun displayContent() {
        this.title = activityLevel.parentUId.name
        val regions = db.getRegions(activityLevel.parentUId.name).sortedBy { region ->
            db.getSectors(region.id).isEmpty()
        }
        _viewAdapter.submitList(regions)
    }

    private fun _onRegionSelected(regionId: Int, regionName: String) {
        startActivity(Intent(this@RegionActivity, SectorActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eSector.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, regionId)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, regionName)
        })
    }
}
