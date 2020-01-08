package com.example.paetz.yacguide.network

import com.example.paetz.yacguide.UpdateListener
import com.example.paetz.yacguide.database.AppDatabase
import com.example.paetz.yacguide.database.Comment.RockComment
import com.example.paetz.yacguide.database.Comment.RouteComment
import com.example.paetz.yacguide.database.Comment.SectorComment
import com.example.paetz.yacguide.database.Rock
import com.example.paetz.yacguide.database.Route
import com.example.paetz.yacguide.utils.AscendStyle
import com.example.paetz.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException

import java.util.HashSet

class RockParser(
        db: AppDatabase,
        listener: UpdateListener,
        private val _sectorId: Int)
    : JSONWebParser(db, listener) {

    init {
        networkRequests.add(NetworkRequest(
                NetworkRequestType.DATA_REQUEST_ID,
                "${baseUrl}jsongipfel.php?app=yacguide&sektorid=$_sectorId"
        ))
        networkRequests.add(NetworkRequest(
                NetworkRequestType.SUBDATA_REQUEST_ID,
                "${baseUrl}jsonwege.php?app=yacguide&sektorid=$_sectorId"
        ))
        networkRequests.add(NetworkRequest(
                NetworkRequestType.COMMENTS_REQUEST_ID,
                "${baseUrl}jsonkomment.php?app=yacguide&sektorid=$_sectorId"
        ))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestType, json: String) {
        when (requestId) {
            NetworkRequestType.DATA_REQUEST_ID -> parseRocks(json)
            NetworkRequestType.SUBDATA_REQUEST_ID -> parseRoutes(json)
            else -> parseComments(json)
        }
    }

    @Throws(JSONException::class)
    private fun parseRocks(json: String) {
        db.rockDao().deleteAll(_sectorId)
        val jsonRocks = JSONArray(json)
        for (i in 0 until jsonRocks.length()) {
            val jsonRock = jsonRocks.getJSONObject(i)
            val type = ParserUtils.jsonField2Char(jsonRock, "typ")
            if (!_CLIMBING_OBJECT_TYPES.contains(type)) {
                continue
            }
            val r = Rock()
            r.id = ParserUtils.jsonField2Int(jsonRock, "gipfel_ID")
            r.nr = ParserUtils.jsonField2Float(jsonRock, "gipfelnr")
            r.name = ParserUtils.jsonField2String(jsonRock, "gipfelname_d", "gipfelname_cz")
            r.type = type
            r.status = ParserUtils.jsonField2Char(jsonRock, "status")
            r.longitude = ParserUtils.jsonField2Float(jsonRock, "vgrd")
            r.latitude = ParserUtils.jsonField2Float(jsonRock, "ngrd")
            r.ascended = false
            r.parentId = _sectorId
            db.rockDao().insert(r)
        }
    }

    @Throws(JSONException::class)
    private fun parseRoutes(json: String) {
        for (rock in db.rockDao().getAll(_sectorId)) {
            db.routeDao().deleteAll(rock.id)
        }
        val jsonRoutes = JSONArray(json)
        for (i in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(i)
            val r = Route()
            val routeId = ParserUtils.jsonField2Int(jsonRoute, "weg_ID")
            r.id = routeId
            r.nr = ParserUtils.jsonField2Float(jsonRoute, "wegnr")
            r.statusId = ParserUtils.jsonField2Int(jsonRoute, "wegstatus")
            r.name = ParserUtils.jsonField2String(jsonRoute, "wegname_d", "wegname_cz")
            r.grade = jsonRoute.getString("schwierigkeit")
            r.firstAscendLeader = jsonRoute.getString("erstbegvorstieg")
            r.firstAscendFollower = jsonRoute.getString("erstbegnachstieg")
            r.firstAscendDate = jsonRoute.getString("erstbegdatum")
            r.typeOfClimbing = jsonRoute.getString("kletterei")
            r.description = ParserUtils.jsonField2String(jsonRoute, "wegbeschr_d", "wegbeschr_cz")
            r.ascendCount = db.ascendDao().getAscendsForRoute(routeId).size // if we re-add a route that has already been marked as ascended in the past
            r.parentId = ParserUtils.jsonField2Int(jsonRoute, "gipfelid")
            db.routeDao().insert(r)
        }

        // update already ascended rocks
        for (rock in db.rockDao().getAll(_sectorId)) {
            var ascended = false
            for (route in db.routeDao().getAll(rock.id)) {
                for (ascend in db.ascendDao().getAscendsForRoute(route.id)) {
                    ascended = ascended or (ascend.styleId < AscendStyle.eBOTCHED.id)
                }
            }
            db.rockDao().updateAscended(ascended, rock.id)
        }
    }

    @Throws(JSONException::class)
    private fun parseComments(json: String) {
        deleteComments()
        val jsonComments = JSONArray(json)
        for (i in 0 until jsonComments.length()) {
            val jsonComment = jsonComments.getJSONObject(i)
            val routeId = ParserUtils.jsonField2Int(jsonComment, "wegid")
            val rockId = ParserUtils.jsonField2Int(jsonComment, "gipfelid")
            val sectorId = ParserUtils.jsonField2Int(jsonComment, "sektorid")
            when {
                routeId != 0 -> {
                    val comment = RouteComment()
                    comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
                    comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
                    comment.gradeId = ParserUtils.jsonField2Int(jsonComment, "schwer")
                    comment.securityId = ParserUtils.jsonField2Int(jsonComment, "sicher")
                    comment.wetnessId = ParserUtils.jsonField2Int(jsonComment, "nass")
                    comment.text = jsonComment.getString("kommentar")
                    comment.routeId = routeId
                    db.routeCommentDao().insert(comment)
                }
                rockId != 0 -> {
                    val comment = RockComment()
                    comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
                    comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
                    comment.text = jsonComment.getString("kommentar")
                    comment.rockId = rockId
                    db.rockCommentDao().insert(comment)
                }
                sectorId != 0 -> {
                    val comment = SectorComment()
                    comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
                    comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
                    comment.text = jsonComment.getString("kommentar")
                    comment.sectorId = sectorId
                    db.sectorCommentDao().insert(comment)
                }
                else -> throw JSONException("Unknown comment origin")
            }
        }
    }

    private fun deleteComments() {
        db.sectorCommentDao().deleteAll(_sectorId)
        for (rock in db.rockDao().getAll(_sectorId)) {
            db.rockCommentDao().deleteAll(rock.id)
            for (route in db.routeDao().getAll(rock.id)) {
                db.routeCommentDao().deleteAll(route.id)
            }
        }
    }

    companion object {

        private val _CLIMBING_OBJECT_TYPES = object : HashSet<Char>() {
            init {
                add(Rock.typeSummit)
                add(Rock.typeMassif)
                add(Rock.typeBoulder)
                add(Rock.typeStonePit)
                add(Rock.typeAlpine)
                add(Rock.typeCave)
                add(Rock.typeUnofficial)
            }
        }
    }
}
