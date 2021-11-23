/*
 * Copyright (C) 2019 Fabian Kantereit
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
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.utils.NetworkUtils
import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.jvm.Throws

class RegionParser(
        private val _db: DatabaseWrapper,
        private val _countryName: String)
    : JSONWebParser() {

    private val _regions = mutableListOf<Region>()

    override fun initNetworkRequests() {
        networkRequests = LinkedList(listOf(
                NetworkRequest(
                    NetworkRequestUId(RequestType.REGION_DATA, 0),
                    "${baseUrl}jsongebiet.php?app=yacguide&land=${NetworkUtils.encodeString2Url(_countryName)}")
        ))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestUId, json: String) {
        val jsonRegions = JSONArray(json)
        for (i in 0 until jsonRegions.length()) {
            val jsonRegion = jsonRegions.getJSONObject(i)
            val r = Region()
            r.id = ParserUtils.jsonField2Int(jsonRegion, "gebiet_ID")
            r.name = jsonRegion.getString("gebiet")
            r.country = _countryName
            _regions.add(r)
        }
    }

    override fun onFinalTaskResolved() {
        _db.deleteRegions(_countryName)
        _db.addRegions(_regions)
        super.onFinalTaskResolved()
    }
}
