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

import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Rock
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.IntentConstants
import com.yacgroup.yacguide.utils.ParserUtils
import com.yacgroup.yacguide.utils.SearchBarHandler
import com.yacgroup.yacguide.utils.WidgetUtils

class RouteActivity : TableActivity() {

    private lateinit var _rock: Rock
    private lateinit var _searchBarHandler: SearchBarHandler
    private var _onlyOfficialRoutes: Boolean = false
    private var _routeNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _rock = db.getRock(activityLevel.parentId)!!

        val rockStatus = when (_rock.status){
            Rock.statusCollapsed -> getString(R.string.rock_collapsed)
            Rock.statusProhibited -> getString(R.string.rock_fully_prohibited)
            Rock.statusTemporarilyProhibited -> getString(R.string.rock_temporarily_prohibited)
            Rock.statusPartlyProhibited -> getString(R.string.rock_partly_prohibited)
            else -> {
                if (_rock.type == Rock.typeUnofficial) {
                    getString(R.string.rock_inofficial)
                } else {
                    ""
                }
            }
        }
        findViewById<TextView>(R.id.infoTextView).text = rockStatus

        _searchBarHandler = SearchBarHandler(
            findViewById(R.id.searchBarLayout),
            R.string.route_search,
            getString(R.string.only_official_routes),
            resources.getBoolean(R.bool.only_official_routes),
            customSettings
        ) { routeNamePart, onlyOfficialRoutes -> _onSearchBarUpdate(routeNamePart, onlyOfficialRoutes) }
    }

    override fun getLayoutId() = R.layout.activity_route

    @Suppress("UNUSED_PARAMETER")
    fun showMap(v: View) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:${_rock.latitude},${_rock.longitude}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_map_app_available, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showComments(v: View) {
        showRockComments(_rock.id)
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val rockName = ParserUtils.decodeObjectNames(_rock.name)
        this.title = if (rockName.first.isNotEmpty()) rockName.first else rockName.second

        var routes = db.getRoutes(_rock.id)
        if (_onlyOfficialRoutes) {
            routes = routes.filter { _isOfficialRoute(it) }
        }

        for (route in routes) {
            val routeName = ParserUtils.decodeObjectNames(route.name)
            if (_routeNamePart.isNotEmpty() && routeName.toList().none{ it.lowercase().contains(_routeNamePart.lowercase()) }) {
                continue
            }
            val commentCount = db.getRouteCommentCount(route.id)
            val commentCountAdd = if (commentCount > 0) "   [$commentCount]" else ""
            val onCLickListener = View.OnClickListener {
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
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = "${routeName.first}$commentCountAdd$decorationAdd",
                    textRight = route.grade.orEmpty(),
                    onClickListener = onCLickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    textLeft = routeName.second,
                    textSizeDp = WidgetUtils.textFontSizeDp,
                    onClickListener = onCLickListener,
                    bgColor = bgColor,
                    typeface = typeface))
            layout.addView(WidgetUtils.createHorizontalLine(this, 1))
        }
    }

    override fun onStop() {
        _searchBarHandler.storeCustomSettings(getString(R.string.only_official_routes))
        super.onStop()
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

