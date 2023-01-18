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

package com.yacgroup.yacguide.network

import com.yacgroup.yacguide.ClimbingObjectUId
import com.yacgroup.yacguide.database.Country
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.utils.NetworkUtils
import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException
import java.lang.RuntimeException
import java.util.*

class CountryAndRegionParser(private val _db: DatabaseWrapper) : JSONWebParser() {

    private val _countries = mutableListOf<Country>()
    private val _regions = mutableListOf<Region>()

    override fun initNetworkRequests(climbingObjectUId: ClimbingObjectUId) {
        networkRequests = LinkedList(listOf(
            NetworkRequest(
                uid = climbingObjectUId,
                type = RequestType.COUNTRY_DATA,
                url = "${baseUrl}jsonland.php?app=yacguide")
        ))
    }

    @Throws(JSONException::class)
    override fun parseData(request: NetworkRequest, json: String) {
        when (request.type) {
            RequestType.COUNTRY_DATA -> _parseCountries(json)
            RequestType.REGION_DATA -> _parseRegions(json, request.uid.name)
            else -> throw RuntimeException("Invalid request type")
        }
    }

    private fun _parseCountries(json: String) {
        val jsonCountries = JSONArray(json)
        for (i in 0 until jsonCountries.length()) {
            val jsonCountry = jsonCountries.getJSONObject(i)
            val name = ParserUtils.replaceUnderscores(jsonCountry.getString("land"))
            val country = Country(name)
            _countries.add(country)
            _requestRegions(name)
        }
    }

    private fun _requestRegions(countryName: String) {
        val request = NetworkRequest(
            uid = ClimbingObjectUId(0, countryName),
            type = RequestType.REGION_DATA,
            url = "${baseUrl}jsongebiet.php?app=yacguide&land=${NetworkUtils.encodeString2Url(countryName)}")
        networkRequests.add(request)
        NetworkTask(request, this).execute()
    }

    private fun _parseRegions(json: String, countryName: String) {
        val jsonRegions = JSONArray(json)
        for (i in 0 until jsonRegions.length()) {
            val jsonRegion = jsonRegions.getJSONObject(i)
            val region = Region(
                id = ParserUtils.jsonField2Int(jsonRegion, "gebiet_ID"),
                name = ParserUtils.replaceUnderscores(jsonRegion.getString("gebiet")),
                country = countryName
            )
            _regions.add(region)
        }
    }

    override fun onFinalTaskResolved(exitCode: ExitCode) {
        if (exitCode == ExitCode.SUCCESS) {
            _db.deleteCountries()
            _db.deleteRegions()
            _db.addCountries(_countries)
            _db.addRegions(_regions)
        }
        super.onFinalTaskResolved(exitCode)
    }
}
