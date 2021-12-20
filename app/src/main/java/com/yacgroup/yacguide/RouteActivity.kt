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

package com.yacgroup.yacguide

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*

import com.yacgroup.yacguide.activity_properties.AscentFilterable
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.SearchBarHandler
import com.yacgroup.yacguide.utils.WidgetUtils

class RouteActivity : TableActivityWithOptionsMenu() {

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
            customSettings
        ) { routeNamePart, onlyOfficialRoutes -> _onSearchBarUpdate(routeNamePart, onlyOfficialRoutes) }

        if (activityLevel.level == ClimbingObjectLevel.eRoute) {
            _rock = db.getRock(activityLevel.parentId)
        } else {
            findViewById<ImageButton>(R.id.mapButton).visibility = View.INVISIBLE
        }
    }

    override fun getLayoutId() = R.layout.activity_route

    @Suppress("UNUSED_PARAMETER")
    fun showMap(v: View) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:${_rock?.latitude},${_rock?.longitude}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
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
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()

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

        for (route in routes) {
            val routeName = ParserUtils.decodeObjectNames(route.name)
            val commentCount = db.getRouteCommentCount(route.id)
            val commentCountAdd = if (commentCount > 0) "   [$commentCount]" else ""
            val onClickListener = View.OnClickListener {
                val intent = Intent(this@RouteActivity, DescriptionActivity::class.java)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_ID, route.id)
                intent.putExtra(IntentConstants.CLIMBING_OBJECT_PARENT_NAME, route.name)
                startActivityForResult(intent, 0)
            }
            val statusId = route.statusId
            var typeface = Typeface.BOLD
            var bgColor = Color.WHITE
            if (statusId == Route.STATUS_CLOSED) {
                typeface = Typeface.ITALIC
                bgColor = Color.LTGRAY
            }
            bgColor = colorizeEntry(route.ascendsBitMask, bgColor)
            val decorationAdd = decorateEntry(route.ascendsBitMask)

            val sectorInfo = _getSectorInfo(route)
            if (sectorInfo.isNotEmpty()) {
                layout.addView(
                    WidgetUtils.createCommonRowLayout(
                        this,
                        textLeft = sectorInfo,
                        textSizeDp = WidgetUtils.textFontSizeDp,
                        onClickListener = onClickListener,
                        bgColor = bgColor,
                        typeface = Typeface.NORMAL
                    )
                )
            }
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = "${routeName.first}$commentCountAdd$decorationAdd",
                    textRight = route.grade.orEmpty(),
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = routeName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onClickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
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

    private fun _getSectorInfo(route: Route): String {
        val arrow = getString(R.string.right_arrow)
        var sectorInfo = ""
        if (activityLevel.level.value < ClimbingObjectLevel.eRoute.value) {
            val rock = db.getRock(route.parentId)!!
            if (activityLevel.level.value < ClimbingObjectLevel.eRock.value) {
                val sector = db.getSector(rock.parentId)!!
                if (activityLevel.level.value < ClimbingObjectLevel.eSector.value) {
                    val region = db.getRegion(sector.parentId)!!
                    sectorInfo = "${region.name} $arrow "
                }
                val sectorNames = ParserUtils.decodeObjectNames(sector.name)
                val altName = if (sectorNames.second.isNotEmpty()) " / ${sectorNames.second}" else ""
                sectorInfo += "${sectorNames.first} $altName $arrow "
            }
            val rockNames = ParserUtils.decodeObjectNames(rock.name)
            val altName = if (rockNames.second.isNotEmpty()) " / ${rockNames.second}" else ""
            sectorInfo += "${rockNames.first} $altName"
        }
        return sectorInfo
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
}

