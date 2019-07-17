package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Country;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CountryParser extends JSONWebParser {

    public CountryParser(AppDatabase db, UpdateListener listener) {
        super(db, listener);
        networkRequests.add(new NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                baseUrl + "jsonland.php?app=yacguide"));
    }

    @Override
    protected void parseData(NetworkRequestType requestId, String json) throws JSONException {
        // We don't need to care about requestId here; we only have one
        db.countryDao().deleteAll();
        final JSONArray jsonCountries = new JSONArray(json);
        for (int i = 0; i < jsonCountries.length(); i++) {
            final JSONObject jsonCountry = jsonCountries.getJSONObject(i);
            Country c = new Country();
            c.setName(jsonCountry.getString("land"));
            db.countryDao().insert(c);
        }
    }
}
