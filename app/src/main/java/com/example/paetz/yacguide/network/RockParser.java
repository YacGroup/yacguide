package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.google.common.collect.ImmutableSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class RockParser extends JSONWebParser {

    private static final Set<Character> _climbingObjectTypes =  ImmutableSet.of(
            Rock.typeSummit,
            Rock.typeMassif,
            Rock.typeBoulder,
            Rock.typeCave,
            Rock.typeUnofficial);
    private int _sectorId;

    public RockParser(AppDatabase db, UpdateListener listener, int sectorId) {
        super(db, listener);
        _sectorId = sectorId;
        networkRequests.add(new NetworkRequest(
                NetworkRequest.DATA_REQUEST_ID,
                "Lade Gipfeldaten...",
                baseUrl + "jsongipfel.php?app=yacguide&sektorid=" + _sectorId
        ));
        networkRequests.add(new NetworkRequest(
                NetworkRequest.SUBDATA_REQUEST_ID,
                "Lade Wegdaten...",
                baseUrl + "jsonwege.php?app=yacguide&sektorid=" + _sectorId
        ));
        networkRequests.add(new NetworkRequest(
                NetworkRequest.COMMENTS_REQUEST_ID,
                "Lade Kommentare",
                baseUrl + "jsonkomment.php?app=yacguide&sektorid=" + _sectorId
        ));
    }

    @Override
    protected void parseData(int requestId, String json) throws JSONException {
        if (requestId == NetworkRequest.DATA_REQUEST_ID) {
            parseRocks(json);
        } else if (requestId == NetworkRequest.SUBDATA_REQUEST_ID) {
            parseRoutes(json);
        } else {
            parseComments(json);
        }
    }

    private void parseRocks(String json) throws JSONException {
        db.rockDao().deleteAll(_sectorId);
        final JSONArray jsonRocks = new JSONArray(json);
        for (int i = 0; i < jsonRocks.length(); i++) {
            final JSONObject jsonRock = jsonRocks.getJSONObject(i);
            final char type = jsonRock.getString("typ").charAt(0);
            if (!_climbingObjectTypes.contains(type)) {
                continue;
            }
            String status = jsonRock.getString("status");
            if (status.isEmpty()) {
                status = " ";
            }
            Rock r = new Rock();
            r.setId(Integer.parseInt(jsonRock.getString("gipfel_ID")));
            r.setNr(Float.parseFloat(jsonRock.getString("gipfelnr")));
            r.setName(jsonRock.getString("gipfelname_d"));
            r.setType(type);
            r.setStatus(status.charAt(0));
            r.setLongitude(Float.parseFloat(jsonRock.getString("vgrd")));
            r.setLatitude(Float.parseFloat(jsonRock.getString("ngrd")));
            r.setAscended(false);
            r.setParentId(_sectorId);
            db.rockDao().insert(r);
        }
    }

    private void parseRoutes(String json) throws JSONException {
        for (final Rock rock : db.rockDao().getAll(_sectorId)) {
            db.routeDao().deleteAll(rock.getId());
        }
        final JSONArray jsonRoutes = new JSONArray(json);
        for (int i = 0; i < jsonRoutes.length(); i++) {
            final JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route r = new Route();
            int routeId = Integer.parseInt(jsonRoute.getString("weg_ID"));
            r.setId(routeId);
            r.setNr(Float.parseFloat(jsonRoute.getString("wegnr")));
            r.setStatus(Integer.parseInt(jsonRoute.getString("wegstatus")));
            r.setName(jsonRoute.getString("wegname_d"));
            r.setGrade(jsonRoute.getString("schwierigkeit"));
            r.setFirstAscendLeader(jsonRoute.getString("erstbegvorstieg"));
            r.setFirstAscendFollower(jsonRoute.getString("erstbegnachstieg"));
            r.setFirstAscendDate(jsonRoute.getString("erstbegdatum"));
            r.setTypeOfClimbing(jsonRoute.getString("kletterei"));
            r.setDescription(jsonRoute.getString("wegbeschr_d"));
            r.setAscendCount(db.ascendDao().getAscendsForRoute(routeId).length); // if we re-add a route that has already been marked as ascended in the past
            r.setParentId(Integer.parseInt(jsonRoute.getString("gipfelid")));
            db.routeDao().insert(r);
        }

        // update already ascended rocks
        for (final Rock rock : db.rockDao().getAll(_sectorId)) {
            boolean ascended = false;
            for (final Route route : db.routeDao().getAll(rock.getId())) {
                ascended |= (route.getAscendCount() > 0);
            }
            db.rockDao().updateAscended(ascended, rock.getId());
        }
    }
}
