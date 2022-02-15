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

package com.yacgroup.yacguide

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

import com.yacgroup.yacguide.activity_properties.AscentFilterable
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.list_adapters.RouteViewAdapter
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.SearchBarHandler

class RouteActivity : TableActivityWithOptionsMenu() {

    private lateinit var _viewAdapter: RouteViewAdapter
    private lateinit var _searchBarHandler: SearchBarHandler
    private var _onlyOfficialRoutes: Boolean = false
    private var _routeNamePart: String = ""
    private var _filterProjects: Boolean = false
    private var _filterBotches: Boolean = false
    private var _rock: Rock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _filterProjects = intent.getBooleanExtra(IntentConstants.FILTER_PROJECTS, false)
        _filterBotches = intent.getBooleanExtra(IntentConstants.FILTER_BOTCHES, false)

        properties = arrayListOf(AscentFilterable(this))

        _searchBarHandler = SearchBarHandler(
            findViewById(R.id.searchBarLayout),
            R.string.route_search,
            getString(R.string.only_official_routes),
            resources.getBoolean(R.bool.only_official_routes),
            customSettings,
            { onlyOfficialRoutes -> _onlyOfficialRoutes = onlyOfficialRoutes },
            { routeNamePart, onlyOfficialRoutes -> _onSearchBarUpdate(routeNamePart, onlyOfficialRoutes) })

        if (activityLevel.level == ClimbingObjectLevel.eRoute) {
            _rock = db.getRock(activityLevel.parentId)
        } else {
            findViewById<ImageButton>(R.id.mapButton).visibility = View.INVISIBLE
        }

        _viewAdapter = RouteViewAdapter(
            this,
            customSettings,
            activityLevel.level,
            db
        ) { routeId, routeName ->
            _onRouteSelected(routeId, routeName)
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
            ClimbingObjectLevel.eSector -> showRegionComments(activityLevel.parentId)
            ClimbingObjectLevel.eRock -> showSectorComments(activityLevel.parentId)
            ClimbingObjectLevel.eRoute -> showRockComments(activityLevel.parentId)
            else -> showNoCommentToast()
        }
    }

    override fun displayContent() {
        val levelName = ParserUtils.decodeObjectNames(activityLevel.parentName)
        this.title = if (levelName.first.isNotEmpty()) levelName.first else levelName.second
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
        _searchBarHandler.storeCustomSettings(getString(R.string.only_official_routes))
        super.onStop()
    }

    private fun _getAndFilterRoutes(): List<Route> = when (activityLevel.level) {
        ClimbingObjectLevel.eCountry -> {
            if (_filterProjects) db.getProjectedRoutes()
            else if (_filterBotches) db.getBotchedRoutes()
            else db.getRoutes()
        }
        ClimbingObjectLevel.eRegion -> {
            if (_filterProjects) db.getProjectedRoutesForCountry(activityLevel.parentName)
            else if (_filterBotches) db.getBotchedRoutesForCountry(activityLevel.parentName)
            else db.getRoutesForCountry(activityLevel.parentName)
        }
        ClimbingObjectLevel.eSector -> {
            if (_filterProjects) db.getProjectedRoutesForRegion(activityLevel.parentId)
            else if (_filterBotches) db.getBotchedRoutesForRegion(activityLevel.parentId)
            else db.getRoutesForRegion(activityLevel.parentId)
        }
        ClimbingObjectLevel.eRock -> {
            if (_filterProjects) db.getProjectedRoutesForSector(activityLevel.parentId)
            else if (_filterBotches) db.getBotchedRoutesForSector(activityLevel.parentId)
            else db.getRoutesForSector(activityLevel.parentId)
        }
        ClimbingObjectLevel.eRoute -> {
            if (_filterProjects) db.getProjectedRoutesForRock(activityLevel.parentId)
            else if (_filterBotches) db.getBotchedRoutesForRock(activityLevel.parentId)
            else db.getRoutesForRock(activityLevel.parentId)
        }
        else -> emptyList()
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

    private fun _onRouteSelected(routeId: Int, routeName: String) {
        startActivity(Intent(this@RouteActivity, DescriptionActivity::class.java).apply {
            putExtra(IntentConstants.CLIMBING_OBJECT_LEVEL, ClimbingObjectLevel.eUnknown.value)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, routeId)
            putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, routeName)
        })
    }
}

