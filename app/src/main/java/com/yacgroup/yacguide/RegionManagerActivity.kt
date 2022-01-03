/*
 * Copyright (C) 2021 Axel Paetzold
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
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.yacgroup.yacguide.database.Country
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.network.CountryParser
import com.yacgroup.yacguide.network.RegionParser
import com.yacgroup.yacguide.network.SectorParser
import com.yacgroup.yacguide.utils.WidgetUtils

class RegionManagerActivity : BaseNavigationActivity() {

    private lateinit var _db: DatabaseWrapper
    private lateinit var _regionParser: RegionParser
    private lateinit var _sectorParser: SectorParser
    private lateinit var _updateHandler: UpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _db = DatabaseWrapper(this)
        _regionParser = RegionParser(_db, "")
        _sectorParser = SectorParser(_db, 0)
        _updateHandler = UpdateHandler(this, _sectorParser)

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

    override fun getLayoutId() = R.layout.activity_table

    private fun _displayContent() {
        this.setTitle(R.string.menu_region_manager)
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

        var currentCountryName = ""
        val regions = _db.getNonEmptyRegions() // already sorted by country name
        for (region in regions) {
            if (!region.country.equals(currentCountryName)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = region.country.orEmpty(),
                    textSizeDp = WidgetUtils.infoFontSizeDp,
                    bgColor = WidgetUtils.tourHeaderColor))
                layout.addView(WidgetUtils.createHorizontalLine(this, 5))
                currentCountryName = region.country.orEmpty()
            }

            val innerLayout = WidgetUtils.createCommonRowLayout(this,
                textLeft = region.name.orEmpty()
            )

            val updateButton = ImageButton(this).apply {
                id = View.generateViewId()
                setImageResource(android.R.drawable.stat_notify_sync)
                setOnClickListener {
                    _updateHandler.setJsonParser(_sectorParser)
                    _sectorParser.setRegionId(region.id)
                    _sectorParser.setRegionName(region.name.orEmpty())
                    _updateHandler.update()
                }
            }
            innerLayout.addView(updateButton)

            val deleteButton = ImageButton(this).apply {
                id = View.generateViewId()
                setImageResource(android.R.drawable.ic_menu_delete)
                setOnClickListener {
                    _updateHandler.delete {
                        _db.deleteSectorsRecursively(region.id)
                        _displayContent()
                    }
                }
            }
            innerLayout.addView(deleteButton)

            val buttonWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f,
                    resources.displayMetrics).toInt()
            (deleteButton.layoutParams as RelativeLayout.LayoutParams).let {
                it.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
                it.height = buttonWidthPx
                it.width = it.height
            }

            (updateButton.layoutParams as RelativeLayout.LayoutParams).let {
                it.addRule(RelativeLayout.LEFT_OF, deleteButton.id)
                it.height = buttonWidthPx
                it.width = it.height
            }

            layout.addView(innerLayout)
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    private fun _updateAll() {
        // We need to update regions recursively since update() is asynchronous.
        _updateHandler.setJsonParser(CountryParser(_db))
        _updateHandler.update({
            _updateHandler.setJsonParser(_regionParser)
            val countries = _db.getNonEmptyCountries().toMutableSet()
            _updateNextCountry(countries)
        }, true)
    }

    private fun _updateNextCountry(countries: MutableSet<Country>) {
        try {
            val nextCountry = countries.first()
            countries.remove(nextCountry)
            _regionParser.setCountryName(nextCountry.name)
            _updateHandler.update({ _updateNextCountry(countries) }, true)
        } catch (e: NoSuchElementException) {
            _updateHandler.setJsonParser(_sectorParser)
            val regions = _db.getNonEmptyRegions().toMutableSet()
            _updateNextRegion(regions)
        }
    }

    private fun _updateNextRegion(regions: MutableSet<Region>) {
        try {
            val nextRegion = regions.first()
            regions.remove(nextRegion)
            _sectorParser.setRegionId(nextRegion.id)
            _sectorParser.setRegionName(nextRegion.name.orEmpty())
            _updateHandler.update({ _updateNextRegion(regions) }, true)
        } catch (e: NoSuchElementException) {
            _updateHandler.finish()
        }
    }
}
