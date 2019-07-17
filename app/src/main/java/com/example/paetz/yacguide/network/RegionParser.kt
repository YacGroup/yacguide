package com.example.paetz.yacguide.network

import com.example.paetz.yacguide.UpdateListener
import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Region
import com.example.paetz.yacguide.utils.NetworkUtils
import com.example.paetz.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException

class RegionParser(
        db: AppDatabase,
        listener: UpdateListener,
        private val countryName: String)
    : JSONWebParser(db, listener) {

    init {
        networkRequests.add(NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                baseUrl + "jsongebiet.php?app=yacguide&land=" + NetworkUtils.encodeString2Url(countryName)))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestType, json: String) {
        // We don't need to care about requestId here; we only have one
        db.regionDao().deleteAll(countryName)
        val jsonRegions = JSONArray(json)
        for (i in 0 until jsonRegions.length()) {
            val jsonRegion = jsonRegions.getJSONObject(i)
            val r = Region()
            r.id = ParserUtils.jsonField2Int(jsonRegion, "gebiet_ID")
            r.name = jsonRegion.getString("gebiet")
            r.country = countryName
            db.regionDao().insert(r)
        }
    }
}
