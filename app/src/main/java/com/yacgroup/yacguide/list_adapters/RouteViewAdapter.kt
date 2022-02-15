/*
 * Copyright (C) 2022 Axel Paetzold
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

package com.yacgroup.yacguide.list_adapters

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yacgroup.yacguide.ClimbingObjectLevel
import com.yacgroup.yacguide.R
import com.yacgroup.yacguide.database.DatabaseWrapper
import com.yacgroup.yacguide.database.Route
import com.yacgroup.yacguide.utils.AscendStyle
import com.yacgroup.yacguide.utils.ParserUtils

class RouteViewAdapter(
    context: Context,
    customSettings: SharedPreferences,
    private val _climbingObjectLevel: ClimbingObjectLevel,
    private val _db: DatabaseWrapper,
    private val _onClick: (Int, String) -> Unit)
    : ListAdapter<Route, RecyclerView.ViewHolder>(RouteDiffCallback) {

    private val _defaultBgColor = ContextCompat.getColor(context, R.color.colorSecondaryLight)
    private val _prohibitedBgColor = ContextCompat.getColor(context, R.color.colorSecondary)
    private val _leadBgColor = customSettings.getInt(
        context.getString(R.string.lead),
        ContextCompat.getColor(context, R.color.color_lead))
    private val _followBgColor = customSettings.getInt(
        context.getString(R.string.follow),
        ContextCompat.getColor(context, R.color.color_follow))
    private val _botchIcon = context.getString(R.string.botch)
    private val _projectIcon = context.getString(R.string.project)
    private val _watchingIcon = context.getString(R.string.watching)
    private val _arrow = context.getString(R.string.right_arrow)

    inner class RouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val _listItemLayout = view.findViewById<LinearLayout>(R.id.listItemLayout)
        private val _infoTextLayout = view.findViewById<ConstraintLayout>(R.id.infoTextLayout)
        private val _infoLeftTextView = view.findViewById<TextView>(R.id.infoLeftTextView)
        private val _mainLeftTextView = view.findViewById<TextView>(R.id.mainLeftTextView)
        private val _mainRightTextView = view.findViewById<TextView>(R.id.mainRightTextView)
        private val _subTextView = view.findViewById<TextView>(R.id.subTextView)

        fun bind(route: Route) {
            val routeName = ParserUtils.decodeObjectNames(route.name)
            val commentCount = _db.getRouteCommentCount(route.id)
            val commentCountAdd = if (commentCount > 0) "   [$commentCount]" else ""
            var bgColor = _defaultBgColor
            var typeface = Typeface.BOLD
            if (route.statusId == Route.STATUS_CLOSED) {
                typeface = Typeface.ITALIC
                bgColor = _prohibitedBgColor
            }
            bgColor = AscendStyle.deriveAscentColor(
                route.ascendsBitMask,
                _leadBgColor,
                _followBgColor,
                defaultColor = bgColor)
            val decorationAdd = AscendStyle.deriveAscentDecoration(
                route.ascendsBitMask,
                _botchIcon,
                _projectIcon,
                _watchingIcon
            )
            val sectorInfo = _getSectorInfo(route)
            if (sectorInfo.isNotEmpty()) {
                _infoTextLayout.visibility = View.VISIBLE
                _infoLeftTextView.text = sectorInfo
            }
            _mainLeftTextView.setTypeface(null, typeface)
            _mainLeftTextView.text = "${routeName.first}$commentCountAdd$decorationAdd"
            _mainRightTextView.setTypeface(null, typeface)
            _mainRightTextView.text = route.grade.orEmpty()
            _subTextView.setTypeface(null, typeface)
            _subTextView.text = routeName.second
            _listItemLayout.apply {
                setBackgroundColor(bgColor)
                setOnClickListener {
                    _onClick(route.id, route.name.orEmpty())
                }
            }
        }

        private fun _getSectorInfo(route: Route): String {
            var sectorInfo = ""
            if (_climbingObjectLevel < ClimbingObjectLevel.eRoute) {
                _db.getRock(route.parentId)?.let { rock ->
                    if (_climbingObjectLevel < ClimbingObjectLevel.eRock) {
                        _db.getSector(rock.parentId)?.let { sector ->
                            if (_climbingObjectLevel < ClimbingObjectLevel.eSector) {
                                _db.getRegion(sector.parentId)?.let { region ->
                                    sectorInfo = "${region.name} $_arrow "
                                }
                            }
                            val sectorNames = ParserUtils.decodeObjectNames(sector.name)
                            val altName = if (sectorNames.second.isNotEmpty()) " / ${sectorNames.second}" else ""
                            sectorInfo += "${sectorNames.first} $altName $_arrow "
                        }
                    }
                    val rockNames = ParserUtils.decodeObjectNames(rock.name)
                    val altName = if (rockNames.second.isNotEmpty()) " / ${rockNames.second}" else ""
                    sectorInfo += "${rockNames.first} $altName"
                }
            }
            return sectorInfo
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.list_data_item, viewGroup, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as RouteViewHolder).bind(getItem(position) as Route)
    }
}

object RouteDiffCallback : DiffUtil.ItemCallback<Route>() {
    override fun areItemsTheSame(oldRoute: Route, newRoute: Route): Boolean {
        return oldRoute.id == newRoute.id
    }

    override fun areContentsTheSame(oldRoute: Route, newRoute: Route): Boolean {
        return oldRoute == newRoute
    }
}