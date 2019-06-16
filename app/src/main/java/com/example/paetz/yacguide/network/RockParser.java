package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.database.Ascend;
import com.example.paetz.yacguide.database.Comment.RockComment;
import com.example.paetz.yacguide.database.Comment.RouteComment;
import com.example.paetz.yacguide.database.Comment.SectorComment;
import com.example.paetz.yacguide.database.Rock;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.AscendStyle;
import com.example.paetz.yacguide.utils.ParserUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class RockParser extends JSONWebParser {

    private static final Set<Character> _climbingObjectTypes = new HashSet<Character>() {{
        add(Rock.typeSummit);
        add(Rock.typeMassif);
        add(Rock.typeBoulder);
        add(Rock.typeStonePit);
        add(Rock.typeAlpine);
        add(Rock.typeCave);
        add(Rock.typeUnofficial);
    }};

    private int _sectorId;

    public RockParser(AppDatabase db, UpdateListener listener, int sectorId) {
        super(db, listener);
        _sectorId = sectorId;
        networkRequests.add(new NetworkRequest(
                NetworkRequest.DATA_REQUEST_ID,
                baseUrl + "jsongipfel.php?app=yacguide&sektorid=" + _sectorId
        ));
        networkRequests.add(new NetworkRequest(
                NetworkRequest.SUBDATA_REQUEST_ID,
                baseUrl + "jsonwege.php?app=yacguide&sektorid=" + _sectorId
        ));
        networkRequests.add(new NetworkRequest(
                NetworkRequest.COMMENTS_REQUEST_ID,
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
            final char type = ParserUtils.jsonField2Char(jsonRock, "typ");
            if (!_climbingObjectTypes.contains(type)) {
                continue;
            }
            Rock r = new Rock();
            r.setId(ParserUtils.jsonField2Int(jsonRock,"gipfel_ID"));
            r.setNr(ParserUtils.jsonField2Float(jsonRock,"gipfelnr"));
            r.setName(ParserUtils.jsonField2String(jsonRock,"gipfelname_d", "gipfelname_cz"));
            r.setType(type);
            r.setStatus(ParserUtils.jsonField2Char(jsonRock, "status"));
            r.setLongitude(ParserUtils.jsonField2Float(jsonRock, "vgrd"));
            r.setLatitude(ParserUtils.jsonField2Float(jsonRock, "ngrd"));
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
            final int routeId = ParserUtils.jsonField2Int(jsonRoute,"weg_ID");
            r.setId(routeId);
            r.setNr(ParserUtils.jsonField2Float(jsonRoute, "wegnr"));
            r.setStatusId(ParserUtils.jsonField2Int(jsonRoute, "wegstatus"));
            r.setName(ParserUtils.jsonField2String(jsonRoute,"wegname_d", "wegname_cz"));
            r.setGrade(jsonRoute.getString("schwierigkeit"));
            r.setFirstAscendLeader(jsonRoute.getString("erstbegvorstieg"));
            r.setFirstAscendFollower(jsonRoute.getString("erstbegnachstieg"));
            r.setFirstAscendDate(jsonRoute.getString("erstbegdatum"));
            r.setTypeOfClimbing(jsonRoute.getString("kletterei"));
            r.setDescription(ParserUtils.jsonField2String(jsonRoute,"wegbeschr_d", "wegbeschr_cz"));
            r.setAscendCount(db.ascendDao().getAscendsForRoute(routeId).length); // if we re-add a route that has already been marked as ascended in the past
            r.setParentId(ParserUtils.jsonField2Int(jsonRoute, "gipfelid"));
            db.routeDao().insert(r);
        }

        // update already ascended rocks
        for (final Rock rock : db.rockDao().getAll(_sectorId)) {
            boolean ascended = false;
            for (final Route route : db.routeDao().getAll(rock.getId())) {
                for (final Ascend ascend : db.ascendDao().getAscendsForRoute(route.getId())) {
                    ascended |= (ascend.getStyleId() < AscendStyle.eBOTCHED.id);
                }
            }
            db.rockDao().updateAscended(ascended, rock.getId());
        }
    }

    private void parseComments(String json) throws JSONException {
        deleteComments();
        final JSONArray jsonComments = new JSONArray(json);
        for (int i = 0; i < jsonComments.length(); i++) {
            final JSONObject jsonComment = jsonComments.getJSONObject(i);
            final int routeId = ParserUtils.jsonField2Int(jsonComment, "wegid");
            final int rockId = ParserUtils.jsonField2Int(jsonComment,"gipfelid");
            final int sectorId = ParserUtils.jsonField2Int(jsonComment, "sektorid");
            if (routeId != 0) {
                RouteComment comment = new RouteComment();
                comment.setId(ParserUtils.jsonField2Int(jsonComment, "komment_ID"));
                comment.setQualityId(ParserUtils.jsonField2Int(jsonComment, "qual"));
                comment.setGradeId(ParserUtils.jsonField2Int(jsonComment, "schwer"));
                comment.setSecurityId(ParserUtils.jsonField2Int(jsonComment, "sicher"));
                comment.setWetnessId(ParserUtils.jsonField2Int(jsonComment, "nass"));
                comment.setText(jsonComment.getString("kommentar"));
                comment.setRouteId(routeId);
                db.routeCommentDao().insert(comment);
            } else if (rockId != 0) {
                RockComment comment = new RockComment();
                comment.setId(ParserUtils.jsonField2Int(jsonComment, "komment_ID"));
                comment.setQualityId(ParserUtils.jsonField2Int(jsonComment, "qual"));
                comment.setText(jsonComment.getString("kommentar"));
                comment.setRockId(rockId);
                db.rockCommentDao().insert(comment);
            } else if (sectorId != 0) {
                SectorComment comment = new SectorComment();
                comment.setId(ParserUtils.jsonField2Int(jsonComment, "komment_ID"));
                comment.setQualityId(ParserUtils.jsonField2Int(jsonComment, "qual"));
                comment.setText(jsonComment.getString("kommentar"));
                comment.setSectorId(sectorId);
                db.sectorCommentDao().insert(comment);
            } else {
                throw new JSONException("Unknown comment origin");
            }
        }
    }

    private void deleteComments() {
        db.sectorCommentDao().deleteAll(_sectorId);
        for (Rock rock : db.rockDao().getAll(_sectorId)) {
            db.rockCommentDao().deleteAll(rock.getId());
            for (Route route : db.routeDao().getAll(rock.getId())) {
                db.routeCommentDao().deleteAll(route.getId());
            }
        }
    }
}
