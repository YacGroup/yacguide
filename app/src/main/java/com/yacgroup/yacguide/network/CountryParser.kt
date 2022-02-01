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

package com.yacgroup.yacguide.network

import com.yacgroup.yacguide.database.Country
import com.yacgroup.yacguide.database.DatabaseWrapper

import org.json.JSONArray
import org.json.JSONException
import java.util.*

class CountryParser(private val _db: DatabaseWrapper) : JSONWebParser() {

    private val _countries = mutableListOf<Country>()

    override fun initNetworkRequests() {
        networkRequests = LinkedList(listOf(
                NetworkRequest(
                    NetworkRequestUId(RequestType.COUNTRY_DATA, 0),
                    "${baseUrl}jsonland.php?app=yacguide")
        ))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestUId, json: String) {
        val jsonCountries = JSONArray(json)
        for (i in 0 until jsonCountries.length()) {
            val jsonCountry = jsonCountries.getJSONObject(i)
            val country = Country(
                jsonCountry.getString("land")
            )
            _countries.add(country)
        }
    }

    override fun onFinalTaskResolved() {
        _db.deleteCountries()
        _db.addCountries(_countries)
        super.onFinalTaskResolved()
    }
}
