/*
 * Copyright (C) 2018 Axel Pätzold
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

import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.database.Country

import org.json.JSONArray
import org.json.JSONException

class CountryParser(db: AppDatabase, listener: UpdateListener) : JSONWebParser(db, listener) {

    init {
        networkRequests.add(NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                "${baseUrl}jsonland.php?app=yacguide"))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestType, json: String) {
        // We don't need to care about requestId here; we only have one
        db.countryDao().deleteAll()
        val jsonCountries = JSONArray(json)
        for (i in 0 until jsonCountries.length()) {
            val jsonCountry = jsonCountries.getJSONObject(i)
            val c = Country()
            c.name = jsonCountry.getString("land")
            db.countryDao().insert(c)
        }
    }
}
