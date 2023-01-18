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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.list_adapters.BaseViewAdapter
import com.yacgroup.yacguide.list_adapters.BaseViewItem
import com.yacgroup.yacguide.list_adapters.SwipeConfig
import com.yacgroup.yacguide.list_adapters.SwipeController
import com.yacgroup.yacguide.network.CountryAndRegionParser
import com.yacgroup.yacguide.network.SectorParser

class RegionManagerActivity : BaseNavigationActivity() {

    private lateinit var _viewAdapter: BaseViewAdapter
    private lateinit var _db: DatabaseWrapper
    private lateinit var _countryAndRegionParser: CountryAndRegionParser
    private lateinit var _sectorParser: SectorParser
    private lateinit var _updateHandler: UpdateHandler
    private lateinit var _customSettings: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _db = DatabaseWrapper(this)
        _countryAndRegionParser = CountryAndRegionParser(_db)
        _sectorParser = SectorParser(_db)
        _updateHandler = UpdateHandler(this, _sectorParser)
        _customSettings = getSharedPreferences(getString(R.string.preferences_filename), Context.MODE_PRIVATE)

        _viewAdapter = BaseViewAdapter(_withItemFooters = true) { regionId -> _selectDefaultRegion(regionId) }
        val listView = findViewById<RecyclerView>(R.id.tableRecyclerView)
        listView.adapter = _viewAdapter

        val swipeRightConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorSync),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_sync_24)!!
        ) { pos ->
            _updateRegionListAndDB(pos) { region ->
                _updateHandler.setJsonParser(_sectorParser)
                _updateHandler.update(ClimbingObjectUId(region.id, region.name.orEmpty()))
            }
        }
        val swipeLeftConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorDelete),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24)!!
        ) { pos ->
            _updateRegionListAndDB(pos) { region ->
                _updateHandler.delete {
                    _db.deleteSectorsRecursively(region.id)
                    _displayContent()
                }
            }
        }
        val swipeController = SwipeController(swipeRightConfig, swipeLeftConfig)
        ItemTouchHelper(swipeController).attachToRecyclerView(listView)

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
        val defaultRegionId = _customSettings.getInt(getString(R.string.default_region_key), R.integer.unknown_id)
        var currentCountryName = ""
        val regions = _db.getNonEmptyRegions() // already sorted by country name
        val regionItemList = mutableListOf<BaseViewItem>()

        regions.forEach { region ->
            if (!region.country.equals(currentCountryName)) {
                currentCountryName = region.country.orEmpty()
                regionItemList.add(BaseViewItem(
                    id = 0,
                    textLeft = currentCountryName,
                    backgroundColor = ContextCompat.getColor(this, R.color.colorSecondary),
                    isHeader = true))
            }
            var regionName = region.name.orEmpty()
            var backgroundResource = R.color.colorSecondaryLight
            if (region.id == defaultRegionId) {
                regionName = "${getString(R.string.startup_arrow)} $regionName"
                backgroundResource = R.color.colorAccentLight
            }
            regionItemList.add(BaseViewItem(
                id = region.id,
                textLeft = regionName,
                backgroundColor = ContextCompat.getColor(this, backgroundResource)))
        }

        _viewAdapter.submitList(regionItemList)
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

    private fun _selectDefaultRegion(regionId: Int) {
        val key = getString(R.string.default_region_key)
        val defaultRegionId = _customSettings.getInt(key, R.integer.unknown_id)
        val newDefaultRegionId = if (regionId == defaultRegionId) {
            Toast.makeText(this, R.string.default_region_reset, Toast.LENGTH_SHORT).show()
            R.integer.unknown_id
        } else {
            Toast.makeText(this, R.string.default_region_set, Toast.LENGTH_SHORT).show()
            regionId
        }
        _customSettings.edit().apply {
            putInt(key, newDefaultRegionId)
            apply()
        }
        _displayContent()
    }

    private inline fun _updateRegionListAndDB(position: Int, dbAction: (region: Region) -> Unit) {
        val item = _viewAdapter.getItemAt(position)
        _db.getRegion(item.id)?.let { dbAction(it) }
        _viewAdapter.notifyItemChanged(position)
    }
}
