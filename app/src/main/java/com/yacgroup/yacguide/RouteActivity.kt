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

package com.yacgroup.yacguide

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.activity_properties.AscentFilterable
import com.yacgroup.yacguide.activity_properties.RouteSearchable
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.database.comment.RouteComment
import com.yacgroup.yacguide.list_adapters.ItemDiffCallback
import com.yacgroup.yacguide.list_adapters.ListItem
import com.yacgroup.yacguide.list_adapters.ListViewAdapter
import com.yacgroup.yacguide.utils.*

class RouteActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: ListViewAdapter<Route>
    private lateinit var _searchBarHandler: SearchBarHandler
    private lateinit var _routeGettersMap: Map<ClimbingObjectLevel, RouteGetter>
    private var _onlyOfficialRoutes: Boolean = false
    private var _routeNamePart: String = ""
    private var _filterName: String = ""
    private var _filterMinGradeId: Int = RouteComment.NO_INFO_ID
    private var _filterMaxGradeId: Int = RouteComment.NO_INFO_ID
    private var _filterMaxQualityId: Int = RouteComment.NO_INFO_ID
    private var _filterMaxProtectionId: Int = RouteComment.NO_INFO_ID
    private var _filterMaxDryingId: Int = RouteComment.NO_INFO_ID
    private var _filterProjects: Boolean = false
    private var _filterBotches: Boolean = false
    private var _rock: Rock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _filterName = intent.getStringExtra(IntentConstants.FILTER_NAME).orEmpty()
        _filterMinGradeId = intent.getIntExtra(IntentConstants.FILTER_GRADE_FROM, RouteComment.NO_INFO_ID)
        _filterMaxGradeId = intent.getIntExtra(IntentConstants.FILTER_GRADE_TO, RouteComment.NO_INFO_ID)
        _filterMaxQualityId = intent.getIntExtra(IntentConstants.FILTER_RELEVANCE, RouteComment.NO_INFO_ID)
        _filterMaxProtectionId = intent.getIntExtra(IntentConstants.FILTER_PROTECTION, RouteComment.NO_INFO_ID)
        _filterMaxDryingId = intent.getIntExtra(IntentConstants.FILTER_DRYING, RouteComment.NO_INFO_ID)
        _filterProjects = intent.getBooleanExtra(IntentConstants.FILTER_PROJECTS, false)
        _filterBotches = intent.getBooleanExtra(IntentConstants.FILTER_BOTCHES, false)

        properties = arrayListOf(
            RouteSearchable(this),
            AscentFilterable(this)
        )

        _routeGettersMap = mapOf(
            ClimbingObjectLevel.eCountry to RouteGetter(
                getAll = { db.getRoutes() },
                getByName = { db.getRoutesByName(_filterName) },
                getByGrade = { db.getRoutesByGrade(_filterMinGradeId, _filterMaxGradeId) },
                getByQuality = { db.getRoutesByQuality(_filterMaxQualityId) },
                getByProtection = { db.getRoutesByProtection(_filterMaxProtectionId) },
                getByDrying = { db.getRoutesByDrying(_filterMaxDryingId) },
                getProjects = { db.getProjectedRoutes() },
                getBotches = { db.getBotchedRoutes() }
            ),
            ClimbingObjectLevel.eRegion to RouteGetter(
                getAll = { db.getRoutesForCountry(activityLevel.parentUId.name) },
                getByName = { db.getRoutesByNameForCountry(activityLevel.parentUId.name, _filterName) },
                getByGrade = { db.getRoutesByGradeForCountry(activityLevel.parentUId.name, _filterMinGradeId, _filterMaxGradeId) },
                getByQuality = { db.getRoutesByQualityForCountry(activityLevel.parentUId.name, _filterMaxQualityId) },
                getByProtection = { db.getRoutesByProtectionForCountry(activityLevel.parentUId.name, _filterMaxProtectionId) },
                getByDrying = { db.getRoutesByDryingForCountry(activityLevel.parentUId.name, _filterMaxDryingId) },
                getProjects = { db.getProjectedRoutesForCountry(activityLevel.parentUId.name) },
                getBotches = { db.getBotchedRoutesForCountry(activityLevel.parentUId.name) }
            ),
            ClimbingObjectLevel.eSector to RouteGetter(
                getAll = { db.getRoutesForRegion(activityLevel.parentUId.id) },
                getByName = { db.getRoutesByNameForRegion(activityLevel.parentUId.id, _filterName) },
                getByGrade = { db.getRoutesByGradeForRegion(activityLevel.parentUId.id, _filterMinGradeId, _filterMaxGradeId) },
                getByQuality = { db.getRoutesByQualityForRegion(activityLevel.parentUId.id, _filterMaxQualityId) },
                getByProtection = { db.getRoutesByProtectionForRegion(activityLevel.parentUId.id, _filterMaxProtectionId) },
                getByDrying = { db.getRoutesByDryingForRegion(activityLevel.parentUId.id, _filterMaxDryingId) },
                getProjects = { db.getProjectedRoutesForRegion(activityLevel.parentUId.id) },
                getBotches = { db.getBotchedRoutesForRegion(activityLevel.parentUId.id) }
            ),
            ClimbingObjectLevel.eRock to RouteGetter(
                getAll = { db.getRoutesForSector(activityLevel.parentUId.id) },
                getByName = { db.getRoutesByNameForSector(activityLevel.parentUId.id, _filterName) },
                getByGrade = { db.getRoutesByGradeForSector(activityLevel.parentUId.id, _filterMinGradeId, _filterMaxGradeId) },
                getByQuality = { db.getRoutesByQualityForSector(activityLevel.parentUId.id, _filterMaxQualityId) },
                getByProtection = { db.getRoutesByProtectionForSector(activityLevel.parentUId.id, _filterMaxProtectionId) },
                getByDrying = { db.getRoutesByDryingForSector(activityLevel.parentUId.id, _filterMaxDryingId) },
                getProjects = { db.getProjectedRoutesForSector(activityLevel.parentUId.id) },
                getBotches = { db.getBotchedRoutesForSector(activityLevel.parentUId.id) }
            ),
            ClimbingObjectLevel.eRoute to RouteGetter(
                getAll = { db.getRoutesForRock(activityLevel.parentUId.id) },
                getByName = { db.getRoutesByNameForRock(activityLevel.parentUId.id, _filterName) },
                getByGrade = { db.getRoutesByGradeForRock(activityLevel.parentUId.id, _filterMinGradeId, _filterMaxGradeId) },
                getByQuality = { db.getRoutesByQualityForRock(activityLevel.parentUId.id, _filterMaxQualityId) },
                getByProtection = { db.getRoutesByProtectionForRock(activityLevel.parentUId.id, _filterMaxProtectionId) },
                getByDrying = { db.getRoutesByDryingForRock(activityLevel.parentUId.id, _filterMaxDryingId) },
                getProjects = { db.getProjectedRoutesForRock(activityLevel.parentUId.id) },
                getBotches = { db.getBotchedRoutesForRock(activityLevel.parentUId.id) }
            )
        )

        _searchBarHandler = SearchBarHandler(
            searchBarLayout = findViewById(R.id.searchBarLayout),
            searchHintResource = R.string.route_search,
            checkBoxTitle = getString(R.string.only_official_routes),
            checkBoxDefaultValue = resources.getBoolean(R.bool.pref_default_only_official_routes),
            _settings = customSettings,
            _settingsKey = getString(R.string.pref_key_only_official_routes),
            initCallback = { onlyOfficialRoutes -> _onlyOfficialRoutes = onlyOfficialRoutes },
            updateCallback = { routeNamePart, onlyOfficialRoutes -> _onSearchBarUpdate(routeNamePart, onlyOfficialRoutes) }
        )

        if (activityLevel.level == ClimbingObjectLevel.eRoute) {
            _rock = db.getRock(activityLevel.parentUId.id)
        } else {
            findViewById<ImageButton>(R.id.mapButton).visibility = View.INVISIBLE
        }

        _viewAdapter = ListViewAdapter(ItemDiffCallback(
            _areItemsTheSame = { route1, route2 -> route1.id == route2.id },
            _areContentsTheSame = { route1, route2 -> route1 == route2 }
        )) { route -> ListItem(
            backgroundColor = _getRouteBackground(route),
            typeface = _getRouteTypeface(route),
            titleText = _getRouteSectorInfo(route),
            mainText = _getRouteMainText(route),
            subText = ParserUtils.decodeObjectNames(route.name).second,
            onClick = { _onRouteSelected(route) })
        }
        findViewById<RecyclerView>(R.id.tableRecyclerView).adapter = _viewAdapter
    }

    override fun getLayoutId() = R.layout.activity_route

    @Suppress("UNUSED_PARAMETER")
    fun showMap(v: View) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:${_rock?.latitude},${_rock?.longitude}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_map_app_available, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showComments(v: View) {
        when (activityLevel.level) {
            ClimbingObjectLevel.eSector -> showRegionComments(activityLevel.parentUId.id)
            ClimbingObjectLevel.eRock -> showSectorComments(activityLevel.parentUId.id)
            ClimbingObjectLevel.eRoute -> showRockComments(activityLevel.parentUId.id)
            else -> showNoCommentToast()
        }
    }

    override fun displayContent() {
        val levelName = ParserUtils.decodeObjectNames(activityLevel.parentUId.name)
        this.title = levelName.first.ifEmpty { levelName.second }
        _displayRockInfo(findViewById(R.id.infoTextView))

        var routes = _getAndFilterRoutes()
        // additional searchbar filters:
        if (_onlyOfficialRoutes) {
            routes = routes.filter { _isOfficialRoute(it) }
        }
        if (_routeNamePart.isNotEmpty()) {
            routes = routes.filter{ it.name.orEmpty().lowercase().contains(_routeNamePart.lowercase()) }
        }

        _viewAdapter.submitList(routes)
    }

    override fun onStop() {
        _searchBarHandler.storeCustomSettings()
        super.onStop()
    }

    private fun _getAndFilterRoutes(): List<Route> {
        val routeGetter = _routeGettersMap[activityLevel.level]!!
        return if (_filterProjects) {
            routeGetter.getProjects()
        } else if (_filterBotches) {
            routeGetter.getBotches()
        } else if (_noNameFilter()
                && _noGradeFilter()
                && _noQualityFilter()
                && _noProtectionFilter()
                && _noDryingFilter()) {
            routeGetter.getAll()
        } else {
            val nameFilteredRoutes =
                if (_noNameFilter()) null
                else routeGetter.getByName().toSet()
            val gradeFilteredRoutes =
                if (_noGradeFilter()) null
                else {
                    _filterMaxGradeId =
                        if (_filterMaxGradeId == RouteComment.NO_INFO_ID)
                            RouteComment.GRADE_MAP.maxOf { it.key }
                        else
                            _filterMaxGradeId
                    routeGetter.getByGrade().toSet()
                }
            val qualityFilteredRoutes =
                if (_noQualityFilter()) null
                else routeGetter.getByQuality().toSet()
            val protectionFilteredRoutes =
                if (_noProtectionFilter()) null
                else routeGetter.getByProtection().toSet()
            val dryingFilteredRoutes =
                if (_noDryingFilter()) null
                else routeGetter.getByDrying().toSet()
            listOfNotNull(nameFilteredRoutes,
                          gradeFilteredRoutes,
                          qualityFilteredRoutes,
                          protectionFilteredRoutes,
                          dryingFilteredRoutes).reduce {
                    intersection, filteredRoutes -> intersection.intersect(filteredRoutes)
            }.toList()
        }
    }

    private fun _displayRockInfo(infoTextView: TextView) {
        infoTextView.text = when (_rock?.status){
            Rock.statusCollapsed -> getString(R.string.rock_collapsed)
            Rock.statusProhibited -> getString(R.string.rock_fully_prohibited)
            Rock.statusTemporarilyProhibited -> getString(R.string.rock_temporarily_prohibited)
            Rock.statusPartlyProhibited -> getString(R.string.rock_partly_prohibited)
            else -> {
                if (_rock?.type == Rock.typeUnofficial) {
                    getString(R.string.rock_inofficial)
                } else {
                    ""
                }
            }
        }
    }

    private fun _onSearchBarUpdate(routeNamePart: String, onlyOfficialRoutes: Boolean) {
        _routeNamePart = routeNamePart
        _onlyOfficialRoutes = onlyOfficialRoutes
        displayContent()
    }

    private fun _isOfficialRoute(route: Route): Boolean {
        return route.statusId != Route.STATUS_CLOSED
            && route.statusId != Route.STATUS_UNACKNOWLEDGED
            && route.statusId != Route.STATUS_UNFINISHED
    }

    private fun _getRouteBackground(route: Route): Int {
        val defaultBgColor = if (route.statusId == Route.STATUS_CLOSED)
                visualUtils.prohibitedBgColor
            else
                visualUtils.defaultBgColor
        return AscendStyle.deriveAscentColor(
            ascentBitMask = route.ascendsBitMask,
            leadColor = visualUtils.leadBgColor,
            followColor = visualUtils.followBgColor,
            defaultColor = defaultBgColor)
    }

    private fun _getRouteTypeface(route: Route): Int {
        return if (route.statusId == Route.STATUS_CLOSED)
                Typeface.ITALIC
            else
                Typeface.BOLD
    }

    private fun _getRouteMainText(route: Route): Pair<String, String> {
        val routeName = ParserUtils.decodeObjectNames(route.name)
        val commentCount = db.getRouteCommentCount(route.id)
        val commentCountAdd = if (commentCount > 0) "   [$commentCount]" else ""

        val decorationAdd = AscendStyle.deriveAscentDecoration(
            route.ascendsBitMask,
            visualUtils.botchIcon,
            visualUtils.projectIcon,
            visualUtils.watchingIcon)
        return Pair("${routeName.first}$commentCountAdd$decorationAdd", route.grade.orEmpty())
    }

    private fun _getRouteSectorInfo(route: Route): Pair<String, String>? {
        var sectorInfo = ""
        if (activityLevel.level < ClimbingObjectLevel.eRoute) {
            db.getRock(route.parentId)?.let { rock ->
                if (activityLevel.level < ClimbingObjectLevel.eRock) {
                    db.getSector(rock.parentId)?.let { sector ->
                        if (activityLevel.level < ClimbingObjectLevel.eSector) {
                            db.getRegion(sector.parentId)?.let { region ->
                                sectorInfo = "${region.name} ${visualUtils.arrow} "
                            }
                        }
                        val sectorNames = ParserUtils.decodeObjectNames(sector.name)
                        val altName = if (sectorNames.second.isNotEmpty()) " / ${sectorNames.second}" else ""
                        sectorInfo += "${sectorNames.first} $altName ${visualUtils.arrow} "
                    }
                }
                val rockNames = ParserUtils.decodeObjectNames(rock.name)
                val altName = if (rockNames.second.isNotEmpty()) " / ${rockNames.second}" else ""
                sectorInfo += "${rock.nr} ${rockNames.first} $altName"
            }
        }
        return if (sectorInfo.isNotEmpty())
                Pair(sectorInfo, "")
            else
                null
    }

    private fun _onRouteSelected(route: Route) {
        startActivity(Intent(this@RouteActivity, DescriptionActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eUnknown.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, route.id)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, route.name)
        })
    }

    private fun _noNameFilter() = _filterName.isEmpty()
    private fun _noGradeFilter() = _filterMinGradeId == RouteComment.NO_INFO_ID
                                       && _filterMaxGradeId == RouteComment.NO_INFO_ID
    private fun _noQualityFilter() = _filterMaxQualityId == RouteComment.NO_INFO_ID
    private fun _noProtectionFilter() = _filterMaxProtectionId == RouteComment.NO_INFO_ID
    private fun _noDryingFilter() = _filterMaxDryingId == RouteComment.NO_INFO_ID
}

class RouteGetter(
    val getAll: () -> List<Route>,
    val getByName: () -> List<Route>,
    val getByGrade: () -> List<Route>,
    val getByQuality: () -> List<Route>,
    val getByProtection: () -> List<Route>,
    val getByDrying: () -> List<Route>,
    val getProjects: () -> List<Route>,
    val getBotches: () -> List<Route>
)
