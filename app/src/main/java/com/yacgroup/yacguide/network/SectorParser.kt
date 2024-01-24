/*
 * Copyright (C) 2019, 2022, 2023 Axel Paetzold
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

import com.yacgroup.yacguide.ClimbingObjectUId
import com.yacgroup.yacguide.database.*
import com.yacgroup.yacguide.database.comment.RegionComment
import com.yacgroup.yacguide.database.comment.RockComment
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.database.comment.SectorComment
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.DateUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.RuntimeException
import java.util.*
import kotlin.jvm.Throws

class SectorParser(private val _db: DatabaseWrapper) : JSONWebParser() {

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
    private var _regionId: Int = 0
    private var _sectorCount: Int = 0
    private var _parsedSectorCount: Int = 0

    override fun initNetworkRequests(climbingObjectUId: ClimbingObjectUId) {
        _regionId = climbingObjectUId.id
        networkRequests = LinkedList(listOf(
            NetworkRequest(
                uid = climbingObjectUId,
                type = RequestType.SECTOR_DATA,
                url = "${baseUrl}jsonteilgebiet.php?app=yacguide&gebietid=$_regionId"),
            NetworkRequest(
                uid = climbingObjectUId,
                type = RequestType.REGION_COMMENTS,
                url = "${baseUrl}jsonkomment.php?app=yacguide&gebietid=$_regionId")
        ))
    }

    @Throws(JSONException::class)
    override fun parseData(request: NetworkRequest, json: String) {
        when (request.type) {
            RequestType.SECTOR_DATA -> _parseSectors(json)
            RequestType.REGION_COMMENTS -> _parseRegionComments(json)
            RequestType.ROCK_DATA -> _parseRocks(json, request.uid.id)
            RequestType.ROUTE_DATA -> _parseRoutes(json)
            RequestType.SECTOR_COMMENTS -> _parseComments(json)
            else -> throw RuntimeException("Invalid request type")
        }
    }

    override fun onFinalTaskResolved(exitCode: ExitCode) {
        if (exitCode == ExitCode.SUCCESS) {
            _db.deleteSectorsRecursively(_regionId)

            _db.addSectors(_sectors)
            _db.addRocks(_rocks)
            _db.addRoutes(_routes)
            _db.addRegionComments(_regionComments)
            _db.addSectorComments(_sectorComments)
            _db.addRockComments(_rockComments)
            _db.addRouteComments(_routeComments)

            _updateAscendedRocks()
        }

        // Clear lists for the case of updating multiple regions
        _sectors.clear()
        _rocks.clear()
        _routes.clear()
        _regionComments.clear()
        _sectorComments.clear()
        _rockComments.clear()
        _routeComments.clear()

        super.onFinalTaskResolved(exitCode)
    }

    @Throws(JSONException::class)
    private fun _parseSectors(json: String) {
        val jsonSectors = JSONArray(json)
        _sectorCount = jsonSectors.length()
        _parsedSectorCount = 0
        listener?.onUpdateStatus("0 %")
        for (i in 0 until jsonSectors.length()) {
            val jsonSector = jsonSectors.getJSONObject(i)
            val firstName = ParserUtils.replaceUnderscores(jsonSector.getString("sektorname_d"))
            val secondName = ParserUtils.replaceUnderscores(jsonSector.getString("sektorname_cz"))
            val sector = Sector(
                id = ParserUtils.jsonField2Int(jsonSector, "sektor_ID"),
                name = ParserUtils.encodeObjectNames(firstName, secondName),
                nr = ParserUtils.jsonField2Float(jsonSector, "sektornr"),
                parentId = _regionId
            )
            _sectors.add(sector)
            _requestRocks(ClimbingObjectUId(sector.id, sector.name.orEmpty()))
        }
    }

    @Throws(JSONException::class)
    private fun _parseRegionComments(json: String) {
        val jsonComments = JSONArray(json)
        for (i in 0 until jsonComments.length()) {
            val jsonComment = jsonComments.getJSONObject(i)
            val user = jsonComment.getString("username")
            val date = _getCommentDate(jsonComment)
            val comment = RegionComment(
                id = ParserUtils.jsonField2Int(jsonComment, "komment_ID"),
                qualityId = ParserUtils.jsonField2Int(jsonComment, "qual"),
                text = jsonComment.getString("kommentar") + "   [$user, $date]",
                regionId = _regionId
            )
            _regionComments.add(comment)
        }
    }

    private fun _requestRocks(sectorUId: ClimbingObjectUId) {
        val requests = listOf(
            NetworkRequest(
                uid = sectorUId,
                type = RequestType.ROCK_DATA,
                url = "${baseUrl}jsongipfel.php?app=yacguide&sektorid=${sectorUId.id}"),
            NetworkRequest(
                uid = sectorUId,
                type = RequestType.ROUTE_DATA,
                url = "${baseUrl}jsonwege.php?app=yacguide&sektorid=${sectorUId.id}"),
            NetworkRequest(
                uid = sectorUId,
                type = RequestType.SECTOR_COMMENTS,
                url = "${baseUrl}jsonkomment.php?app=yacguide&sektorid=${sectorUId.id}")
        )
        networkRequests.addAll(requests)
        requests.forEach {
            NetworkTask(it, networkScope, this).execute()
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
            val firstName = ParserUtils.replaceUnderscores(jsonRock.getString("gipfelname_d"))
            val secondName = ParserUtils.replaceUnderscores(jsonRock.getString("gipfelname_cz"))
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
            val firstName = ParserUtils.replaceUnderscores(jsonRoute.getString("wegname_d"))
            val secondName = ParserUtils.replaceUnderscores(jsonRoute.getString("wegname_cz"))
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
        listener?.onUpdateStatus("${ (100 * (++_parsedSectorCount)) / _sectorCount } %")
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
            val date = _getCommentDate(jsonComment)
            when {
                routeId != 0 -> {
                    val comment = RouteComment(
                        id = ParserUtils.jsonField2Int(jsonComment, "komment_ID"),
                        qualityId = ParserUtils.jsonField2Int(jsonComment, "qual"),
                        gradeId = ParserUtils.jsonField2Int(jsonComment, "schwer"),
                        securityId = ParserUtils.jsonField2Int(jsonComment, "sicher"),
                        wetnessId = ParserUtils.jsonField2Int(jsonComment, "nass"),
                        text = jsonComment.getString("kommentar") + "   [$user, $date]",
                        routeId = routeId
                    )
                    _routeComments.add(comment)
                }
                rockId != 0 -> {
                    val comment = RockComment(
                        id = ParserUtils.jsonField2Int(jsonComment, "komment_ID"),
                        qualityId = ParserUtils.jsonField2Int(jsonComment, "qual"),
                        text = jsonComment.getString("kommentar") + "   [$user, $date]",
                        rockId = rockId
                    )
                    _rockComments.add(comment)
                }
                sectorId != 0 -> {
                    val comment = SectorComment(
                        id = ParserUtils.jsonField2Int(jsonComment, "komment_ID"),
                        qualityId = ParserUtils.jsonField2Int(jsonComment, "qual"),
                        text = jsonComment.getString("kommentar") + "   [$user, $date]",
                        sectorId = sectorId
                    )
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

    /**
     * Return the change date from the given JSON object.
     *
     * If the change date is empty, try the creation date.
     */
    private fun _getCommentDate(jsonObject: JSONObject): String {
        val changeDate = jsonObject.getString("adatum")
        val creationDate = jsonObject.getString("datum")
        val date = if (changeDate.isNotBlank()) {
            changeDate
        } else if (creationDate.isNotBlank()) {
            creationDate
        } else {
            ""
        }
        // The date is expected in the format yyyy-MM-dd HH:mm:ss.
        // Return only the date in the format yyyy-MM-dd.
        date.split(" ").let {
            return if (it.isNotEmpty()) DateUtils.formatDate(it[0]) else ""
        }
    }
}
