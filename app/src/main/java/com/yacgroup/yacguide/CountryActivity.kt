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
import com.yacgroup.yacguide.database.Country
import com.yacgroup.yacguide.list_adapters.*
import com.yacgroup.yacguide.network.CountryAndRegionParser
import com.yacgroup.yacguide.utils.IntentConstants

class CountryActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: ListViewAdapter<Country>
    private lateinit var _updateHandler: UpdateHandler
    private lateinit var _pinnedCountries: Set<String>

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _updateHandler = UpdateHandler(this, CountryAndRegionParser(db))
        properties = arrayListOf(
            RouteSearchable(this),
            RockSearchable(this),
            AscentFilterable(this)
        )
        _pinnedCountries = customSettings.getStringSet(getString(R.string.pref_key_pinned_countries), emptySet()).orEmpty()

        val listView = findViewById<RecyclerView>(R.id.tableRecyclerView)
        _viewAdapter = ListViewAdapter(ItemDiffCallback(
            _areItemsTheSame = { country1, country2 -> country1.name == country2.name },
            _areContentsTheSame = { country1, country2 -> country1 == country2 }
        )) { country -> ListItem(
            backgroundColor = _getCountryBackground(country),
            mainText = Pair(country.name, ""),
            onClick = { _onCountrySelected(country) })
        }
        listView.adapter = _viewAdapter

        val swipeRightConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorEdit),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_push_pin_24)!!
        ) { viewHolder ->
            _onPinningGesture(
                viewHolder = viewHolder as ListViewAdapter<Country>.ItemViewHolder,
                errorHintResource = R.string.country_already_pinned
            ) { pinnedCountries, countryName ->
                _pinCountry(pinnedCountries, countryName)
            }
        }
        val swipeLeftConfig = SwipeConfig(
            color = ContextCompat.getColor(this, R.color.colorDelete),
            background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_stroked_pin_24)!!
        ) { viewHolder ->
            _onPinningGesture(
                viewHolder = viewHolder as ListViewAdapter<Country>.ItemViewHolder,
                errorHintResource = R.string.country_not_yet_pinned
            ) { pinnedCountries, countryName ->
                _unpinCountry(pinnedCountries, countryName)
            }
        }
        val swipeController = SwipeController(swipeRightConfig, swipeLeftConfig)
        ItemTouchHelper(swipeController).attachToRecyclerView(listView)

        if (intent.getBooleanExtra(IntentConstants.SHOW_WHATS_NEW, false)) {
            WhatsNewInfo(this).showDialog()
        }
    }

    override fun getLayoutId() = R.layout.activity_table

    override fun displayContent() {
        setTitle(R.string.app_name)
        val countries = db.getCountries()
        _viewAdapter.submitList(countries.sortedBy { !_pinnedCountries.contains(it.name) })
        _updateHandler.configureDownloadButton(
            enabled = countries.isEmpty(),
            climbingObjectUId = ClimbingObjectUId(0, getString(R.string.countries_and_regions))
        ){ displayContent() }
    }

    private fun _getCountryBackground(country: Country): Int {
        return if (_pinnedCountries.contains(country.name))
                visualUtils.accentBgColor
            else
                visualUtils.defaultBgColor
    }

    private fun _onCountrySelected(country: Country) {
        startActivity(Intent(this@CountryActivity, RegionActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRegion.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, country.name)
        })
    }

    private inline fun _onPinningGesture(
        viewHolder: ListViewAdapter<Country>.ItemViewHolder,
        errorHintResource: Int,
        storePinning: (MutableSet<String>, String) -> Boolean
    ) {
        val pinnedCountries = customSettings.getStringSet(getString(R.string.pref_key_pinned_countries), emptySet()).orEmpty().toMutableSet()
        viewHolder.getItem()?.let { country ->
            if (!storePinning(pinnedCountries, country.name)) {
                Toast.makeText(this, errorHintResource, Toast.LENGTH_SHORT).show()
                _viewAdapter.notifyItemChanged(viewHolder.adapterPosition)
            }
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
        editor.putStringSet(getString(R.string.pref_key_pinned_countries), pinnedCountries)
        editor.apply()
        _viewAdapter.submitList(
            db.getCountries().sortedBy { !pinnedCountries.contains(it.name) }) {
            _viewAdapter.notifyDataSetChanged()
            Toast.makeText(this, successHintResource, Toast.LENGTH_SHORT).show()
        }
        _pinnedCountries = pinnedCountries
    }
}
