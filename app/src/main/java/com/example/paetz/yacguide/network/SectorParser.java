package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Comment.RegionComment;
import com.example.paetz.yacguide.database.Sector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SectorParser extends JSONWebParser {

    private int _regionId;

    public SectorParser(AppDatabase db, UpdateListener listener, int regionId) {
        super(db, listener);
        _regionId = regionId;
        networkRequests.add(new NetworkRequest(
                NetworkRequest.DATA_REQUEST_ID,
                baseUrl + "jsonteilgebiet.php?app=yacguide&gebietid=" + _regionId
        ));
        networkRequests.add(new NetworkRequest(
                NetworkRequest.COMMENTS_REQUEST_ID,
                baseUrl + "jsonkomment.php?app=yacguide&gebietid=" + _regionId
        ));
    }

    @Override
    protected void parseData(int requestId, String json) throws JSONException {
        if (requestId == NetworkRequest.DATA_REQUEST_ID) {
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
            s.setId(jsonField2Int(jsonSector, "sektor_ID"));
            s.setName(jsonSector.getString("sektorname_d"));
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
            comment.setId(jsonField2Int(jsonComment, "komment_ID"));
            comment.setQualityId(jsonField2Int(jsonComment, "qual"));
            comment.setText(jsonComment.getString("kommentar"));
            comment.setRegionId(_regionId);
            db.regionCommentDao().insert(comment);
        }
    }
}
