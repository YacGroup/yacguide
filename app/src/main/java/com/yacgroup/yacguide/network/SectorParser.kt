/*
 * Copyright (C) 2019, 2022 Axel Paetzold
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

import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.database.comment.RegionComment
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.database.comment.SectorComment
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

import org.json.JSONArray
import org.json.JSONException
import java.lang.RuntimeException
import java.util.*
import kotlin.jvm.Throws

class SectorParser(private val _db: DatabaseWrapper,
                   private var _regionId: Int,
                   private var _regionName: String = "") : JSONWebParser() {

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

    private val _sectors = mutableListOf<Sector>()
    private val _rocks = mutableListOf<Rock>()
    private val _routes = mutableListOf<Route>()
    private val _regionComments = mutableListOf<RegionComment>()
    private val _sectorComments = mutableListOf<SectorComment>()
    private val _rockComments = mutableListOf<RockComment>()
    private val _routeComments = mutableListOf<RouteComment>()

    private var _sectorCount: Int = 0
    private var _parsedSectorCount: Int = 0

    override fun initNetworkRequests() {
        networkRequests = LinkedList(listOf(
                NetworkRequest(
                        NetworkRequestUId(RequestType.SECTOR_DATA, 0),
                        "${baseUrl}jsonteilgebiet.php?app=yacguide&gebietid=$_regionId"),
                NetworkRequest(
                        NetworkRequestUId(RequestType.REGION_COMMENTS, 0),
                        "${baseUrl}jsonkomment.php?app=yacguide&gebietid=$_regionId")
        ))
    }

    @Throws(JSONException::class)
    override fun parseData(requestId: NetworkRequestUId, json: String) {
        when (requestId.type) {
            RequestType.SECTOR_DATA -> _parseSectors(json)
            RequestType.REGION_COMMENTS -> _parseRegionComments(json)
            RequestType.ROCK_DATA -> _parseRocks(json, requestId.id)
            RequestType.ROUTE_DATA -> _parseRoutes(json)
            RequestType.SECTOR_COMMENTS -> _parseComments(json)
            else -> throw RuntimeException("Invalid request type")
        }
    }

    override fun onFinalTaskResolved() {
        _db.deleteSectorsRecursively(_regionId)

        _db.addSectors(_sectors)
        _db.addRocks(_rocks)
        _db.addRoutes(_routes)
        _db.addRegionComments(_regionComments)
        _db.addSectorComments(_sectorComments)
        _db.addRockComments(_rockComments)
        _db.addRouteComments(_routeComments)

        _updateAscendedRocks()

        // Clear lists for the case of updating multiple regions
        _sectors.clear()
        _rocks.clear()
        _routes.clear()
        _regionComments.clear()
        _sectorComments.clear()
        _rockComments.clear()
        _routeComments.clear()

        super.onFinalTaskResolved()
    }

    fun setRegionId(id: Int) {
        _regionId = id
    }

    fun setRegionName(name: String) {
        _regionName = name
    }

    @Throws(JSONException::class)
    private fun _parseSectors(json: String) {
        val jsonSectors = JSONArray(json)
        _sectorCount = jsonSectors.length()
        _parsedSectorCount = 0
        listener?.onUpdateStatus("$_regionName 0 %")
        for (i in 0 until jsonSectors.length()) {
            val jsonSector = jsonSectors.getJSONObject(i)
            val firstName = jsonSector.getString("sektorname_d")
            val secondName = jsonSector.getString("sektorname_cz")
            val s = Sector(
                id = ParserUtils.jsonField2Int(jsonSector, "sektor_ID"),
                name = ParserUtils.encodeObjectNames(firstName, secondName),
                nr = ParserUtils.jsonField2Float(jsonSector, "sektornr"),
                parentId = _regionId
            )
            _sectors.add(s)
            _requestRocks(s.id)
        }
    }

    @Throws(JSONException::class)
    private fun _parseRegionComments(json: String) {
        val jsonComments = JSONArray(json)
        for (i in 0 until jsonComments.length()) {
            val jsonComment = jsonComments.getJSONObject(i)
            val comment = RegionComment()
            comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
            comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
            val user = jsonComment.getString("username")
            comment.text = jsonComment.getString("kommentar") + "   [$user]"
            comment.regionId = _regionId
            _regionComments.add(comment)
        }
    }

    private fun _requestRocks(sectorId: Int) {
        val requests = listOf(
            NetworkRequest(
                    NetworkRequestUId(RequestType.ROCK_DATA, sectorId),
                    "${baseUrl}jsongipfel.php?app=yacguide&sektorid=$sectorId"),
            NetworkRequest(
                    NetworkRequestUId(RequestType.ROUTE_DATA, sectorId),
                    "${baseUrl}jsonwege.php?app=yacguide&sektorid=$sectorId"),
            NetworkRequest(
                    NetworkRequestUId(RequestType.SECTOR_COMMENTS, sectorId),
                    "${baseUrl}jsonkomment.php?app=yacguide&sektorid=$sectorId")
        )
        networkRequests.addAll(requests)
        requests.forEach {
            NetworkTask(it.requestId, this).execute(it.url)
        }
    }

    @Throws(JSONException::class)
    private fun _parseRocks(json: String, sectorId: Int) {
        val jsonRocks = JSONArray(json)
        for (i in 0 until jsonRocks.length()) {
            val jsonRock = jsonRocks.getJSONObject(i)
            val type = ParserUtils.jsonField2Char(jsonRock, "typ")
            if (!_CLIMBING_OBJECT_TYPES.contains(type)) {
                continue
            }
            val firstName = jsonRock.getString("gipfelname_d")
            val secondName = jsonRock.getString("gipfelname_cz")
            val rock = Rock(
                id = ParserUtils.jsonField2Int(jsonRock, "gipfel_ID"),
                nr = ParserUtils.jsonField2Float(jsonRock, "gipfelnr"),
                name = ParserUtils.encodeObjectNames(firstName, secondName),
                type = type,
                status = ParserUtils.jsonField2Char(jsonRock, "status"),
                longitude = ParserUtils.jsonField2Float(jsonRock, "vgrd"),
                latitude = ParserUtils.jsonField2Float(jsonRock, "ngrd"),
                ascendsBitMask = 0,
                parentId = sectorId
            )
            _rocks.add(rock)
        }
    }

    @Throws(JSONException::class)
    private fun _parseRoutes(json: String) {
        val jsonRoutes = JSONArray(json)
        for (i in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(i)
            val routeId = ParserUtils.jsonField2Int(jsonRoute, "weg_ID")
            val firstName = jsonRoute.getString("wegname_d")
            val secondName = jsonRoute.getString("wegname_cz")
            var ascentsBitMask = 0
            // re-store route's bitmask if it is re-added after being marked as ascended in the past
            _db.getRouteAscends(routeId).forEach {
                ascentsBitMask = ascentsBitMask or AscendStyle.bitMask(it.styleId)
            }
            val route = Route(
                id = routeId,
                nr = ParserUtils.jsonField2Float(jsonRoute, "wegnr"),
                statusId = ParserUtils.jsonField2Int(jsonRoute, "wegstatus"),
                name = ParserUtils.encodeObjectNames(firstName, secondName),
                grade = jsonRoute.getString("schwierigkeit"),
                firstAscendLeader = jsonRoute.getString("erstbegvorstieg"),
                firstAscendFollower = jsonRoute.getString("erstbegnachstieg"),
                firstAscendDate = jsonRoute.getString("erstbegdatum"),
                typeOfClimbing = jsonRoute.getString("kletterei"),
                description = jsonRoute.getString("wegbeschr_d"),
                ascendsBitMask = ascentsBitMask,
                parentId = ParserUtils.jsonField2Int(jsonRoute, "gipfelid")
            )
            _routes.add(route)
        }
        listener?.onUpdateStatus("$_regionName ${ (100 * (++_parsedSectorCount)) / _sectorCount } %")
    }

    @Throws(JSONException::class)
    private fun _parseComments(json: String) {
        val jsonComments = JSONArray(json)
        for (i in 0 until jsonComments.length()) {
            val jsonComment = jsonComments.getJSONObject(i)
            val routeId = ParserUtils.jsonField2Int(jsonComment, "wegid")
            val rockId = ParserUtils.jsonField2Int(jsonComment, "gipfelid")
            val sectorId = ParserUtils.jsonField2Int(jsonComment, "sektorid")
            val user = jsonComment.getString("username")
            when {
                routeId != 0 -> {
                    val comment = RouteComment()
                    comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
                    comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
                    comment.gradeId = ParserUtils.jsonField2Int(jsonComment, "schwer")
                    comment.securityId = ParserUtils.jsonField2Int(jsonComment, "sicher")
                    comment.wetnessId = ParserUtils.jsonField2Int(jsonComment, "nass")
                    comment.text = jsonComment.getString("kommentar") + "   [$user]"
                    comment.routeId = routeId
                    _routeComments.add(comment)
                }
                rockId != 0 -> {
                    val comment = RockComment(
                        id = ParserUtils.jsonField2Int(jsonComment, "komment_ID"),
                        qualityId = ParserUtils.jsonField2Int(jsonComment, "qual"),
                        text = jsonComment.getString("kommentar") + "   [$user]",
                        rockId = rockId
                    )
                    _rockComments.add(comment)
                }
                sectorId != 0 -> {
                    val comment = SectorComment()
                    comment.id = ParserUtils.jsonField2Int(jsonComment, "komment_ID")
                    comment.qualityId = ParserUtils.jsonField2Int(jsonComment, "qual")
                    comment.text = jsonComment.getString("kommentar") + "   [$user]"
                    comment.sectorId = sectorId
                    _sectorComments.add(comment)
                }
                else -> throw JSONException("Unknown comment origin")
            }
        }
    }

    private fun _updateAscendedRocks() {
        // update already ascended rocks
        val updatedRocks = _rocks.map { rock ->
            _db.getRockAscends(rock.id).forEach { ascent ->
                rock.ascendsBitMask = rock.ascendsBitMask or AscendStyle.bitMask(ascent.styleId)
            }
            rock
        }
        _db.updateRocks(updatedRocks)
    }
}