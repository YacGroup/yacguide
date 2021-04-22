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

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*

import com.yacgroup.yacguide.database.comment.RockComment
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
    private var _routeFilter: SearchBarHandler.ElementFilter = SearchBarHandler.ElementFilter.eNone
    private var _routeNamePart: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rockId = intent.getIntExtra(IntentConstants.ROCK_KEY, DatabaseWrapper.INVALID_ID)

        _rock = db.getRock(rockId)!!
        _searchBarHandler = SearchBarHandler(findViewById(R.id.searchBarLayout), R.string.route_search, R.array.routeFilters) {
            routeNamePart, routeFilter -> _onSearchBarUpdate(routeNamePart, routeFilter)
        }

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
    }

    @Suppress("UNUSED_PARAMETER")
    fun showMap(v: View) {
        val gmmIntentUri = Uri.parse("geo:${_rock.latitude},${_rock.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        if (mapIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.no_map_app_available, Toast.LENGTH_SHORT).show()
        } else {
            startActivity(mapIntent)
        }
    }

    override fun showComments(v: View) {
        val comments = _rock.let { db.getRockComments(it.id) } ?: emptyList()
        if (comments.isNotEmpty()) {
            prepareCommentDialog().findViewById<LinearLayout>(R.id.commentLayout)?.let {
                for ((idx, comment) in comments.withIndex()) {
                    val qualityId = comment.qualityId
                    val text = comment.text

                    if (idx > 0) {
                        it.addView(WidgetUtils.createHorizontalLine(this, 1))
                    }
                    if (RockComment.QUALITY_MAP.containsKey(qualityId)) {
                        it.addView(WidgetUtils.createCommonRowLayout(this,
                                textLeft = getString(R.string.nature),
                                textRight = RockComment.QUALITY_MAP[qualityId].orEmpty(),
                                textSizeDp = WidgetUtils.textFontSizeDp,
                                typeface = Typeface.NORMAL))
                    }
                    it.addView(WidgetUtils.createCommonRowLayout(this,
                            textLeft = text.orEmpty(),
                            textSizeDp = WidgetUtils.textFontSizeDp,
                            typeface = Typeface.NORMAL))
                }
            }
        } else {
            showNoCommentToast()
        }
    }

    override fun displayContent() {
        val layout = findViewById<LinearLayout>(R.id.tableLayout)
        layout.removeAllViews()
        val rockName = ParserUtils.decodeObjectNames(_rock.name)
        this.title = if (rockName.first.isNotEmpty()) rockName.first else rockName.second

        val routes = when (_routeFilter) {
            SearchBarHandler.ElementFilter.eOfficial -> db.getRoutes(_rock.id).filter { _isOfficialRoute(it) }
            SearchBarHandler.ElementFilter.eProject -> db.getProjectedRoutes(_rock.id)
            SearchBarHandler.ElementFilter.eBotch -> db.getBotchedRoutes(_rock.id)
            else -> db.getRoutes(_rock.id)
        }
        for (route in routes) {
            val routeName = ParserUtils.decodeObjectNames(route.name)
            if (_routeNamePart.isNotEmpty() && routeName.toList().none{ it.toLowerCase().contains(_routeNamePart.toLowerCase()) }) {
                continue
            }
            val commentCount = db.getRouteCommentCount(route.id)
            val commentCountAdd = if (commentCount > 0) "   [$commentCount]" else ""
            val onCLickListener = View.OnClickListener {
                val intent = Intent(this@RouteActivity, DescriptionActivity::class.java)
                intent.putExtra(IntentConstants.ROUTE_KEY, route.id)
                startActivityForResult(intent, 0)
            }
            val statusId = route.statusId
            var typeface = Typeface.BOLD
            var bgColor = Color.WHITE
            if (statusId == Route.statusClosed) {
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

    private fun _onSearchBarUpdate(routeNamePart: String, routeFilter: SearchBarHandler.ElementFilter) {
        _routeNamePart = routeNamePart
        _routeFilter = routeFilter
        displayContent()
    }

    private fun _isOfficialRoute(route: Route): Boolean {
        return route.statusId != Route.statusClosed
            && route.statusId != Route.statusUnacknowledged
            && route.statusId != Route.statusUnfinished
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_route
    }
}

