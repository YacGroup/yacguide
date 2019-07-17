package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Comment.RegionComment;
import com.example.paetz.yacguide.database.Sector;
import com.example.paetz.yacguide.utils.ParserUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SectorParser extends JSONWebParser {

    private int _regionId;

    public SectorParser(AppDatabase db, UpdateListener listener, int regionId) {
        super(db, listener);
        _regionId = regionId;
        networkRequests.add(new NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                baseUrl + "jsonteilgebiet.php?app=yacguide&gebietid=" + _regionId
        ));
        networkRequests.add(new NetworkRequest(
                NetworkRequestType.COMMENTS_REQUEST_ID,
                baseUrl + "jsonkomment.php?app=yacguide&gebietid=" + _regionId
        ));
    }

    @Override
    protected void parseData(NetworkRequestType requestId, String json) throws JSONException {
        if (requestId == NetworkRequestType.DATA_REQUEST_ID) {
            parseSectors(json);
        } else {
            parseRegionComments(json);
        }
    }

    private void parseSectors(String json) throws JSONException {
        db.sectorDao().deleteAll(_regionId);
        final JSONArray jsonSectors = new JSONArray(json);
        for (int i = 0; i < jsonSectors.length(); i++) {
            final JSONObject jsonSector = jsonSectors.getJSONObject(i);
            Sector s = new Sector();
            s.setId(ParserUtils.jsonField2Int(jsonSector, "sektor_ID"));
            s.setName(ParserUtils.jsonField2String(jsonSector, "sektorname_d", "sektorname_cz"));
            s.setNr(ParserUtils.jsonField2Float(jsonSector, "sektornr"));
            s.setParentId(_regionId);
            db.sectorDao().insert(s);
        }
    }

    private void parseRegionComments(String json) throws JSONException {
        db.regionCommentDao().deleteAll(_regionId);
        final JSONArray jsonComments = new JSONArray(json);
        for (int i = 0; i < jsonComments.length(); i++) {
            final JSONObject jsonComment = jsonComments.getJSONObject(i);
            RegionComment comment = new RegionComment();
            comment.setId(ParserUtils.jsonField2Int(jsonComment, "komment_ID"));
            comment.setQualityId(ParserUtils.jsonField2Int(jsonComment, "qual"));
            comment.setText(jsonComment.getString("kommentar"));
            comment.setRegionId(_regionId);
            db.regionCommentDao().insert(comment);
        }
    }
}
