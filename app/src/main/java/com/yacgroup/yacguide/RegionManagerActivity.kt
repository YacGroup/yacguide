/*
 * Copyright (C) 2021 - 2023 Axel Paetzold
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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.list_adapters.*
import com.yacgroup.yacguide.network.CountryAndRegionParser
import com.yacgroup.yacguide.network.SectorParser
import com.yacgroup.yacguide.utils.VisualUtils

class RegionManagerActivity : BaseNavigationActivity() {

    private lateinit var _visualUtils: VisualUtils
    private lateinit var _viewAdapter: SectionViewAdapter<Region>
    private lateinit var _db: DatabaseWrapper
    private lateinit var _countryAndRegionParser: CountryAndRegionParser
    private lateinit var _sectorParser: SectorParser
    private lateinit var _updateHandler: UpdateHandler
    private lateinit var _customSettings: SharedPreferences
    private var _defaultRegionId: Int = 0

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _db = DatabaseWrapper(this)
        _countryAndRegionParser = CountryAndRegionParser(_db)
        _sectorParser = SectorParser(_db)
        _updateHandler = UpdateHandler(this, _sectorParser)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)
        _defaultRegionId = _customSettings.getInt(
            getString(R.string.default_region_key),
            resources.getInteger(R.integer.default_region_id)
        )

        _visualUtils = VisualUtils(this)
        val swipeRightConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorSync),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_sync_24)!!
        ) { viewHolder ->
            _updateRegionListAndDB(viewHolder as ListViewAdapter<Region>.ItemViewHolder) { region ->
                _updateHandler.setJsonParser(_sectorParser)
                _updateHandler.update(ClimbingObjectUId(region.id, region.name.orEmpty()))
            }
        }
        val swipeLeftConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorDelete),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24)!!
        ) { viewHolder ->
            _updateRegionListAndDB(viewHolder as ListViewAdapter<Region>.ItemViewHolder) { region ->
                _updateHandler.delete {
                    _db.deleteSectorsRecursively(region.id)
                    _displayContent()
                }
            }
        }
        _viewAdapter = SectionViewAdapter(_visualUtils, SwipeController(swipeRightConfig, swipeLeftConfig)) {
            ListViewAdapter(ItemDiffCallback(
                _areItemsTheSame = { region1, region2 -> region1.id == region2.id },
                _areContentsTheSame = { region1, region2 -> region1 == region2 }
            )) { region -> ListItem(
                backgroundColor = _getRegionBackground(region),
                mainText = _getRegionMainText(region),
                onClick = { _selectDefaultRegion(region) })
            }
        }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter

        _displayContent()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.update_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        _updateAll()
        return true
    }

    override fun getLayoutId() = R.layout.activity_region_manager

    private fun _displayContent() {
        this.setTitle(R.string.menu_region_manager)

        val sectionViews = _db.getNonEmptyRegions().groupBy { it.country }.map {
            SectionViewItem(
                title = Pair(it.key.orEmpty(), ""),
                elements = it.value
            )
        }
        _viewAdapter.submitList(sectionViews)
    }

    private fun _updateAll() {
        // We need to update regions recursively since update() is asynchronous.
        _updateHandler.setJsonParser(_countryAndRegionParser)
        _updateHandler.update(
            climbingObjectUId = ClimbingObjectUId(0, getString(R.string.countries_and_regions)),
            onUpdateFinished = {
                _updateHandler.setJsonParser(_sectorParser)
                _updateNextRegion(_db.getNonEmptyRegions().toMutableSet()) },
            isRecurring = true)
    }

    private fun _updateNextRegion(regions: MutableSet<Region>) {
        try {
            val nextRegion = regions.first()
            regions.remove(nextRegion)
            _updateHandler.update(
                climbingObjectUId = ClimbingObjectUId(nextRegion.id, nextRegion.name.orEmpty()),
                onUpdateFinished = { _updateNextRegion(regions) },
                isRecurring = true)
        } catch (e: NoSuchElementException) {
            _updateHandler.finish()
        }
    }

    private fun _getRegionBackground(region: Region): Int {
        return if (region.id == _defaultRegionId)
                _visualUtils.accentBgColor
            else
                _visualUtils.defaultBgColor
    }

    private fun _getRegionMainText(region: Region): Pair<String, String> {
        return if (region.id == _defaultRegionId)
                Pair("${_visualUtils.startupArrow} ${region.name}", "")
            else
                Pair(region.name.orEmpty(), "")
    }

    private fun _selectDefaultRegion(region: Region) {
        val key = getString(R.string.default_region_key)
        val invalidId = resources.getInteger(R.integer.default_region_id)
        _defaultRegionId = if (region.id == _defaultRegionId) {
            Toast.makeText(this, R.string.default_region_reset, Toast.LENGTH_SHORT).show()
            invalidId
        } else {
            Toast.makeText(this, R.string.default_region_set, Toast.LENGTH_SHORT).show()
            region.id
        }
        _customSettings.edit().apply {
            putInt(key, _defaultRegionId)
            apply()
        }
        // we need to update all sections since you may switch between different countries for default region
        _viewAdapter.notifyDataSetChanged()
    }

    private inline fun _updateRegionListAndDB(
        viewHolder: ListViewAdapter<Region>.ItemViewHolder,
        dbAction: (region: Region) -> Unit
    ) {
        viewHolder.getItem()?.let { region ->
            _db.getRegion(region.id)?.let { dbAction(it) }
        }
        viewHolder.getParentAdapter().notifyItemChanged(viewHolder.adapterPosition)
    }
}
