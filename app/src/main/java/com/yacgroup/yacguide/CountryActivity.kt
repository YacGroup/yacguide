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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.activity_properties.*
import com.yacgroup.yacguide.list_adapters.CountryViewAdapter
import com.yacgroup.yacguide.list_adapters.SwipeConfig
import com.yacgroup.yacguide.list_adapters.SwipeController
import com.yacgroup.yacguide.network.CountryAndRegionParser
import com.yacgroup.yacguide.utils.IntentConstants

class CountryActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: CountryViewAdapter
    private lateinit var _updateHandler: UpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _updateHandler = UpdateHandler(this, CountryAndRegionParser(db))
        properties = arrayListOf(
            RouteSearchable(this),
            RockSearchable(this),
            AscentFilterable(this)
        )

        val listView = findViewById<RecyclerView>(R.id.tableRecyclerView)
        _viewAdapter = CountryViewAdapter(this, customSettings) { countryName -> _onCountrySelected(countryName) }
        listView.adapter = _viewAdapter

        val swipeRightConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorEdit),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_push_pin_24)!!
        ) { pos ->
            _onPinningGesture(pos, R.string.country_already_pinned) { pinnedCountries, countryName ->
                _pinCountry(pinnedCountries, countryName)
            }
        }
        val swipeLeftConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorDelete),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_stroked_pin_24)!!
        ) { pos ->
            _onPinningGesture(pos, R.string.country_not_yet_pinned) { pinnedCountries, countryName ->
                _unpinCountry(pinnedCountries, countryName)
            }
        }
        val swipeController = SwipeController(swipeRightConfig, swipeLeftConfig)
        ItemTouchHelper(swipeController).attachToRecyclerView(listView)

        WhatsNewInfo(this).let {
            if (it.checkForVersionUpdate()) {
                it.showDialog()
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_table

    override fun displayContent() {
        setTitle(R.string.app_name)
        val countries = db.getCountries()
        val pinnedCountries = customSettings.getStringSet(getString(R.string.pinned_countries), emptySet()).orEmpty()
        _viewAdapter.submitList(countries.sortedBy { !pinnedCountries.contains(it.name) })
        _updateHandler.configureDownloadButton(countries.isEmpty()){ displayContent() }
    }

    private fun _onCountrySelected(countryName: String) {
        startActivity(Intent(this@CountryActivity, RegionActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRegion.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, countryName)
        })
    }

    private inline fun _onPinningGesture(position: Int, errorHintResource: Int, storePinning: (MutableSet<String>, String) -> Boolean) {
        val countryName = _viewAdapter.getItemAt(position).name
        val pinnedCountries = customSettings.getStringSet(getString(R.string.pinned_countries), emptySet()).orEmpty().toMutableSet()
        if (!storePinning(pinnedCountries, countryName)) {
            Toast.makeText(this, errorHintResource, Toast.LENGTH_SHORT).show()
            _viewAdapter.notifyItemChanged(position)
        }
    }

    private fun _pinCountry(pinnedCountries: MutableSet<String>, countryName: String): Boolean {
        val countryUnpinned = !pinnedCountries.contains(countryName)
        if (countryUnpinned) {
            pinnedCountries.add(countryName)
            _storePinnedCountriesAndUpdateCountryList(pinnedCountries, R.string.country_pinned)
        }
        return countryUnpinned
    }

    private fun _unpinCountry(pinnedCountries: MutableSet<String>, countryName: String) : Boolean {
        val countryPinned = pinnedCountries.contains(countryName)
        if (countryPinned) {
            pinnedCountries.remove(countryName)
            _storePinnedCountriesAndUpdateCountryList(pinnedCountries, R.string.country_unpinned)
        }
        return countryPinned
    }

    private fun _storePinnedCountriesAndUpdateCountryList(pinnedCountries: MutableSet<String>, successHintResource: Int) {
        val editor = customSettings.edit()
        editor.putStringSet(getString(R.string.pinned_countries), pinnedCountries)
        editor.apply()
        _viewAdapter.submitList(
            db.getCountries().sortedBy { !pinnedCountries.contains(it.name) }) {
            _viewAdapter.notifyDataSetChanged()
            Toast.makeText(this, successHintResource, Toast.LENGTH_SHORT).show()
        }
    }
}