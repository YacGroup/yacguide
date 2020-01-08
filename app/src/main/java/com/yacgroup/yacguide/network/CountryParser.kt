package com.example.paetz.yacguide.network

import com.example.paetz.yacguide.UpdateListener
import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Country

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
