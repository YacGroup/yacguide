/*
 * Copyright (C) 2019 Fabian Kantereit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yacgroup.yacguide.network

import com.yacgroup.yacguide.UpdateListener
import com.yacgroup.yacguide.database.Comment.RockComment
import com.yacgroup.yacguide.database.Comment.RouteComment
import com.yacgroup.yacguide.database.Comment.SectorComment
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException

import java.util.HashSet

class RockParser(
        private val _db: DatabaseWrapper,
        listener: UpdateListener,
        private val _sectorId: Int)
    : JSONWebParser(listener) {

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
        val rocks = mutableListOf<Rock>()
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
            r.parentId = _sectorId
            rocks.add(r)
        }
        _db.refreshRocks(rocks, _sectorId)
    }

    @Throws(JSONException::class)
    private fun parseRoutes(json: String) {
        val routes = mutableListOf<Route>()
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
            // if we re-add a route that has already been marked as ascended in the past:
            _db.getRouteAscends(routeId).map { r.ascendsBitMask = (r.ascendsBitMask or AscendStyle.bitMask(it.styleId)) }
            r.parentId = ParserUtils.jsonField2Int(jsonRoute, "gipfelid")
            routes.add(r)
        }
        _db.refreshRoutes(routes, _sectorId)

        // update already ascended rocks
        val updatedRocks = _db.getRocks(_sectorId).map {
            for (ascend in _db.getRockAscends(it.id)) {
                it.ascendsBitMask = it.ascendsBitMask or AscendStyle.bitMask(ascend.styleId)
            }
            it
        }
        _db.updateRocks(updatedRocks)
    }

    @Throws(JSONException::class)
    private fun parseComments(json: String) {
        val sectorComments = mutableListOf<SectorComment>()
        val rockComments = mutableListOf<RockComment>()
        val routeComments = mutableListOf<RouteComment>()
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
                    routeComments.add(comment)
                }
                rockId != 0 -> {
                    val comment = RockComment()
                    comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
                    comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
                    comment.text = jsonComment.getString("kommentar")
                    comment.rockId = rockId
                    rockComments.add(comment)
                }
                sectorId != 0 -> {
                    val comment = SectorComment()
                    comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
                    comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
                    comment.text = jsonComment.getString("kommentar")
                    comment.sectorId = sectorId
                    sectorComments.add(comment)
                }
                else -> throw JSONException("Unknown comment origin")
            }
        }
        _db.refreshSectorComments(sectorComments, _sectorId)
        _db.refreshRockComments(rockComments, _sectorId)
        _db.refreshRouteComments(routeComments, _sectorId)
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
