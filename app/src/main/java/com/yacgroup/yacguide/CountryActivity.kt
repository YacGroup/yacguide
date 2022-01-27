/*
 * Copyright (C) 2019 Axel Paetzold
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
import com.yacgroup.yacguide.list_adapters.CountryViewAdapter
import com.yacgroup.yacguide.network.CountryParser
import com.yacgroup.yacguide.utils.IntentConstants

class CountryActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: CountryViewAdapter
    private lateinit var _updateHandler: UpdateHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _updateHandler = UpdateHandler(this, CountryParser(db))
        properties = arrayListOf(RockSearchable(this), AscentFilterable(this))

        _viewAdapter = CountryViewAdapter { countryName -> _onCountrySelected(countryName) }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter

        WhatsNewInfo(this).let {
            if (it.checkForVersionUpdate()) {
                it.showDialog()
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_country

    override fun displayContent() {
        setTitle(R.string.app_name)
        val countries = db.getCountries()
        _viewAdapter.submitList(countries)
        _updateHandler.configureDownloadButton(countries.isEmpty()){ displayContent() }
    }

    private fun _onCountrySelected(countryName: String) {
        val intent = Intent(this@CountryActivity, RegionActivity::class.java)
        intent.putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eRegion.value)
        intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, countryName)
        startActivity(intent)
    }
}
