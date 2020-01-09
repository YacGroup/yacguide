package com.yacgroup.yacguide.network

import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.database.AppDatabase
import com.yacgroup.yacguide.database.Region
import com.yacgroup.yacguide.utils.NetworkUtils
import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException

class RegionParser(
        db: AppDatabase,
        listener: UpdateListener,
        private val _countryName: String)
    : JSONWebParser(db, listener) {

    init {
        networkRequests.add(NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                "${baseUrl}jsongebiet.php?app=yacguide&land=${NetworkUtils.encodeString2Url(_countryName)}"))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestType, json: String) {
        // We don't need to care about requestId here; we only have one
        db.regionDao().deleteAll(_countryName)
        val jsonRegions = JSONArray(json)
        for (i in 0 until jsonRegions.length()) {
            val jsonRegion = jsonRegions.getJSONObject(i)
            val r = Region()
            r.id = ParserUtils.jsonField2Int(jsonRegion, "gebiet_ID")
            r.name = jsonRegion.getString("gebiet")
            r.country = _countryName
            db.regionDao().insert(r)
        }
    }
}
