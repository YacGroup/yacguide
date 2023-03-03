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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.activity_properties.*
import com.yacgroup.yacguide.database.Sector
import com.yacgroup.yacguide.list_adapters.*
import com.yacgroup.yacguide.network.SectorParser
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils

class SectorActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: ListViewAdapter<Sector>
    private lateinit var _updateHandler: UpdateHandler
    private lateinit var _rockCounter: RockCounter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _updateHandler = UpdateHandler(this, SectorParser(db))
        properties = arrayListOf(
            RouteSearchable(this),
            RockSearchable(this),
            AscentFilterable(this)
        )
        _rockCounter = RockCounter(RockCounterConfig.generate(this, customSettings))

        _viewAdapter = ListViewAdapter(ItemDiffCallback(
            _areItemsTheSame = { sector1, sector2 -> sector1.id == sector2.id }
        )) { sector -> ListItem(
            backgroundColor = visualUtils.defaultBgColor,
            mainText = _getSectorMainText(sector),
            subText = ParserUtils.decodeObjectNames(sector.name).second,
            onClick = { _onSectorSelected(sector) })
        }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter

        if (intent.getBooleanExtra(IntentConstants.SHOW_WHATS_NEW, false)) {
            WhatsNewInfo(this).showDialog()
        }
    }

    override fun getLayoutId() = R.layout.activity_sector

    override fun showComments(v: View) {
        showRegionComments(activityLevel.parentUId.id)
    }

    override fun displayContent() {
        this.title = activityLevel.parentUId.name
        val sectors = db.getSectors(activityLevel.parentUId.id)
        _viewAdapter.submitList(sectors)
        _updateHandler.configureDownloadButton(
            enabled = sectors.isEmpty(),
            climbingObjectUId = ClimbingObjectUId(activityLevel.parentUId.id, activityLevel.parentUId.name)
        ) { displayContent() }
    }

    private fun _getSectorMainText(sector: Sector): Pair<String, String> {
        val sectorName = ParserUtils.decodeObjectNames(sector.name)
        val rockCount = if (_rockCounter.isApplicable()) {
                val rockCount = _rockCounter.calculateRockCount(db.getRocksForSector(sector.id))
                "(${rockCount.ascended} / ${rockCount.total})"
            } else
                ""
        return Pair(sectorName.first, rockCount)
    }

    private fun _onSectorSelected(sector: Sector) {
        startActivity(Intent(this@SectorActivity, RockActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRock.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, sector.id)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, sector.name)
        })
    }
}
