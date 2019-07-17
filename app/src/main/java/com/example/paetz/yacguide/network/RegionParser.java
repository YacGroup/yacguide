package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Region;
import com.example.paetz.yacguide.utils.NetworkUtils;
import com.example.paetz.yacguide.utils.ParserUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegionParser extends JSONWebParser {

    private String _countryName;

    public RegionParser(AppDatabase db, UpdateListener listener, String countryName) {
        super(db, listener);
        _countryName = countryName;
        networkRequests.add(new NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                baseUrl + "jsongebiet.php?app=yacguide&land=" + NetworkUtils.encodeString2Url(countryName)));
    }

    @Override
    protected void parseData(NetworkRequestType requestId, String json) throws JSONException {
        // We don't need to care about requestId here; we only have one
        db.regionDao().deleteAll(_countryName);
        final JSONArray jsonRegions = new JSONArray(json);
        for (int i = 0; i < jsonRegions.length(); i++) {
            final JSONObject jsonRegion = jsonRegions.getJSONObject(i);
            Region r = new Region();
            r.setId(ParserUtils.jsonField2Int(jsonRegion, "gebiet_ID"));
            r.setName(jsonRegion.getString("gebiet"));
            r.setCountry(_countryName);
            db.regionDao().insert(r);
        }
    }
}
