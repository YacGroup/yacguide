/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.activity_properties.*
import com.yacgroup.yacguide.list_adapters.SectorViewAdapter
import com.yacgroup.yacguide.network.SectorParser
import com.yacgroup.yacguide.utils.IntentConstants

class SectorActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: SectorViewAdapter
    private lateinit var _updateHandler: UpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _updateHandler = UpdateHandler(this, SectorParser(db, activityLevel.parentId))
        properties = arrayListOf(RockSearchable(this), AscentFilterable(this))

        _viewAdapter = SectorViewAdapter(this, customSettings, db) { sectorId, sectorName -> _onSectorSelected(sectorId, sectorName) }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_sector

    override fun showComments(v: View) {
        showRegionComments(activityLevel.parentId)
    }

    override fun displayContent() {
        this.title = activityLevel.parentName
        val sectors = db.getSectors(activityLevel.parentId)
        _viewAdapter.submitList(sectors)
        _updateHandler.configureDownloadButton(sectors.isEmpty()) { displayContent() }
    }

    private fun _onSectorSelected(sectorId: Int, sectorName: String) {
        startActivity(Intent(this@SectorActivity, RockActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRock.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, sectorId)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, sectorName)
        })
    }
}
